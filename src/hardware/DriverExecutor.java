package hardware;


import general.EegData;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

import javolution.util.FastList;
import javolution.util.FastTable;

import org.apache.log4j.Logger;

import signalProcessing.ISignalFilter;
import astart.ProjectStarter;
import configuration.Configuration;
import configuration.IConfigurationListener;

/**
 * Gets data from hardware or from random number generator
 * @author berni
 *
 */
public class DriverExecutor implements Runnable, IConfigurationListener {

	FastTable<IDriverListener> driverListeners = new FastTable<IDriverListener>() ;
	FastTable<EegData> dataBuffer = null ;
	private static EegData data ;
	static Logger logger = Logger.getLogger(DriverExecutor.class);
	IProtocol packetSupply ; 
	private SerialPort serialPort ;
	private volatile float samplesInterspace = 1 ;
	InputStream in = null ;
	private boolean connectionMade = false ;
	private volatile int actualPacketNumber;
	int sinVal;
	int steper = 230;
	private int bufferPointer;
	FastTable<ISignalFilter> signalFilters = new FastTable<ISignalFilter>() ;
	private String protocolType;
	private int sourceType;
	private volatile boolean generatePositiveSignal = false ;

	public void addDriverListener(IDriverListener drvListener) {
		driverListeners.add(drvListener);
	}

	private volatile boolean runDriver = true ;

