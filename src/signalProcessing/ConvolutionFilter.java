package signalProcessing;

import general.EegData;
import javolution.util.FastList;
import javolution.util.FastTable;
/**
 * Convolution digital filter implementation
 * @author berni
 *
 */
public class ConvolutionFilter implements ISignalFilter {
	
	private double[] coefficientsA;
	private double[] coefficientsB;
	private String filterName;

	public ConvolutionFilter(double [] coeffs, String filterName){
	    this.coefficientsA = coeffs;
	    this.filterName = filterName ;
	}
	
	public ConvolutionFilter(double[] valuesa, double[] valuesb, String name) {
		this.coefficientsA = valuesa;
		this.coefficientsB = valuesb;
	    this.filterName = name ;
	}

	FastList<int[]> dataBufferX = new FastList<int[]>() ;
	FastList<double[]> dataBufferY = new FastList<double[]>() ;

	public int[] process(int[] eegDataBuffer) {
		return null;
	}

	double[] output =  null ;
	short bufferPointer = 0 ;
	
	/**
	 * Reusing buffers to avoid garbage collecting
	 */
	public void process(EegData data) {
		if(dataBufferX.size()==0&&dataBufferY.size()==0){
			System.out.println("[ConvolutionFilter] Init");
			for(int i=0;i<coefficientsB.length;i++){
				dataBufferX.add(new int[data.getValues().length]);
				dataBufferY.add(new double[data.getValues().length]);
			}
			output = new double[data.getChannelsNumber()];
		}
		for(int i=0;i<output.length;i++){
			output[i] = 0 ;
		}

		// add to buffer
		if(coefficientsB.length>0){
			int[] buffer = dataBufferX.get(bufferPointer); 
			int[] inputVals = data.getValues() ;
			for(int i=0;i<buffer.length;i++){
				buffer[i] = inputVals[i];
			}
			int b = coefficientsB.length-1 ;
			int pointer = bufferPointer+1 ;
			while(b>=0){
				if(pointer>=dataBufferX.size())
					pointer = 0;
				int[] chanData  = dataBufferX.get(pointer++);
				for(int i = 0 ; i< chanData.length; i++){
					output[i] += (chanData[i]*coefficientsB[b]);
				}
				b-- ;
			}
		}
		if(coefficientsA.length>0){
			// convolve data
			int a = coefficientsA.length-1 ;
			int pointer = bufferPointer+1 ;
			while(a>=0){
				if(pointer>=dataBufferY.size())
					pointer = 0;
				double[] chanData  = dataBufferY.get(pointer++);
				if(a==0){
					for(int i = 0 ; i< chanData.length; i++){
						output[i] /= coefficientsA[0];
					}
					break ;
				}
					
				for(int i = 0 ; i< chanData.length; i++){
					output[i] -= (chanData[i]*coefficientsA[a]);
				}
				
				a-- ;
			}
			double[] buffer = dataBufferY.get(bufferPointer); 
			for(int i=0;i<buffer.length;i++){
				buffer[i] = output[i];
			}
		}
		
		data.setValues(output) ;
		bufferPointer++;
		if(bufferPointer>=dataBufferX.size())
			bufferPointer = 0 ;
	}

	public String getFilterName() {
		return filterName ;
	}
	
	public static void main(String[] args) {
		double[] b = new double[]{0.8159,-4.0794,8.1588,-8.1588,4.0794,-0.8159} ;
		double[] a = new double[]{1.0000,-4.5934,8.4551,-7.7949,3.5989,-0.6657} ;
		double[] inputVals = new double[]{-7.592675e+001,-5.951698e+001,8.123996e+001,6.954083e+000,-1.833687e+002,1.827363e+002,6.540751e+001,-1.544769e+002,-3.751090e+001,2.076883e+001,-7.656145e+001,-1.063552e+001,3.387694e+001,1.033461e+002,-1.404822e+002,-1.030570e+002,-6.433720e+001,1.707866e+001,1.344839e+002,1.936273e+002,7.413357e+001,8.119796e+001,-1.428084e+001,-9.985849e+000,-8.001315e+001,4.932490e+001,1.237574e+002,1.295951e+002,-2.781961e+001,2.171272e+001,6.307280e+001,-5.485486e+001,2.296316e+001,3.553112e+001,5.212841e+001,-6.159706e+001,1.345803e+002,9.749224e+001,-2.377934e+002,-1.092319e+002,-3.257096e+001,-2.012228e+002,1.567660e+002,2.333372e+001,6.464197e+001,-1.129412e+002,1.970383e+001,1.696870e+002,7.260336e+001,7.925258e+001,6.033566e+001,-5.840540e+000,-1.108666e+002,2.144229e+002,-1.352821e+002,4.570213e+001,3.911748e+001,2.073013e+002,-3.233184e+001,1.468132e+002,-5.023987e+001,2.095933e+001,7.548000e+001,-9.481891e+001,6.131567e+001,1.760503e+002,8.876229e+000,2.595570e+002,-6.754698e+001,2.786804e+002,-1.682683e+000,2.716509e+001,-9.141023e+001,-1.951371e+002,-3.174182e+001,5.883334e+001,8.289964e+001,-1.674851e+002,-1.922293e+002,-4.366621e+001,4.497413e+000,2.416609e+002,-3.098918e+001,1.875830e+001,9.476989e+001,-5.257028e+001,-1.115605e+002,-1.592320e+002,1.174844e+002,4.851439e+001,1.645480e+002,-4.542332e+001,1.008768e+002,2.049403e+002,6.020201e+001,1.785960e+000,-1.610426e+002,1.238752e+002,6.835869e+001,-7.807158e+001
		};
		System.out.println("IN");
		short[] inputValsShort = new short[inputVals.length];
		for(int i = 0 ; i <inputValsShort.length ;i++){
			inputValsShort[i] = (short) inputVals[i];
			System.out.print(inputValsShort[i]+",");
		}
		System.out.println("\nOUT");
		ConvolutionFilter filter = new ConvolutionFilter(a,b,"My filter");
		for(int i=0;i<inputVals.length;i++ ){
			EegData portion = new EegData(i,1,new int[]{inputValsShort[i]});
			filter.process(portion);
			System.out.println("y("+i+") = "+portion.getValues()[0]);
		}
		
		System.out.println("REFERENCE TEST");
		FastTable<double[]> dataBufferY = new FastTable<double[]>();
		for(int i=0;i<6;i++)
			dataBufferY.add(new double[]{1,2,3,4,5});
		double[] buffer = dataBufferY.get(2); 
		for(int i=0;i<buffer.length;i++){
			buffer[i] = 3;
		}
		for(double[] dataBuffer: dataBufferY){
			for(double num:dataBuffer){
				System.out.print(" "+num);
			}
			System.out.println(" ");
		}
	}
}
