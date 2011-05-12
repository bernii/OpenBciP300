package hardware;

import general.EegData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;

import org.apache.log4j.Logger;

import astart.ProjectStarter;

/**
 * Implementation of Braintronics hardware protocol
 * @author berni
 *
 */
public class BraintronicsProtocol implements IProtocol {

	private byte[] clearBuffer = new byte[4096];

	static  Logger logger = Logger.getLogger(BraintronicsProtocol.class);

	public boolean endConnection(InputStream in, OutputStream out) throws IOException,ProtocolException {
		byte[] outbuffer = new byte[2];

		outbuffer[0] = 'H';
		outbuffer[1] = ~(byte)('H');
		out.write(outbuffer) ;
		System.out.println("[Braintronics:endConn] writing "+bytesToHex(outbuffer)+" to client");
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//		 get data that is still somewhere in buffer
		int bytesRead = 0 ;
		while(in.available()>0){
			bytesRead = in.read(clearBuffer);
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		byte b = clearBuffer[bytesRead-1] ;
		System.out.println("[Braintronics:endConn] got "+byteToHex(b)+" from client");
		if(b == (byte)0x00){
			b = (byte) in.read() ;
		}else if(b!=(byte)0x09){
			if(b==(byte)0x02)
				throw new ProtocolException(" ERROR: EEG AMPLIFIER FAILED TO RECEIVE CONFIGURATION BYTES") ;
			else if(b==(byte)0x03)
				throw new ProtocolException(" ERROR: EEG AMPLIFIER RECEIVED CORRUPTED CONFIGURATION BYTES") ;
			else if(b==(byte)0x04)
				throw new ProtocolException(" ERROR: EEG AMPLIFIER HAS RECEIVED UNKNOW CONFIGURATION BYTES") ;
			else
				throw new ProtocolException(" ERROR: EEG AMPLIFIER HAS SENT UNKNOW ERROR CODE "+b);
		}
		return true;
	}

	byte[] generatorOut = null ;
	public byte[] generatePacket(int sampleNumber, int[] arrayOfValues) {
		if(generatorOut==null)
			generatorOut = new byte[4+arrayOfValues.length*2];
		generatorOut[0] = (byte) 0xFF ;
		generatorOut[1] = (byte) 0xFF ;
		generatorOut[2] = (byte)(sampleNumber & 0xFF);
		generatorOut[3] = (byte)((sampleNumber >> 8) & 0xFF);

		int a = 4 ;
		for(int i=0;i<arrayOfValues.length;i++){
			generatorOut[a++] = (byte) (((short)arrayOfValues[i]) & 0xff) ;
			generatorOut[a++] = (byte) (((short)arrayOfValues[i])>>>8 & 0xff) ;
		}
		return generatorOut;
	}

	public String getType() {
		return "Braintronics" ;
	}

	public boolean initConnection(InputStream in, OutputStream out) throws IOException,ProtocolException {

		endConnection(in, out) ;

		byte[] outbuffer = new byte[6];
		int samplingFreq = ProjectStarter.getConf().getValueInt("samplingFrequency");

		outbuffer[0] = 'S';
		outbuffer[1] = ~(byte)('S');

		if(samplingFreq==200)
		{
			outbuffer[2] = 0x62;
			outbuffer[3] = ~0x62;
			outbuffer[4] = 0x04;
			outbuffer[5] = ~0x04;
		}else if(samplingFreq==250){
			/** UW SETTINGS **/
			outbuffer[2] = (byte)0x68;
			outbuffer[3] = (byte)~0x68;
			outbuffer[4] = (byte)0x03;
			outbuffer[5] = (byte)~0x03;
		}else if(samplingFreq==400){
			outbuffer[2] = (byte) 0xF1;
			outbuffer[3] = (byte) ~0xF1;
			outbuffer[4] = 0x01;
			outbuffer[5] = ~0x01;
		}else if(samplingFreq==500){
			outbuffer[2] = 0x74;
			outbuffer[3] = ~0x74;
			outbuffer[4] = 0x01;
			outbuffer[5] = ~0x01;
		}else if(samplingFreq==625){
			outbuffer[2] = 0x10;
			outbuffer[3] = ~0x10;
			outbuffer[4] = 0x01;
			outbuffer[5] = ~0x01;
		}else if(samplingFreq==1000){
			outbuffer[2] = 0x7A;
			outbuffer[3] = ~0x7A;
			outbuffer[4] = 0x00;
			outbuffer[5] = ~0x00;
		}else if(samplingFreq==1250){
			outbuffer[2] = 0x48;
			outbuffer[3] = ~0x48;
			outbuffer[4] = 0x00;
			outbuffer[5] = ~0x00;
		}
		logger.info("[Braintronics:init] writing "+bytesToHex(outbuffer)+" to client");
		out.write(outbuffer) ;

		byte b ;
		if((b = (byte) in.read())==(byte)0x00){
			b = (byte) in.read() ;
		}
		if(b!=(byte)0x09){
			logger.info("[Braintronics:init] response "+byteToHex(b)+" from client");
			if(b==(byte)0x02)
				throw new ProtocolException(" ERROR: EEG AMPLIFIER FAILED TO RECEIVE CONFIGURATION BYTES") ;
			else if(b==(byte)0x03)
				throw new ProtocolException(" ERROR: EEG AMPLIFIER RECEIVED CORRUPTED CONFIGURATION BYTES") ;
			else if(b==(byte)0x04)
				throw new ProtocolException(" ERROR: EEG AMPLIFIER HAS RECEIVED UNKNOW CONFIGURATION BYTES") ;
			else
				throw new ProtocolException(" ERROR: EEG AMPLIFIER HAS SENT UNKNOW ERROR CODE "+b);
		}
		logger.info("[Braintronics:init] completed - communication started OK");

		return true;
	}

	private short lastPacketNumber ;

	private int channelsNumber = 64;

	//	private short[] packetBuffer = new short[channelsNumber+2] ;

	//	private EegData data;

	private static short ToShort (byte[] data) {
		//return (short) (0xff & data[0] |data[1] << 8 );
		short a = (short) data[0] ;
		short b = (short) data[1] ;
		return (short) ( (a&0xff) | (b&0xff) << 8 );
	}

	public static short ToShort (byte data,byte data1) {
		short a = (short) data ;
		short b = (short) data1 ;
		return (short) ( (a&0xff) | (b&0xff) << 8 );
	}

	private static String byteToHex(byte data)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data>>>4)&0x0F));
		buf.append(toHexChar(data&0x0F));
		return buf.toString();
	}

	private static char toHexChar(int i)
	{
		if ((0 <= i) && (i <= 9 ))
			return (char)('0' + i);
		else
			return (char)('a' + (i-10));
	}

	private static String bytesToHex(byte[] data)
	{
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++)
		{
			buf.append(byteToHex(data[i]));
		}
		return buf.toString();
	}

	private volatile int bytesRead = 0 ;
	private byte[] b = new byte[2] ;
	private byte[] dataBuff = new byte[2*channelsNumber] ;

	int packetSize = 0 ;
	byte[] recievedBuffer = new byte[2*channelsNumber+2+2]; 
	byte[] recievedBuffer1 = new byte[2*channelsNumber+2+2]; 

	byte[] bufferToWorkOn = recievedBuffer ;
	byte[] bufferToWorkPacketize = recievedBuffer ;

	boolean searchForPacketStart = false ;
	boolean resetPacketNumber = false ;
	public int isValidPacket(byte oneByte){
		bufferToWorkOn[packetSize] = oneByte ;
		packetSize++ ;
		if(!searchForPacketStart){

			if(packetSize>=(2*channelsNumber+2+2)){
				packetSize = 0 ;
				if(ToShort(bufferToWorkOn[0],bufferToWorkOn[1])!=(short)0xFFFF){
					searchForPacketStart = true ;
					System.out.println("[Braintronics:isValidPacket] Packet not valid "+bytesToHex(new byte[]{bufferToWorkOn[0],bufferToWorkOn[1]})+" short "+ToShort(new byte[]{bufferToWorkOn[0],bufferToWorkOn[1]})+" != "+(short)0xFFFF);
					return -1 ;
				}
				if(bufferToWorkOn==recievedBuffer){
					bufferToWorkOn = recievedBuffer1 ;
					bufferToWorkPacketize = recievedBuffer ;
				}else{
					bufferToWorkOn = recievedBuffer ;
					bufferToWorkPacketize = recievedBuffer1 ;
				}
				return 1 ;
			}
		}else{
			if(packetSize>=2&&searchForPacketStart)
				if(ToShort(bufferToWorkOn[0],bufferToWorkOn[1])==(short)0xFFFF){
					resetPacketNumber = true ;
					searchForPacketStart = false ;
					System.out.println("[Braintronics:isValidPacket] Found next packet beggining "+bytesToHex(new byte[]{bufferToWorkOn[0],bufferToWorkOn[1]})+" short "+ToShort(new byte[]{bufferToWorkOn[0],bufferToWorkOn[1]}));
				}else{
					bufferToWorkOn[0] = bufferToWorkOn[1] ;
					packetSize = 1 ;
				}
		}
		return 0 ;
	}

	public void getDataPortion(EegData portion) throws ProtocolException{
		int a = 0 ;
		if(ToShort(bufferToWorkPacketize[a],bufferToWorkPacketize[a+1])!=(short)0xFFFF){
			logger.info("[Braintronics:isValidPacket] NOT VALID ");
			throw new ProtocolException("Packet not valid "+bytesToHex(new byte[]{bufferToWorkPacketize[a],bufferToWorkPacketize[a+1]})+" short "+ToShort(new byte[]{bufferToWorkPacketize[a],bufferToWorkPacketize[a+1]})+" != "+(short)0xFFFF);
		}

		a = 2 ;
		short currentPacketNumber = ToShort(bufferToWorkPacketize[a],bufferToWorkPacketize[a+1]) ;
		if(resetPacketNumber){
			System.out.println("[BrainttronicsProtocol:isValidPacket] resetting packet number");
			resetPacketNumber = false ;
			lastPacketNumber = (short) (currentPacketNumber -1) ;
		}
		if(((currentPacketNumber==0) && (lastPacketNumber==0)) || 
				(currentPacketNumber==(lastPacketNumber + 1)) || 
				(lastPacketNumber==32767&&currentPacketNumber==-32768) || 
				((currentPacketNumber==0) && (lastPacketNumber==(short)0xFFFF))){
		}else{
			System.out.println("[BrainttronicsProtocol:isValidPacket] Packet not valid - current packetNumber "+bytesToHex(new byte[]{bufferToWorkPacketize[a],bufferToWorkPacketize[a+1]})+" , "+currentPacketNumber+" previous packet number "+lastPacketNumber) ;
		}
		portion.setPacketNumber(currentPacketNumber);
		//all ok - reading data
		a = 4 ;
		for(int i = 0 ; i <getNumberOfChannels() ;i++){
			portion.setValue(i,ToShort(bufferToWorkPacketize[a],bufferToWorkPacketize[a+1])) ;
			a+=2 ;
		}
		lastPacketNumber = currentPacketNumber ;
	}

	public int isValidPacket(InputStream in) throws IOException {
		// all 2 bytes values - short !
		// [0xFFFF] [cncn][datadata]

		bytesRead = 0 ;
		while((bytesRead += in.read(b,bytesRead,b.length-bytesRead))<b.length) ;

		if(ToShort(b)!=(short)0xFFFF){
			logger.info("[Braintronics:isValidPacket] NOT VALID ");
			throw new ProtocolException("Packet not valid "+bytesToHex(b)+" short "+ToShort(b)+" != "+(short)0xFFFF+", bytes read = "+bytesRead);
		}
		bytesRead = 0 ;
		while((bytesRead += in.read(b,bytesRead,b.length-bytesRead))<b.length) ;
		short currentPacketNumber = ToShort(b) ;

		if(((currentPacketNumber==0) && (lastPacketNumber==0)) || 
				(currentPacketNumber==(lastPacketNumber + 1)) || 
				(lastPacketNumber==32767&&currentPacketNumber==-32768) || 
				((currentPacketNumber==0) && (lastPacketNumber==(short)0xFFFF))){
			//all ok - reading data
			int[] vals = new int[channelsNumber] ;
			bytesRead =0 ;

			while((bytesRead += in.read(dataBuff,bytesRead,dataBuff.length-bytesRead))<dataBuff.length) ;
			int a = 0 ;
			for(int i = 0 ; i <vals.length ;i++){
				vals[i] = ToShort(dataBuff[a],dataBuff[a+1]) ;
				a+=2 ;
			}
			lastPacketNumber = currentPacketNumber ;

			return 1 ;
		}else{
			throw new ProtocolException("Packet not valid - current packetNumber "+bytesToHex(b)+" , "+currentPacketNumber+" previous packet number "+lastPacketNumber) ;
		}
	}



	public void reset(){
		this.lastPacketNumber = 0 ;
	}

	public int getPacketSize() {
		return 4+2*channelsNumber;
	}

	public int[] getExtremeValues() {
		return new int[]{-32768,32768};
	}

	public int getNumberOfChannels() {
		return 64;
	}

	public static void main(String[] args) {
		int[] cos = new int[32];
		for(int i=0;i<cos.length;i++)
			cos[i] = 12 ;
		cos[3] = 4 ;
		cos[0] = cos[3] ;
		System.out.println(""+cos[0]+" "+cos[3]) ;
		cos[3] = 5 ;
		System.out.println(""+cos[0]+" "+cos[3]+" "+cos[4]) ;
	}
}
