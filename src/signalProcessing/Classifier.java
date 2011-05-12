package signalProcessing;

import javolution.util.FastTable;
import configuration.ClassificatorConfElem;
import configuration.Configuration;
import configuration.ElementClassificatorConf;
import configuration.IConfigurationListener;
import general.ITimeManagerListener;
import astart.ProjectStarter;

/**
 * Classifies each recorder signal according to selected method
 * @author berni
 *
 */
public class Classifier implements IConfigurationListener {

	private int[] extremeValues ;
	private int actualSignalNumber;
	private int classificationType ;
	private int actualWeight;
	private int[] sampleNumbers;
	private boolean findMaximum; 
	
	int tempValue = 0 ;
	
	public void initialize(int numberOfSignals){
		extremeValues = new int[numberOfSignals] ;
		update();
	}
		
	public void update(){
		tempValue = 0 ;
		findMaximum = ProjectStarter.getConf().getValueString("classificationAvrageType").compareTo("max")==0?true:false;
		float oneSampleTimeLen = 1000F/ProjectStarter.getConf().getValueInt("samplingFrequency");
		
		int beginTime = ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true).getFirst().span ;
		int endTime = ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true).getLast().span ;
		
		int responseTimeLen = endTime - beginTime;
		float tempSamplesPerSignal = (responseTimeLen / oneSampleTimeLen) ;
		int samplesPerSignal = (int) tempSamplesPerSignal ;
		if(tempSamplesPerSignal>Math.floor(tempSamplesPerSignal))
			samplesPerSignal++ ;
		
		actualSignalNumber = 0 ;
		
		classificationType = ProjectStarter.getConf().getValueInt("classificationType") ;
		if(classificationType==Configuration.CLASSIFICATION_WEIGHTS){
			actualWeight = 0 ;
			// convert time to sample numbers
			FastTable<ClassificatorConfElem> params = ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true);
			sampleNumbers = new int[params.size()];
			int i = 0 ;
			int sampleNumber = 0 ;
			int timeZero = 0 ;
			for(ClassificatorConfElem elem : params){
				if(i==0){
					timeZero = elem.span;
					sampleNumbers[i++] = 0 ;
					continue ;
				}else if(i==sampleNumbers.length-1){
					sampleNumbers[i++] = samplesPerSignal-1 ;
					continue ;
				}
				while(i<sampleNumbers.length){
					if(elem.span<(sampleNumber*oneSampleTimeLen + timeZero)){
						sampleNumbers[i++] = sampleNumber ;
						break ;
					}
					sampleNumber++;
				}
			}
			System.out.println("[Classifier] Samples numbers:");
			for(int sampleNum: sampleNumbers)
				System.out.print(" "+sampleNum);
			System.out.println(" ");	
		}
		
		tempSumOfWeights = 0 ;
	}
	private int tempSumOfWeights ;
	private int numberOfSamples;
	
	public void updateSample(int sampleNumber, int sampleValue) {
		switch(classificationType){
		case Configuration.CLASSIFICATION_WINDOW:
			if(findMaximum){
				if(sampleValue>extremeValues[actualSignalNumber])
					extremeValues[actualSignalNumber] = sampleValue ;
			}else{
				if(sampleValue<extremeValues[actualSignalNumber])
					extremeValues[actualSignalNumber] = sampleValue ;
			}
			break;

		case Configuration.CLASSIFICATION_WEIGHTS:
			
			if(sampleNumbers[actualWeight]==sampleNumber){
				actualWeight ++ ;
				tempSumOfWeights+= sampleValue ;
			}
			if(actualWeight==sampleNumbers.length){
				extremeValues[actualSignalNumber] = tempSumOfWeights/actualWeight ;
				tempSumOfWeights = 0 ;
				actualWeight = 0 ;
			}
			break;

		case Configuration.CLASSIFICATION_AVG_IN_WINDOW:
			tempValue += sampleValue ;
			if(sampleNumber==numberOfSamples-1){
				extremeValues[actualSignalNumber] = tempValue / sampleNumber ;
			}
			break;
		}
		
	}

	public int[] getExtremeVal() {
		return extremeValues;
	}

	public void setSignalNumber(int actualSignal) {
		this.actualSignalNumber = actualSignal ;
		tempValue = 0 ;
	}
	public void configurationChanged() {
		update();
	}

	public void setNumberOfSamples(int numberOfSamples) {
		this.numberOfSamples = numberOfSamples ;
	}

}
