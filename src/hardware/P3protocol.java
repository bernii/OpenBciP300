package hardware;

import general.EegData;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.net.ProtocolException;
import java.util.BitSet;
import java.util.Random;

public class P3protocol implements IProtocol {
	
	public static final int MAX_PACKET_SIZE = 24 ;
	
	static int i = 0;
	static int j = 0;
	static int packetCounter = 0;
	static int auxChan = 0;
	static int[] samples = new int[MAX_PACKET_SIZE];
	static boolean needsHeader = true ;
	static boolean needsAux;
	public static EegData data ;
	
	public int isValidPacket(byte oneByte1){
		short oneByte = (short)oneByte1 ;
		int samplesNumber ;
		if (needsHeader) {
			if ((oneByte & 0x80)!=0) reset() ;
			packetCounter = oneByte >> 1;
			auxChan = oneByte & 1;
			needsHeader = false;
			needsAux = true ;
			return 0;
		}
		if (needsAux) {
			if ((oneByte & 0x80)!=0) reset() ;
			auxChan += (int) oneByte * 2;
			needsAux = false ;
			return 0;
		}
		if (2*i < MAX_PACKET_SIZE) {
	       switch (j) {
	           case 0: samples[2*i] = (short) oneByte; break;
	           case 1: samples[2*i+1] = (short) oneByte; break;
	           case 2:
	               samples[2*i] += (short)   (oneByte & 0x70) << 3;
	               samples[2*i+1] += (short) (oneByte & 0x07) << 7;
	               i += 1;
	               break;
	       }
		}
		j = (j+1) % 3;
		if ((oneByte & 0x80)!=0) {
			if (j == 0) {
				int k;
				samplesNumber = i * 2;
				for (k = 0; k < samplesNumber; ++k)
					if (samples[k] > 1023)
						reset();
				i = 0;
				j = 0;
				needsHeader = true;
				handleSamples(packetCounter, samplesNumber, samples);
				return 1;
			}
			else {
			}
			reset();
		}
		return 0;
			
		
	}
	
	public void reset(){
		i = 0;
		j = 0;
		needsHeader = true;
	}
	
	private void handleSamples(int packetCounter, int chan, int []vals)
	{
		int i;
		data = new EegData(packetCounter, chan,vals) ;
	}
		
	public byte[] generatePacket(int packetNumber, int[] randVals){	
		byte[] byteArr = new byte[11] ; 
		byteArr[0] = (byte)( 0 | (packetNumber << 1 & 0x7E)) ;
		byteArr[1] = (byte) 0 ;
		int a = 2 ;
		for(int i = 0 ; i<randVals.length; i+=2){
			byteArr[a++] = (byte) (( (0) ) | (randVals[i] & 0x7F)) ;
			byteArr[a++] = (byte) (( (0) ) | (randVals[i+1]  & 0x7F)) ;
			if(i==randVals.length-2)
				byteArr[a++] = (byte) ((0x80)|(byte) ( randVals[i] >> 7 & 0x7) | ( randVals[i+1] >> 3 & 0x70 ))  ; 
			else
				byteArr[a++] = (byte) ((byte) ( randVals[i] >> 7 & 0x7) | ( randVals[i+1] >> 3 & 0x70 ))  ; 
		}
//		byteArr[2] = (byte) (( (0) ) | (randVals[0] & 0x7F)) ;
//		byteArr[3] = (byte) (( (0) ) | (randVals[0]  & 0x7F)) ;
//		byteArr[4] = (byte) ((byte) ( randVals[0] >> 7 & 0x7) | ( randVals[0] >> 3 & 0x70 ))  ; 
//		
//		byteArr[5] = (byte) (( (0) ) | (randVals[1] & 0x7F)) ;
//		byteArr[6] = (byte) (( (0) ) | (randVals[1]  & 0x7F)) ;
//		byteArr[7] = (byte) ((byte) ( randVals[1] >> 7 & 0x7) | ( randVals[1] >> 3 & 0x70 ))  ; 
//		
//		byteArr[8] = (byte) (( (0) ) | (randVals[2] & 0x7F)) ;
//		byteArr[9] = (byte) (( (0) ) | (randVals[2]  & 0x7F)) ;
//		byteArr[10] = (byte) ((0x80)|(byte) ( randVals[2] >> 7 & 0x7) | ( randVals[2] >> 3 & 0x70 ))  ; 
//		
		return byteArr ;
	}
	public static void main(String[] args) {
		BitsUtil bu = new BitsUtil() ;
		P3protocol p3 = new P3protocol() ;
		// generate random data in P3 format and try to parse them
		/**
		 * 0ppppppx packet header
			0xxxxxxx
			0aaaaaaa channel 0 LSB
			0bbbbbbb channel 1 LSB
			0aaa-bbb channel 0 and 1 MSB
			0ccccccc channel 2 LSB
			0ddddddd channel 3 LSB
			0ccc-ddd channel 2 and 3 MSB
			0eeeeeee channel 4 LSB
			0fffffff channel 5 LSB
			1eee-fff channel 4 and 5 MSB
			1 and 0 = sync bits.
			p = 6-bit packet counter
			x = auxilary channel byte
			a-f = 10-bit samples from
			ADC channels 0 - 5
			- = unused, must be zero
		 */
		Random rnd = new Random() ;
		// 10 1001 0001
		int[] randVals = new int[]{657,333,161} ;
		
		for(byte packetNum =0 ; packetNum<3 ; packetNum++){
			randVals = new int[]{rnd.nextInt(1024),rnd.nextInt(1024),rnd.nextInt(1024)} ;
			byte[] byteArr = p3.generatePacket(packetNum, randVals) ;		
			bu.printByteArray(byteArr);
			
			System.out.println("\nrand vals ["+randVals[0]+","+randVals[1]+","+randVals[2]+"" +
					"] \nCatching ...");
			for(int i=0;i<byteArr.length;i++){
				p3.isValidPacket((byte) byteArr[i]);
			}
		}
		
		
	}

	public EegData getDataPortion() {
		return data ;
	}

	public boolean endConnection() {
		return false;
	}

	public String getType() {
		return "OpenEEG_P3" ;
	}

	public boolean initConnection(InputStream in, OutputStream out) {
		return true;
	}

	public boolean endConnection(InputStream in, OutputStream out) throws IOException, ProtocolException {
		return true;
	}
	private int out ;

	public int getPacketSize() {
		return 8;
	}

	public int[] getExtremeValues() {
		return new int[]{-512,512};
	}
	
	public int getNumberOfChannels() {
		return 6;
	}

	public void getDataPortion(EegData portionToUpdate)
			throws ProtocolException {
		int i=0 ;
		for(int val:data.getValues()){
			portionToUpdate.getValues()[i++] = val ;
		}
	}
}
