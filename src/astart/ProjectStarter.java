package astart;
import general.EegData;
import general.ITimeManagerListener;
import general.TimeManager;
import hardware.DriverExecutor;
import hardware.HddRecordingPlayer;

import java.util.Random;

import javolution.util.FastTable;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import result.ResultManager;
import signalProcessing.AveragingFilter;
import signalProcessing.Classifier;
import signalProcessing.ConvolutionFilter;
import signalProcessing.SignalAccumulator;
import signalProcessing.SignalProcessor;
import training.EegDataWithDescr;
import training.Trainer;
import collect.DataObject;
import collect.HardDriveCollector;
import collect.IShowedSymbols;
import configuration.Configuration;
import configuration.ConfigurationLoader;
import configuration.ElementClassificatorConf;
import configuration.ElementFrequnecyFilter;
import configuration.ElementMatrixDefinition;
import configuration.IConfigurationListener;
import display.ControlPanel;
import display.DisplayUpdater;
import display.MatrixResponsesCanvas;


public class ProjectStarter extends Thread implements IConfigurationListener {

	public static final String VERSION = "0.96";

	/**
	 * @param args
	 */
	public ProjectStarter(){
		jEEG = this ;
	}

	private static DriverExecutor drv  ;
	private static TimeManager timeManager;
	private static Configuration conf;
	
	private static ResultManager resultManager = new ResultManager() ;

	private static ProjectStarter jEEG ;
	private static ControlPanel cp;
	private static SignalAccumulator sacc;
	private static SignalProcessor sp;

	public SignalProcessor getSignalProcessor(){
		return this.sp ;
	}
	
	public ResultManager getResultManager(){
		return resultManager ;
	}
	
	public SignalAccumulator getSignalAccumulator(){
		return sacc ;
	}

	private static DisplayUpdater displayUpdater;
	private static HardDriveCollector hddCollector;
	
	public HardDriveCollector getHddCollector(){
		return hddCollector ;
	}
	private static Trainer trainer;

	public static ProjectStarter getInstance(){
		if(jEEG==null)
			jEEG = new ProjectStarter() ;
		return jEEG ;
	}
	private static Random rnd = new Random() ;
	private static Classifier classifier;
//	private static ProtocolExecutor protoExecutor;
	
	public static Classifier getClassifier(){
		return classifier ;
	}
	
