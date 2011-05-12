package hardware;

import general.EegData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;

public interface IProtocol {

	public void getDataPortion(EegData portionToUpdate) throws ProtocolException ;
	
	/**
	 * Initis connection with hardware
	 * @param out 
	 * @param in 
	 * @return true if init was successfull, false if not
	 */
	public boolean initConnection(InputStream in, OutputStream out) throws IOException,ProtocolException;
	
	/**
	 * Ends connection with hardware
	 * @param out 
	 * @param in 
	 * @return true if init was successfull, false if not
	 * @throws ProtocolException 
	 * @throws IOException 
	 */
	public boolean endConnection(InputStream in, OutputStream out) throws IOException, ProtocolException;

	/**
	 * Returns type of the protocol (OpenEEG_P2,OpenEEG_P3, Braintronics...)
	 * @return
	 */
	public String getType();

	public byte[] generatePacket(int sampleNumber, int[] arrayOfValues);

	public void reset();

	public int getPacketSize();

	public int isValidPacket(byte b) throws ProtocolException;
	
	public int[] getExtremeValues();

	public int getNumberOfChannels();
}