	public void setRunState(boolean state){
		runDriver = state ;
	}
	public void run(){

		dataBuffer = new FastTable<EegData>(10000);
		dataBuffer.setSize(10000);
		actualPacketNumber = 0 ;
		Random rnd = new Random() ;
		byte[] byteArr = new byte[]{1,2,3} ;
		int[] generatedVals = null ;

		if(ProjectStarter.getConf()!=null){
			protocolType = ProjectStarter.getConf().getValueString("protocolType") ;
			sourceType = ProjectStarter.getConf().getValueInt("signalSource") ; 
		}
		while(runDriver){
			try {
				LockSupport.parkNanos((long)(samplesInterspace*1000000L));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if(packetSupply==null){
				System.out.println("[DriverExecutor] Setting packet supply");
				packetSupply = ProtocolFactory.get(protocolType) ;
				packetSupply.reset();
				int[] vals = new int[packetSupply.getNumberOfChannels()];
				for(int i =0;i<10000;i++){
					EegData packet =  new EegData(0, packetSupply.getNumberOfChannels(),vals) ;
					dataBuffer.set(i,packet) ;
				}
			}
			switch(sourceType){
			case Configuration.SOURCE_RANDOM:
				if(packetSupply instanceof BraintronicsProtocol){		
					if((sinVal+steper)>=(packetSupply.getExtremeValues()[1]-3024)||(sinVal+steper)<=(packetSupply.getExtremeValues()[0]+3024))
						steper *= -1 ;
					sinVal +=steper ;
					if(generatedVals==null) 
						generatedVals = new int[64];
					for(int h = 0  ;h <generatedVals.length;h++)
						if(h==0){
							generatedVals[h] = sinVal ;
						}else if(!generatePositiveSignal)
							generatedVals[h] = rnd.nextInt(3024)-1512 ;
						else if(rnd.nextInt(100)>10){
							generatedVals[h] = rnd.nextInt(3024)-1512+(int) ((rnd.nextGaussian()+150)*15) ;
							if(generatedVals[h]>6024)
								generatedVals[h] = 6024 ;
						}else
							generatedVals[h] = rnd.nextInt(3024)-1512 ;

					byteArr = packetSupply.generatePacket(actualPacketNumber,generatedVals);
				}else{
					int[] vals = new int[6];
					for(int h = 0  ;h <vals.length;h++)
						if(!generatePositiveSignal)
							vals[h] = rnd.nextInt(1024)-512 ;
						else{
							vals[h] = rnd.nextInt(1024)-512+(int) ((rnd.nextGaussian()+50)*12) ;
							if(vals[h]>1024)
								vals[h] = 1024 ;
						}
					byteArr = packetSupply.generatePacket(actualPacketNumber, vals);
				}
				try {;
				for(byte oneByte: byteArr){
					if(packetSupply.isValidPacket(oneByte)==1){
						data = dataBuffer.get(bufferPointer++);
						packetSupply.getDataPortion(data) ;
						if(bufferPointer>=10000)
							bufferPointer = 0 ;
						sendToListeners() ;
					}
				}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				break;
			case Configuration.SOURCE_COM:
				if(in==null&&!connectionMade&&packetSupply!=null){


					String com = ProjectStarter.getConf().getValueString("ComPortIdentifier") ;
					System.out.println("[DriverExecutor] - creating connection to COM port "+com);
					/************ JD2XX Start ************************/						
					//						jd = new JD2XX();
					//						Object[] devs;
					//						try {
					//							devs = jd.listDevicesBySerialNumber();
					//							for (int i=0; i<devs.length; ++i)
					//								  System.out.println(devs[i]);
					//							
					//							devs = jd.listDevicesByDescription();
					//							for (int i=0; i<devs.length; ++i)
					//							  System.out.println(devs[i]);
					//
					//							// List devices by port location.
					//
					//							devs = jd.listDevicesByLocation();
					//							for (int i=0; i<devs.length; ++i)
					//							  System.out.println(
					//							    Integer.toHexString((Integer)devs[i])
					//							  );
					//
					//							jd.open(0);
					//
					//							jd.setBaudRate(JD2XX.BAUD_921600);
					//							jd.setDataCharacteristics(
					//							  8, JD2XX.STOP_BITS_1, JD2XX.PARITY_NONE
					//							);
					//							jd.setFlowControl(
					//							  JD2XX.FLOW_NONE, 0, 0
					//							);
					//							jd.setTimeouts(1000, 1000);
					//
					//							connectionMade = packetSupply.initConnection(new JD2XXInputStream(jd),new JD2XXOutputStream(jd));
					//							
					//							try {
					//								jd.addEventListener(
					//									new JD2XXEventListener() {
					//										private volatile AtomicBoolean isProcessing = new AtomicBoolean(false);
					////										
					//										public void jd2xxEvent(JD2XXEvent ev) {
					//											JD2XX jo = (JD2XX)ev.getSource();
					//											int et = ev.getEventType();
					//											try {
					//												if ((et & JD2XX.EVENT_RXCHAR) != 0) {
					////													int r = jo.getQueueStatus();
					////													System.out.println("RX event: " + new String(jo.read(r)));
					//													
					//													while(in.available()>0/*packetSupply.getPacketSize()*/&&isProcessing.compareAndSet(false,true)){
					////														System.out.println("[DriverExecutor] "+isProcessing.get()+" inn "+in.available() +" i = "+i++);
					//														if(packetSupply.isValidPacket(in)){
					//															data = packetSupply.getDataPortion() ;
					//															sendToListeners() ;
					//														}
					//														isProcessing.set(false);
					////														System.out.println("[DriverExecutor] "+isProcessing.get()+" TO FALSE !") ;
					//													}
					//												}
					////												else if ((et & JD2XX.EVENT_MODEM_STATUS) != 0) {
					////													System.out.println("Modem status event");
					////												}
					//											}
					//											catch (IOException e) { }
					//										}
					//									}
					//								);
					//							}
					//							catch (TooManyListenersException e) { }
					//							jd.notifyOnEvent(JD2XX.EVENT_RXCHAR | JD2XX.EVENT_MODEM_STATUS, true);
					//							
					//							in = new JD2XXInputStream(jd);
					////							JD2XXOutputStream outs = new JD2XXOutputStream(jd);
					//
					//							
					//						} catch (IOException e1) {
					//							e1.printStackTrace();
					//						}
					//						
					/************ JD2XX END ************************/
					/************ SerialPort START ************************/	
					logger.info("CommPort communication start.");
					CommPortIdentifier portIdentifier;
					try {
						portIdentifier = CommPortIdentifier.getPortIdentifier(com);
						serialPort = (SerialPort) portIdentifier.open("RS232Example", 16000);

						serialPort.setSerialPortParams(
								128000, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
						serialPort.setInputBufferSize(32*packetSupply.getPacketSize()) ;
						connectionMade = packetSupply.initConnection(serialPort.getInputStream(),serialPort.getOutputStream());
						serialPort.setInputBufferSize(32*packetSupply.getPacketSize()) ;
						in =  new BufferedInputStream(serialPort.getInputStream(),16000) ;
						serialPort.setInputBufferSize(32*packetSupply.getPacketSize()) ;

						/**
						 * SERIAL EVENT START
						 */
						/*
							try {
								serialPort.addEventListener(new SerialPortEventListener(){
									int valid = 0 ;
									volatile long lastTime = -1 ; ;
									volatile double time  ;
									volatile int itime = 0 ;
									volatile double avail = 0; 
									public void serialEvent(SerialPortEvent event){

										 switch(event.getEventType()) {
								            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
//							                outputBufferEmpty(event);
								                break;
								            case SerialPortEvent.OE:
								            	System.out.println("[DriverExecutor] !!!!!!! OverrunError");
//								                outputBufferEmpty(event);
									                break;
								            case SerialPortEvent.DATA_AVAILABLE:
								            	try {

								            		if( in.available() >= packetSupply.getPacketSize()) {
								            			avail+= in.available();
									            		if(lastTime==-1){
									            			lastTime = System.currentTimeMillis() ;
									            		}else{
										            		time += (System.currentTimeMillis() - lastTime) ;
										            		lastTime = System.currentTimeMillis() ;


									            			itime++;
									            			if(itime>100){
									            				System.out.println("avg time = "+time/101+" avail "+avail/101+"  size "+packetSupply.getPacketSize());
									            				itime = 0 ;
									            				time = 0 ;
									            				avail = 0 ;
									            			}
									            		}

//									                	if(in.available()<packetSupply.getPacketSize())
//									                		continue ;
								            			while(in.available()>0) {
										                	valid = packetSupply.isValidPacket((byte) in.read()) ;
										                	if(valid==1){
										                		data = dataBuffer.get(bufferPointer++);
										                		packetSupply.getDataPortion(data) ;
										                		sendToListeners() ;
										                		if(bufferPointer>=10000)
										                			bufferPointer = 0 ;

															}else if(valid==-1){
																// something went wrong - send zeros instead
																in.skip(in.available());
																System.out.println("[DriverExecutor] sending zeros as sample");
																data = dataBuffer.get(bufferPointer++);
										                		for(int i=0;i<data.getChannelsNumber();i++)
										                			data.getValues()[i] = 0 ;
										                		sendToListeners() ;
										                		if(bufferPointer>=10000)
										                			bufferPointer = 0 ;
															}
														}
//									                    protocol.onReceive((byte) b);
									                }
												} catch (Exception e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
								                break;
								            case SerialPortEvent.BI:
//							                breakInterrupt(event);
								                break;
								        }

									}});
							} catch (TooManyListenersException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							serialPort.notifyOnDataAvailable(true);
						 */
						/**
						 * SERIAL EVENT END
						 */
						/**
						 * THREAD LOOP START
						 */

						serialPort.notifyOnDataAvailable(false);
						new Thread(){
							int valid ;
							public void run(){
								try {
									int b;
									while(in!=null) {

										// if stream is not bound in.read() method returns -1
										while((b = in.read()) != -1) {
											valid = packetSupply.isValidPacket((byte)b) ;
											if(valid==1){
												data = dataBuffer.get(bufferPointer++);
												packetSupply.getDataPortion(data) ;
												sendToListeners() ;
												if(bufferPointer>=10000)
													bufferPointer = 0 ;

											}else if(valid==-1){
												// something went wrong - send zeros instead
												in.skip(in.available());
												System.out.println("[DriverExecutor] sending zeros as sample");
												data = dataBuffer.get(bufferPointer++);
												for(int i=0;i<data.getChannelsNumber();i++)
													data.getValues()[i] = 0 ;
												sendToListeners() ;
												if(bufferPointer>=10000)
													bufferPointer = 0 ;
											}
										}
										// wait 10ms when stream is broken and check again
										sleep(10);
										System.out.println("Stream CLOSED w8ting 10ms");
									}
								} catch (IOException e) {
									e.printStackTrace();
								} catch (InterruptedException e) {
									e.printStackTrace();
								} 
							}
						}.start() ;


						/**
						 * THREAD LOOP END
						 */

					} catch (NoSuchPortException e) {
						e.printStackTrace();
					} catch (PortInUseException e) {
						connectionMade = true ; // do not try to connect again ;]
						e.printStackTrace();
					} catch (UnsupportedCommOperationException e) {
						e.printStackTrace();
					} catch (ProtocolException e) {
						System.out.println("[DriverExectuor] Protocol exception !");
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					/************ SerialPort END ************************/	
				}
				break ;
			default:
				in = null ;
			break ;
			}

			actualPacketNumber++ ;
		}
		if(serialPort!=null){
			System.out.println("[DriverExecutor] - shutting down drivers");
			try {
				packetSupply.endConnection(in,serialPort.getOutputStream());
				in.close();
				serialPort.close();
				in = null ;				
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public void addSignalFilter(ISignalFilter filter){
		signalFilters.add(filter);
	}

	private void sendToListeners(){	
		// APPLY SPATIAL FILTERS IF NEEDED
		synchronized(signalFilters){
			for(ISignalFilter filter : signalFilters){
				filter.process(data) ;
			}
		}
		for (IDriverListener listener : driverListeners) {
			listener.dataArrived(data) ;
		}
	}

	public IProtocol getPacketSupply() {
		return packetSupply;
	}

	public void setPacketSupply(IProtocol packetSupply) {
		this.packetSupply = packetSupply;
	}

	public void removeSignalFilter(ISignalFilter filter) {
		synchronized(signalFilters){
			for(int i =0 ; i<signalFilters.size();i++){
				if(signalFilters.get(i)==filter)
					signalFilters.remove(i);
			}
		}
	}

	public void removeSignalFilter(String name) {
		for(int i =0 ; i<signalFilters.size();i++){
			if(signalFilters.get(i).getFilterName()==name)
				signalFilters.remove(i);
		}
	}

	public boolean hasFilter(String name) {
		for(int i =0 ; i<signalFilters.size();i++){
			if(signalFilters.get(i).getFilterName()==name)
				return true ;
		}
		return false;
	}

	public void configurationChanged() {
		// diagnose if something about packetSupply has changed 
		System.out.println("[DriverExecutor:configurationChanged] proto:"+protocolType+" source "+sourceType);
		if(!protocolType.equalsIgnoreCase(ProjectStarter.getConf().getValueString("protocolType"))||
				sourceType!=ProjectStarter.getConf().getValueInt("signalSource")){
			packetSupply = null ;
			protocolType = ProjectStarter.getConf().getValueString("protocolType") ;
			sourceType = ProjectStarter.getConf().getValueInt("signalSource") ;
			System.out.println("[DriverExecutor:configurationChanged] switching protocol to "+protocolType);

			actualPacketNumber = 0 ;
			connectionMade = false ; 
		}
		samplesInterspace = 1000f/ProjectStarter.getConf().getValueInt("samplingFrequency")	 ;	
		System.out.println("[DriverExecutor:configurationChanged] freq "+ProjectStarter.getConf().getValueInt("samplingFrequency")+" samplesInterspace "+samplesInterspace);
	}
	public long getSamplesInterspace(){
		return (long)(samplesInterspace*1000) ;
	}

	public void setSignalPositive(boolean b) {
		generatePositiveSignal  = b ;
	}

	public static void main(String[] args) {
		System.out.println("Starting GC test");
		DriverExecutor de = new DriverExecutor();
		de.protocolType = "Braintronics" ;
		de.sourceType = Configuration.SOURCE_RANDOM ;
		new Thread(de).start();
		new Thread(){
			Random rnd = new Random() ;
			public void run(){
				int o = 0 ;
				FastTable<EegData> ints = new FastTable<EegData>(10000);
				FastList<int[]> int2 = new FastList<int[]>(10000);
				int[] vals = new int[64];
				for(int i=0;i<10000;i++){
					ints.add(new EegData(0,64,new int[64]));
					int2.add(new int[]{1,2,34,5,4});
				}
				byte[] cos = null ;

				for(int i=0;i<vals.length;i++)
					vals[i] = rnd.nextInt();
				IProtocol ps = new BraintronicsProtocol();
				int num =0 ;
				int[] myint = new int[]{1,2,3};
				EegData tempvar = null ;
				while(true){
					int2.removeFirst();
					int2.add(myint);
					cos = ps.generatePacket(num++,vals);
					for(byte oneByte: cos){
						try {
							if(ps.isValidPacket(oneByte)==1){
								tempvar = ints.get(o) ;
								try {
									ps.getDataPortion(tempvar) ;
								} catch (ProtocolException e) {
									e.printStackTrace();
								}							
							}
						} catch (ProtocolException e) {
							e.printStackTrace();
						}
					}
					if(o>=ints.size())
						o=0 ;;
						ints.get(o).setValue(1, rnd.nextInt());
						for(int i=0;i<vals.length;i++)
							vals[i] = rnd.nextInt();
				}
			}
		}.start();
		int i = 0;
		while(i<10){
			System.out.println(" i "+i);
			i++;	
		}
	}
}