	private static void updateTiming(){
		int interspaceTime = conf.getValueInt("interspaceTimeMin")+rnd.nextInt(conf.getValueInt("interspaceTimeMax")-conf.getValueInt("interspaceTimeMin"));
		System.out.println("[ProjectStarter] interspace time = "+interspaceTime);
		if(timeManager.getListenersChainSize()==0){
			timeManager.addTimeManagerListenerChain(new long[]{interspaceTime,conf.getValueInt("presentationTime")}, new int[]{ITimeManagerListener.ACTION_HIGHLIGHT,ITimeManagerListener.ACTION_CHANGE_POS}, true);
			timeManager.addTimeManagerListenerChain(new long[]{interspaceTime+conf.getValueInt("presentationTime"),conf.getValueInt("responseSpan")}, new int[]{ITimeManagerListener.PROCESSING_START,ITimeManagerListener.PROCESSING_END}, true);
		}else{
			timeManager.updateTimeSpan(ITimeManagerListener.ACTION_HIGHLIGHT,interspaceTime) ;
			timeManager.updateTimeSpan(ITimeManagerListener.ACTION_CHANGE_POS,conf.getValueInt("presentationTime"));
			switch(ProjectStarter.getConf().getValueInt("classificationType")){
			case Configuration.CLASSIFICATION_WINDOW:
				sp.setStartTime(conf.getValueInt("responseDelay"));
				sp.setEndTime(conf.getValueInt("responseSpan")+conf.getValueInt("responseDelay"));
				break;

			case Configuration.CLASSIFICATION_WEIGHTS:
				System.out.println("[ProjectStarter] updating time for type WEIGHTS");
				int beginTime = ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true).getFirst().span ;
				int endTime = ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true).getLast().span ;
				System.out.println("[ProjectStarter:updateTiming()] begin "+beginTime+ " len time "+(endTime-beginTime));
				sp.setStartTime(beginTime);
				sp.setEndTime(endTime);
				break;
			case Configuration.CLASSIFICATION_AVG_IN_WINDOW:
				beginTime = ProjectStarter.getConf().getValueInt("classificationAvrageFrom") ;
				endTime = ProjectStarter.getConf().getValueInt("classificationAvrageTo") ;
				System.out.println("[ProjectStarter:updateTiming()] begin "+beginTime+ " len time "+(endTime-beginTime));
				sp.setStartTime(beginTime);
				sp.setEndTime(endTime);
				break ;
			}
		}
	}
	
	static Logger logger = Logger.getLogger(ProjectStarter.class.getName());

	
	public static void main(String[] args) {
		logger.debug("Starting... jEEG "+VERSION);
		PropertyConfigurator.configure("log4jconf.txt");
		new Thread("High frequency daemon") {
			{ this.setDaemon(true); this.start(); }
			public void run() {
				while(true) {
					System.out.println("High frequency daemon stared.");
					try {
						Thread.sleep(Integer.MAX_VALUE);
					}
					catch(InterruptedException ex) {
					}
				}
			}
		};
		System.out.println("Loading configuration...");
		if(args.length>0 && args[0]!=null){
			System.out.println("Loading configuration... from file "+args[0]);
			conf = ConfigurationLoader.load(args[0]) ;
		}
		else
			conf = ConfigurationLoader.load() ;
		    
		System.out.println("Starting drivers and timers...");
		
		drv = new DriverExecutor() ;	
		Thread drvThread = new Thread(drv,"Driver executor");
		drvThread.setPriority(Thread.MAX_PRIORITY);
		drvThread.start() ;
					
		conf.addConfigurationListener(getInstance()) ;
		timeManager = new TimeManager() ;
		trainer = new Trainer() ;
		hddCollector = new HardDriveCollector(conf.getValueString("subjectName"),conf);

		classifier = new Classifier() ;
		
		conf.addConfigurationListener(hddCollector) ;
		conf.addConfigurationListener(trainer) ;
		conf.addConfigurationListener(resultManager) ;
		conf.addConfigurationListener(drv) ;
		conf.addConfigurationListener(classifier);
		Display display = new Display();
		Shell s = new Shell(display);
		cp = new ControlPanel(s) ;
		hddCollector.setDisplayedObjects((IShowedSymbols) cp.getCanvasUserVisibleMatrix());
		sacc = new SignalAccumulator(conf.getValueInt("signalRepeats"),new int[]{6,5}) ;
		sp = new SignalProcessor() ;
		displayUpdater = new DisplayUpdater() ;
		
		drv.addDriverListener(cp.getCanvasRawEegData()) ;
		drv.addDriverListener(trainer);
		drv.addDriverListener(sp) ;
		drv.addDriverListener(hddCollector);
		drv.addSignalFilter(new AveragingFilter());
		
		sp.addSignalProcessorListener(sacc);
		sacc.addSignalAccumulatorListener(cp.getCanvasMatrixResponses());
		sacc.addSignalAccumulatorWinnerListeners(cp);
		sacc.addSignalAccumulatorWinnerListeners(resultManager);
		displayUpdater.addListener(cp.getCanvasUserVisibleMatrix()) ;

		timeManager.addTimeManagerListener(displayUpdater, new int[]{ITimeManagerListener.ACTION_HIGHLIGHT,ITimeManagerListener.ACTION_CHANGE_POS}) ;
		timeManager.addTimeManagerListener(sp, new int[]{ITimeManagerListener.ACTION_HIGHLIGHT});
		timeManager.addTimeManagerListener(trainer, new int[]{ITimeManagerListener.ACTION_HIGHLIGHT}) ;

		updateTiming() ;
		
		drv.addDriverListener(timeManager);
		
		conf.addConfigurationListener(displayUpdater);
		conf.addConfigurationListener((IConfigurationListener) cp.getCanvasRawEegData()) ;
		conf.addConfigurationListener((IConfigurationListener) cp.getRawEeegDataCanvasScale()) ;
		
		displayUpdater.updateDisplayMatrix(((ElementMatrixDefinition) ProjectStarter.getConf().getElement("userVisibleMatrixSmall")).getMatrixValues(),((ElementMatrixDefinition) ProjectStarter.getConf().getElement("userVisibleMatrixSmall")).getMatrixDimensions());
		
		while (!cp.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		resultManager.stopRecording();
		drv.setRunState(false) ;
		timeManager.setRunState(false) ;
		getInstance().setRunState(false) ;
		displayUpdater.setRunState(false);
		synchronized(displayUpdater){
			displayUpdater.notify();
		}

		sp.setRunState(false);
		synchronized(sp){
			sp.notify();
		}
		synchronized(getInstance()){
			getInstance().notify() ;
		}
		hddCollector.setRunState(false);
		synchronized(hddCollector){
			hddCollector.notify() ;
		}
		trainer.setRunState(false);
		synchronized(trainer){
			trainer.notify() ;
		}
		System.out.println("Shutting down...");
	}

	public static Configuration getConf() {
		return conf;
	}

	public static TimeManager getTimeManager() {
		return timeManager;
	}
	
	public static Trainer getTrainer() {
		return trainer;
	}

	public static ControlPanel getControlPanel() {
		return cp;
	}

	public void stopLearning(){
		hddCollector.addData(new DataObject(DataObject.HEADER_NORMAL));
	}

	public void startLearning(){
		updateConfigurationChanges() ;
		System.out.println("STARTING learning sequence");

		if(conf.getValueInt("signalSource")==Configuration.SOURCE_HDD){
			// fastforward to initialization sequence data
			DataObject header = ((HddRecordingPlayer) drv.getPacketSupply()).goToInitializationSession();
			System.out.println("Got header: "+header.getData());
			FastTable<FastTable<EegDataWithDescr>> repetitions = new FastTable<FastTable<EegDataWithDescr>>() ;
			FastTable<EegDataWithDescr> data = new FastTable<EegDataWithDescr>() ;
			DataObject packet;
			FastTable <int[]> positivePositions = (FastTable<int[]>) ((HddRecordingPlayer) drv.getPacketSupply()).getDataPacket().getData() ;
			FastTable <int[]> highlightedPositions = (FastTable<int[]>) ((HddRecordingPlayer) drv.getPacketSupply()).getDataPacket().getData() ;
			System.out.println("Highlighted positions: ");
			for(int[] positions : highlightedPositions){
				for(int pos : positions)
					System.out.print(" "+pos) ;
				System.out.println("");
			}
			System.out.println("Positive positions: ");
			for(int[] positions : positivePositions){
				for(int pos : positions)
					System.out.print(" "+pos) ;
				System.out.println("");
			}
			System.out.println("");
			Configuration conf  = (Configuration) ((HddRecordingPlayer) drv.getPacketSupply()).getDataPacket().getData() ;
			boolean addData = false ;
			int positive = -1 ;
			int tempRep = 0 ;
			int tempElem = 0 ;
			System.out.println("[ProjectStarter:startlearning] loading data");
			while((packet = ((HddRecordingPlayer) drv.getPacketSupply()).getDataPacket())!=null){
				if(packet.getDataType()==DataObject.HEADER_INITIALIZATION_NEXT_REPETITION){
					if(data.size()>0)
						repetitions.add(data);
					data = new FastTable<EegDataWithDescr>() ; 
					tempElem=0;
				}else if(packet.getDataType()==DataObject.HEADER_INITIALIZATION_SYMBOL_END){
					// start delay between presenting next symbol
					addData = false ;
				}else if(packet.getDataType()==DataObject.HEADER_INITIALIZATION_NEXT_HIGHLIGHT){
					positive = (Integer) packet.getData();
					data.add(new EegDataWithDescr(null,EegDataWithDescr.NEGATIVE));
					addData = true ;
					continue ;
				}else if(packet.getDataType()==DataObject.HEADER_INITIALIZATION_SYMBOL_START){
					// end of delay between presenting next symbol
				}else if(packet.getDataType()!=DataObject.TYPE_EEGDATA)
					break ;
				if(addData&&positive!=-1){
					tempElem++;
					data.add(new EegDataWithDescr((EegData) packet.getData(),positive));
					}
			}
			
			// generate data for trainer
			trainer.drawRsquareSpecturm();
		}else{
			// not from HDD - random or COM positivePositions
			if(!hddCollector.getRunState())
				new Thread(hddCollector,"HardDriveCollector").start() ;
			
			trainer.initialize() ;
			sp.reset();
			if(timeManager.getRunState()==false){
				if(sp.getRunState())
					sp.setRunState(false) ;
				
				timeManager.setRunState(true);
				timeManager.setMakeFastReset(true);
				makeSynchronization() ;
				
				Thread train = new Thread(trainer,"Trainer");
				Thread thr = new Thread(timeManager,"Time manager");
				thr.setPriority(Thread.MAX_PRIORITY);
				thr.start() ;
				train.start() ;
				
			}else{
				Thread train = new Thread(trainer,"Trainer");
				Thread thr = new Thread(timeManager,"Time manager");
				thr.setPriority(Thread.MAX_PRIORITY);
				thr.start() ;
				new Thread(displayUpdater,"DisplayUpdater").start();
				train.start() ;

				getInstance().start() ;
			}
			
			DataObject header = new DataObject(DataObject.HEADER_INITIALIZATION);
			header.setData(cp.getTextOutput().getText()) ;
			hddCollector.addHeader(header);
			header = new DataObject(DataObject.HEADER_INITIALIZATION);
			header.setData(trainer.getPositivePositions()) ;
			hddCollector.addHeader(header);
			
			System.out.println("[ProjectStarter:trainingStart] writing positive pos LEN "+trainer.getPositivePositions().length);
		}

	}

	public void startRunning(){
		if(timeManager.getRunState()==false){
			timeManager.setRunState(true);
			timeManager.setMakeFastReset(true);
			if(hasConfigurationChanged)
				updateConfigurationChanges();
			makeSynchronization() ;	
			Thread thr = new Thread(timeManager,"Time manager") ;
			thr.setPriority(Thread.MAX_PRIORITY);
			thr.start() ;
		}else{
			// First run
			Thread thr = new Thread(timeManager,"Time manager");
			thr.setPriority(Thread.MAX_PRIORITY);
			thr.start() ;
			getInstance().start() ;
		}
		if(!sp.getRunState())
			new Thread(sp,"SignalProcessor").start() ;
		if(!hddCollector.getRunState())
			new Thread(hddCollector,"HardDriveCollector").start() ;
		if(!displayUpdater.isRunState())
			new Thread(displayUpdater,"DisplayUpdater").start();

		System.out.println("STARTED");
	}
	public volatile boolean hasConfigurationChanged = false ;

	public void configurationChanged() {
		updateTiming() ;
		System.out.println("[ProjectStarter] updating singal source : "+conf.getValueInt("signalSource"));
		HddRecordingPlayer hdd;
		switch (conf.getValueInt("signalSource")){

		case Configuration.SOURCE_HDD:
			hdd = new HddRecordingPlayer(conf.getValueString("recordedFile")) ;
			Configuration oldConf = conf;

			conf = hdd.getConfiguration() ;
			conf.setConfListeners(oldConf.getConfListeners());
			conf.setValue("signalSource",oldConf.getValueInt("signalSource")) ;
			conf.setValue("recordedFile",oldConf.getValueString("recordedFile")) ;
			if(!(drv.getPacketSupply() instanceof HddRecordingPlayer))
				drv.setPacketSupply(hdd);
			break ;
		}

		for(ElementFrequnecyFilter elem : conf.getFilters()){
			if(elem.getValue()==1&&!drv.hasFilter(elem.getName())){
				ConvolutionFilter convFilter = new ConvolutionFilter(elem.getValues(ElementFrequnecyFilter.COEFF_A),elem.getValues(ElementFrequnecyFilter.COEFF_B),elem.getName()) ;
				drv.addSignalFilter(convFilter) ;
			}else if(elem.getValue()==0){
				drv.removeSignalFilter(elem.getName()) ;
			}
		}

		hasConfigurationChanged = true ;
	}

	public void updateConfigurationChanges(){
		System.out.print("[ProjectStarter] Configuration changed - updating...") ;
		updateTiming() ;
		sacc.setNumberOfRepeats(conf.getValueInt("signalRepeats"));
		System.out.println(".. [OK] ") ;
		hasConfigurationChanged = false ;
	}

	public void makeSynchronization(){
		System.out.println("[ProjectStarter] Making synchronization... ") ;
		synchronized(hddCollector){
			hddCollector.notify() ;
		}
		timeManager.reset();
		sp.reset() ;
		sacc.reset() ;
		((MatrixResponsesCanvas) cp.getCanvasMatrixResponses()).reset() ;
		displayUpdater.reset() ;
	}

	private volatile boolean run = true ;	

	public void setRunState(boolean val){
		run = val ;
	}

	public void run(){
		while(run){
			try {
				synchronized(this){
					this.wait() ;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			if(hasConfigurationChanged)
				updateConfigurationChanges();

			makeSynchronization();

			try {
				synchronized(this){
					this.wait(getConf().getValueInt("waitBetweenTime"));
				}
				synchronized(timeManager){
					timeManager.notify() ;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("jEEG starter/sychronizer stopped.");
	}

	public void stopRunning() {
		timeManager.setRunState(false) ;
		sp.resetGenerator();
	}

	public static DisplayUpdater getDisplayUpdater() {
		return displayUpdater ;
	}

	public static DriverExecutor getDriver() {
		return drv ;
	}

	public Shell getShell() {
		return null;
	}

}


