package hardware;

import general.EegData;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.util.zip.GZIPInputStream;

import collect.DataObject;
import configuration.Configuration;

public class HddRecordingPlayer implements IProtocol {

	private String file;

	// compatibility with protocol factory
	public HddRecordingPlayer(){
		
	}
	public HddRecordingPlayer(String fileUrl){
		this.file = fileUrl ;
		readStart() ;
	}
	private void readStart(){
		System.out.println("[HddRecordingPlayer] opening file ("+file+") for reading");
		try {
			in = new GZIPInputStream(new FileInputStream(file)) ;
			obj = new ObjectInputStream(in);
		} catch(FileNotFoundException e) {
			System.err.println("Could not open file: " + file);
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	private ObjectInputStream obj = null ;
	private GZIPInputStream in = null;

	private Object object = null ;

	public DataObject goToInitializationSession(){
		return goToSession(DataObject.HEADER_INITIALIZATION) ;
	}

	public DataObject goToSession(int type){
		try {
			while(!(object instanceof DataObject && ((DataObject)object).getDataType()==type)){
				if(((DataObject) object).getDataType()!=1)
				System.out.println("read... obj "+object+" type: "+((DataObject)object).getDataType());
				object = obj.readObject();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return (DataObject) object ;
	}

	public EegData getDataPortion(){
		return new EegData(1,6,new int[]{1,1,1,1,1,1});
	}

	public Configuration getConfiguration() {
		try {
			while(object==null||!(object instanceof DataObject && ((DataObject) object).getDataType()==DataObject.TYPE_CONFIGURATION)){
				System.out.println("read...");
				object = obj.readObject();
				System.out.println("read..."+object+" aval "+obj.available());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		return (Configuration) ((DataObject) object).getData() ;
	}
	public DataObject getDataPacket() {
		try {
			object = obj.readObject();
			if(object instanceof DataObject)
				return (DataObject) object ; 

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public boolean endConnection(InputStream in, OutputStream out)
			throws IOException, ProtocolException {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public byte[] generatePacket(int sampleNumber, int[] arrayOfValues) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void getDataPortion(EegData portionToUpdate)
			throws ProtocolException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int[] getExtremeValues() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int getNumberOfChannels() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getPacketSize() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean initConnection(InputStream in, OutputStream out)
			throws IOException, ProtocolException {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public int isValidPacket(byte b) throws ProtocolException {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}
}
