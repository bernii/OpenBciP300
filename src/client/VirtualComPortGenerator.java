package client;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import hardware.BraintronicsProtocol;
import hardware.IProtocol;
import hardware.P3protocol;
import hardware.ProtocolFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;


public class VirtualComPortGenerator {
	// 17 / 18 pair

	static OutputStream out;

	public static void setWriterStream(OutputStream out) {
		VirtualComPortGenerator.out = out;
	}

	public static void send(byte[] bytes) {
		try {
			out.write(bytes);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static InputStream in;
	private static SerialPort serialPort;

	public void connect(String portDefinition) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portDefinition);
		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Port in use!");
		} else {
			// points who owns the port and connection timeout
			serialPort = (SerialPort) portIdentifier.open("RS232Example", 2000);
			// setup connection parameters
			serialPort.setSerialPortParams(
					138400, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			// setup serial port writer
			out = serialPort.getOutputStream() ;
			in = serialPort.getInputStream() ;

		}
	}

	static byte[] byteArr ;
	private static IProtocol packetSupply;


	public static void main(String[] args) throws Exception {
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
		// connects to the port which name (e.g. COM1) is in the first argument
		new VirtualComPortGenerator().connect(args[0]);
		// OpenEEG_P3
		packetSupply = ProtocolFactory.get(args[1]);
		// send HELO message through serial port using protocol implementation
		Random rnd = new Random() ;

		int freq = 1000/Integer.parseInt(args[2]) ;
		System.out.println("Prepearing for sending packets through "+args[0]+" protocol: "+args[1]+" sample every "+freq+" ms");
		if(packetSupply.getType().equalsIgnoreCase("Braintronics")){
			byte[] buff = new byte[2];
			int readBytes = 0 ;
			System.out.println("Waiting for halting packet") ;
			while((readBytes += in.read(buff,readBytes,buff.length-readBytes))<buff.length){
				Thread.sleep(10,1); 
				if(readBytes!=0)
					System.out.println("read "+readBytes+" - "+ bytesToHex(buff)+" w8 for more..");
			}
			if(buff[0]=='H'&&buff[1]==~(byte)('H')){
				System.out.println("OK - halted");
				VirtualComPortGenerator.send(new byte[]{0x09});
				/*outbuffer[2] = 0x68;
				outbuffer[3] = ~0x68;
				outbuffer[4] = 0x03;
				outbuffer[5] = ~0x03;
				 */ 
			}
			buff = new byte[6];
			System.out.println("Waiting for starting packet") ;
			readBytes =0 ;
			while((readBytes += in.read(buff,readBytes,buff.length-readBytes))<buff.length){
				Thread.sleep(10,1); 
				if(readBytes!=0)
					System.out.println("read "+readBytes+" - "+ bytesToHex(buff)+" w8 for more..");
			}
			System.out.print("got "+bytesToHex(buff)+" from app["+readBytes+"]: ") ;
			if(buff[0]=='S'&&buff[1]==~(byte)('S')&&buff[2]==0x68&&buff[3]==~0x68&&
					buff[4]==0x03&&buff[5]==~0x03){
				System.out.println("OK - sampling freq = 250");
				VirtualComPortGenerator.send(new byte[]{0x09});
				/*outbuffer[2] = 0x68;
				outbuffer[3] = ~0x68;
				outbuffer[4] = 0x03;
				outbuffer[5] = ~0x03;
				 */ 
			}
			else{
				System.out.println("ERROR");
			}
		}
		System.out.println("Sending packets") ;

		serialPort.addEventListener(new SerialPortEventListener(){
			public void serialEvent(SerialPortEvent event){
				switch(event.getEventType()) {
				case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
					break;
				case SerialPortEvent.DATA_AVAILABLE:
					try {
						endConncetion() ;
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					break;
				case SerialPortEvent.BI:
					break;
				}
			}});
		serialPort.notifyOnDataAvailable(true);
		int i = 0 ;
		int tempVal2 = 0;
		int step = 40 ;
		int step2 = 740 ;
		int tmp = 0 ;
		long timer = 0 ;
		while(true){
			if ( System.in.available() > 0 ){
				tmp++;
				if(tmp%2==0){
					step *=-1 ;
					step2 *=-1 ;
					System.out.println(" KEY "+step);
					timer = 0 ;
				}
			}

			if(packetSupply instanceof BraintronicsProtocol){
				int[] vals = new int[64];
				timer++ ;
				for(int h = 0  ;h <vals.length;h++)
					if(h==0){
						if(timer<80)
							vals[h] = rnd.nextInt(3024)-1512+2800 ;
						else
							vals[h] = rnd.nextInt(3024)-1512 ;
					}else if(h==1){
						vals[h] = tempVal2+=step2 ;
						if(tempVal2>32000||tempVal2<-32000)
							step2 *= -1 ;
					}else
						vals[h] = rnd.nextInt(10024) ;
				byteArr = packetSupply.generatePacket(i++,vals);
			}else{
				int[] vals = new int[6];
				for(int h = 0  ;h <vals.length;h++)
					vals[h] = rnd.nextInt(1024)-512 ;
				byteArr = packetSupply.generatePacket(i++,vals);
			}
			VirtualComPortGenerator.send(byteArr);
			LockSupport.parkNanos(freq*1000000L);
		}
	}

	private static String byteToHex(byte data)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(toHexChar((data>>>4)&0x0F));
		buf.append(toHexChar(data&0x0F));
		return buf.toString();
	}

	public static void endConncetion() throws IOException, InterruptedException{
		System.out.println("Server wants to close the connection !");
		if(packetSupply.getType().equalsIgnoreCase("Braintronics")){
			byte[] buff = new byte[2];
			int readBytes = 0 ;
			System.out.println("Waiting for ending packet") ;

			while((readBytes += in.read(buff,readBytes,buff.length-readBytes))<buff.length){
				Thread.sleep(10,1); 
				if(readBytes!=0)
					System.out.println("read "+readBytes+" - "+ bytesToHex(buff)+" w8 for more..");
			}
			System.out.print("got "+bytesToHex(buff)+" from app["+readBytes+"]: ") ;
			if(buff[0]=='H'&&buff[1]==~(byte)('H')){
				System.out.println("OK ");
				VirtualComPortGenerator.send(new byte[]{0x09});
				serialPort.close();
				System.exit(1);
				/*outbuffer[2] = 0x68;
				outbuffer[3] = ~0x68;
				outbuffer[4] = 0x03;
				outbuffer[5] = ~0x03;
				 */ 
			}
			else{
				System.out.println("ERROR");
			}

		}
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
}
