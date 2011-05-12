package server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import javolution.text.CharArray;
import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReaderImpl;

import astart.ProjectStarter;

public class ServerMatrixChangerThread extends IServerThread {
	
	public ServerMatrixChangerThread(Socket socket) {
		super(socket);
	}

	public void run() {
		PrintWriter out = null ;
		BufferedReader in = null ;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(
					new InputStreamReader(
							socket.getInputStream()));
			out.println("jP300 - ver "+ProjectStarter.VERSION);
			String fromClient;
			TextBuilder textBuffer = new TextBuilder();
			while ((fromClient = in.readLine()) != null) {
				textBuffer.append(fromClient);
				if(fromClient.indexOf("</userVisibleMatrix>")!=-1){
					updateMatrix(textBuffer);
					textBuffer.clear();
				}
			}	
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			out.close();
			try {
				in.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateMatrix(TextBuilder textBuffer) {
		System.out.println("[ServerMatrixChagnerThread] Updating with text: "+textBuffer);
		FastTable<FastTable<String>> characters = new FastTable<FastTable<String>>() ;
		FastTable<String> row = new FastTable<String>() ;
		XMLStreamReaderImpl xmlReader = new XMLStreamReaderImpl();
		try {
			xmlReader.setInput(new ByteArrayInputStream(textBuffer.toString().getBytes("UTF-8")));
			for (int e=xmlReader.next(); e != XMLStreamConstants.END_DOCUMENT; e = xmlReader.next()) {
		         switch (e) { // Event
		             case XMLStreamConstants.START_ELEMENT:
		             if (xmlReader.getLocalName().equals("txt")) {
		                  CharArray hour = xmlReader.getElementText();
		                  System.out.println("txt = "+hour);
		                  row.add(hour.toString());
		             }
		             break;
		             
		             case XMLStreamConstants.END_ELEMENT:
			             if (xmlReader.getLocalName().equals("row")) {
			            	 characters.add(row);
			            	 row = new FastTable<String>() ;
			             }
		             break;
		         }         
		     }
		     xmlReader.close();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (XMLStreamException e1) {
			e1.printStackTrace();
		}
	     ProjectStarter.getDisplayUpdater().updateDisplayMatrix(characters);
	}
}
