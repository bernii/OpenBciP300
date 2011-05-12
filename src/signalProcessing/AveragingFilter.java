package signalProcessing;

import general.EegData;
import javolution.util.FastTable;

import astart.ProjectStarter;

import configuration.ElementChannelsCalibration;
import configuration.ElementChannelsWeights;
import configuration.IConfigurationListener;

public class AveragingFilter implements ISignalFilter, IConfigurationListener {


	private static boolean applyFilterToInput = false ;
	private static int[] nonZeroWeights;

	int [] output = null ;
	/**
	 * Convert from many EEG channels to one array (average of values)
	 * @param eegDataBuffer
	 * @return
	 */
	public int[] process(FastTable<EegData> eegDataBuffer) {
		double tmp = 0 ;
		if(output==null||output.length<eegDataBuffer.size())
			output = new int[eegDataBuffer.size()] ;
		Double[][] mychannellsWeights = channellsWeights;
		// SPATIAL FILTER ALREADY APPLIED AT THE BEGINNING
		if(!applyFilterToInput){
			for(int i=eegDataBuffer.size();--i>=0;){
				int chanNumber = 0 ;
				int actualChan = 0 ;
				int b = 0 ;
				tmp = 0 ;
				for(int val : eegDataBuffer.get(i).getValues()){
					if(nonZeroWeights[actualChan++]>0){
						tmp += val*mychannellsWeights[chanNumber][b++] ;
						chanNumber++ ;
					}
				}
				output[i] = (int) (tmp/chanNumber) ;
			}
		}else{
			for(int i=eegDataBuffer.size();--i>=0;){
				int chanNumber = 0 ;
				tmp = 0 ;
				int actualChan = 0 ;
				for(int val : eegDataBuffer.get(i).getValues()){
					if(nonZeroWeights[actualChan++]>0){
						tmp += val ;
						chanNumber++ ;
					}
				}
				output[i] = (int) (tmp/chanNumber) ;
			}
		}
		return output ;
	}
	
	/**
	 * Used in filtering at the beginning
	 */
	public void process(EegData data) {
		if(applyFilterToInput){
			int out ;
			for(int chan=0; chan < data.getChannelsNumber() ; chan++){
				out=0 ;
				if(nonZeroWeights[chan]!=0){
					for(int chan2=0; chan2 < data.getChannelsNumber() ; chan2++)
						out += ((data.getValues()[chan2]+channellsOffsets[chan2])*channellsGains[chan2])*channellsWeights[chan][chan2]; 
						data.setValue(chan,out/nonZeroWeights[chan]) ;
				}
				else
					data.setValue(chan,0) ;
				
			}
		}
	}
	
	public int[] process(int[] eegDataBuffer) {
		return eegDataBuffer ;
	}

	private static Classifier classifier = ProjectStarter.getClassifier();
	private  static Double[][] channellsWeights ;
	private static double[] channellsGains;
	private static double[] channellsOffsets;
	
	/**
	 * Average data from many repeats to one averaged array
	 * @param js
	 * @param actualRepeat
	 * @return
	 */
	public int[] processInt(int[][] js, int actualRepeat) {
		int[] output = new int[js[0].length] ;
		if(actualRepeat==0){
			
			for(int a=0;a<js[0].length;a++){
				if(js[0]==null)
					output[a] = 0 ;
				else
					output[a] = js[0][a] ;
			}
		}else{
			for(int i=0;i<=actualRepeat;i++){
				System.out.print("[AvragingFilter] buffer["+i+"] len = "+js[i].length);
				for(int a=0;a<output.length;a++){
					if(a>=js[i].length-1) //different amount of samples ..
						output[a] += js[i][js[i].length-1];
					else{
						output[a] += js[i][a] ;
					}
				}
			}
			for(int i=0;i<output.length;i++){
				output[i] /= (actualRepeat+1) ; 
				classifier.updateSample(i,output[i]);
			}
			System.out.println(" ");
		}
		
		return output ;
	}

	public void configurationChanged() {
		channellsWeights = ((ElementChannelsWeights)ProjectStarter.getConf().getElement("Channels weights")).getMatrixValues() ;
		channellsGains = ((ElementChannelsCalibration)ProjectStarter.getConf().getElement("Channels calibration")).getGains() ;
		channellsOffsets = ((ElementChannelsCalibration)ProjectStarter.getConf().getElement("Channels calibration")).getOffsets() ;
		nonZeroWeights = new int[channellsWeights.length] ;
		for(int chan=0;chan<channellsWeights.length;chan++){
			int nonZero = 0 ;
			for(int chan2=0;chan2<channellsWeights[chan].length;chan2++)
				if(channellsWeights[chan][chan2]!=0)
					nonZero++ ;
			nonZeroWeights[chan] = nonZero ;
		}
		applyFilterToInput = ProjectStarter.getConf().getValueInt("applySpatialFilterToInput")==1?true:false; 
		System.out.println("[AvragingFilter] updating chann coeffs ["+channellsWeights.length+"]");
	}
	
	public String getFilterName() {
		return "Avraging filter" ;
	}
}
