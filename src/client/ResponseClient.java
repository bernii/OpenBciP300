package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import javolution.text.TextBuilder;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import display.ControlPanel;

public class ResponseClient {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label labelTextConnStatus = null;
	private Label labelConnectionStatus = null;
	private Button buttonConnectDisconnect = null;
	private Group groupSettings = null;
	private Label labelHostName = null;
	private Label labelHostPort = null;
	private Text textHostName = null;
	private Text textHostPort = null;
	private Label labelOutput = null;
	private Text textOutput = null;
	private String serverInfo;
	private String serverInfoMatrixUpdater ;
	private boolean isConnected;
	private boolean isConnectedMatrixUpdater ;

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("jP300 - server client");
		sShell.setSize(new Point(449, 407));
		sShell.setLayout(null);
		
		
		createGroupSettings();
		createGroupMatrixChanger();
		
	}

	/**
	 * This method initializes groupSettings	
	 *
	 */
	private void createGroupSettings() {
		groupSettings = new Group(sShell, SWT.NONE);
		groupSettings.setLayout(null);
		groupSettings.setText("Output listener");
		groupSettings.setBounds(new Rectangle(13, 12, 411, 163));
		labelHostName = new Label(groupSettings, SWT.NONE);
		labelHostName.setBounds(new Rectangle(17, 106, 135, 15));
		labelHostName.setText("Host name");
		labelHostPort = new Label(groupSettings, SWT.NONE);
		labelHostPort.setBounds(new Rectangle(209, 106, 28, 15));
		labelHostPort.setText("Port");
		textHostName = new Text(groupSettings, SWT.BORDER);
		textHostName.setBounds(new Rectangle(16, 130, 173, 21));
		textHostName.setText("localhost");
		textHostPort = new Text(groupSettings, SWT.BORDER);
		textHostPort.setBounds(new Rectangle(209, 129, 76, 21));
		textHostPort.setText("4444");
		
		labelTextConnStatus = new Label(groupSettings, SWT.NONE);
		labelTextConnStatus.setBounds(new Rectangle(15, 30, 121, 15));
		labelTextConnStatus.setText("Connection status:");
		labelConnectionStatus = new Label(groupSettings, SWT.NONE);
		labelConnectionStatus.setBounds(new Rectangle(143, 30, 257, 15));
		labelConnectionStatus.setText("not connected");
		labelOutput = new Label(groupSettings, SWT.NONE);
		labelOutput.setBounds(new Rectangle(16, 54, 61, 15));
		labelOutput.setText("Output");
		textOutput = new Text(groupSettings, SWT.BORDER);
		textOutput.setBounds(new Rectangle(17, 74, 322, 21));
		
		buttonConnectDisconnect = new Button(groupSettings, SWT.NONE);
		buttonConnectDisconnect.setBounds(new Rectangle(326, 127, 76, 25));
		buttonConnectDisconnect.setText("connect");
		checkBoxChangeMatrixOnOutput = new Button(groupSettings, SWT.CHECK);
		checkBoxChangeMatrixOnOutput.setBounds(new Rectangle(245, 50, 158, 16));
		checkBoxChangeMatrixOnOutput.setText("update matrix on output");
		buttonConnectDisconnect
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				//System.out.println("widgetSelected()"); // TODO Auto-generated Event stub widgetSelected()
				if(!isConnected)
					connectionStart() ;
				else
					connectionEnd() ;
			}
		});
		
	}

	private volatile String outputText = "";  //  @jve:decl-index=0:

	private void updateOutputText(){
		if(!Display.getDefault().isDisposed()){
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					textOutput.setText(outputText);
				}
			});
		}
	}

	private Socket kkSocket = null;
	private Socket kkSocketMatrixUpdater = null ;
	private Group groupMatrixChanger = null;
	private Label labelHostname = null;
	private Label labelPortNumber = null;
	private Text textMatrixUpdaterHost = null;
	private Text textMatrixUpdaterPort = null;
	private Button buttonMatrixUpdaterConnectDisconnect = null;
	private Label labelMatrixUpdaterConnStatus = null;
	private Label labelMatrixUpdaterStatus = null;
	private Button buttonUpdateMatrix = null;
	private Thread changerThread;  //  @jve:decl-index=0:
	private Button checkBoxChangeMatrixOnOutput = null;

	private void connectionEnd(){
		try {
			kkSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void connectionMatrixUpdaterEnd(){
		try {
			kkSocketMatrixUpdater.close();
			changerThread.setName("end") ;
			synchronized(changerThread){
				changerThread.notify();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void connectionMatrixUpdaterStart(){
		final String hostname = textMatrixUpdaterHost.getText(); 
		final int hostPort = Integer.parseInt(textMatrixUpdaterPort.getText());
		changerThread = new Thread(){
			
			public volatile boolean run = true;

			public void run(){
				System.out.println("[ClientMatrixUpdater]: Establishing connection !!");
//				Socket kkSocket = null;
				PrintWriter out = null;
				BufferedReader in = null;

				try {
					kkSocketMatrixUpdater = new Socket(hostname,hostPort);
					out = new PrintWriter(kkSocketMatrixUpdater.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(kkSocketMatrixUpdater.getInputStream()));
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host: "+hostname+":"+hostPort);
					serverInfo = "Don't know about host" ;
					setConntectedMatrixUpdater(false);
					return ;
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection to: "+hostname+":"+hostPort);
					serverInfo = "Couldn't get I/O" ;
					setConntectedMatrixUpdater(false);
					return ;
				}

				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				String fromServer;

				try{
					fromServer = in.readLine() ;
					serverInfoMatrixUpdater = fromServer ;
					setConntectedMatrixUpdater(true);
					while(run&&this.getName().indexOf("end")==-1){
						System.out.println("[RespClient] wainting for command !");
						try {
							synchronized(this){
								this.wait();
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.out.println("[RespClient] sending text !");
						Random rnd = new Random();
						TextBuilder buff = new TextBuilder("<userVisibleMatrix><name>userVisibleMatrix</name>") ;
						int rndRows = rnd.nextInt(4) ;
						int rndCols = rnd.nextInt(4) ;
						for(int rows = 0;rows<4+rndRows;rows++){
							buff.append("<row>");
							for(int cols = 0;cols<4+rndCols;cols++){
								buff.append("<txt>"+(char)('a' + (int)Math.floor(Math.random() * 26D))+"</txt>");
							}
							buff.append("</row>");
						}
						buff.append("</userVisibleMatrix>");
						out.println(buff);
					}
					out.close();
					in.close();
					stdIn.close();
					kkSocketMatrixUpdater.close();
					setConntectedMatrixUpdater(false);
				}catch(SocketException e){
					System.out.println("[Client]: Server closed the connection !!");
					setConntectedMatrixUpdater(false);
				} catch (IOException e) {
					e.printStackTrace();
					setConntectedMatrixUpdater(false);
				}
			}
		} ;
		changerThread.start();
	}
	
	private void connectionStart(){
		final String hostname = textHostName.getText(); 
		final int hostPort = Integer.parseInt(textHostPort.getText());
		new Thread(){
			public void run(){
				System.out.println("[Client]: Establishing connection !!");
				PrintWriter out = null;
				BufferedReader in = null;

				try {
					kkSocket = new Socket(hostname,hostPort);
					out = new PrintWriter(kkSocket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
				} catch (UnknownHostException e) {
					System.err.println("Don't know about host: "+hostname);
					serverInfo = "Don't know about host" ;
					setConntected(false);
					return ;
				} catch (IOException e) {
					System.err.println("Couldn't get I/O for the connection to: "+hostname);
					serverInfo = "Couldn't get I/O" ;
					setConntected(false);
					return ;
				}

				BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
				String fromServer;
				String fromUser;
				int i = 0 ;

				try{
					while ((fromServer = in.readLine()) != null) {
						if(i==0){
							serverInfo = fromServer ;
							setConntected(true);
						}
						else{
							outputText += fromServer ;
							if(!Display.getDefault().isDisposed()){
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										if(isConnectedMatrixUpdater&&checkBoxChangeMatrixOnOutput.getSelection()){
											synchronized(changerThread){
												changerThread.notify();
											}
										}
									}});}
							updateOutputText();
						}
						i++;
					}
					out.close();
					in.close();
					stdIn.close();
					kkSocket.close();
				}catch(SocketException e){
					// server unexpectedly closed the connection
					System.out.println("[Client]: Server closed the connection !!");
					setConntected(false);
				} catch (IOException e) {
					e.printStackTrace();
					setConntected(false);
				}
			}
		}.start() ;
	}

	private void setConntectedMatrixUpdater(final boolean isConn){
		if(!Display.getDefault().isDisposed()){
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					isConnectedMatrixUpdater = isConn ;
					if(isConnectedMatrixUpdater){
						buttonMatrixUpdaterConnectDisconnect.setText("disconnect") ;
						labelMatrixUpdaterStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						labelMatrixUpdaterStatus.setText("conntected: "+serverInfoMatrixUpdater) ;
						serverInfoMatrixUpdater="" ;
					}else{
						buttonMatrixUpdaterConnectDisconnect.setText("connect") ;
						labelMatrixUpdaterStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						labelMatrixUpdaterStatus.setText("not conntected: "+serverInfoMatrixUpdater) ;
					}
				}
			});
		}
	}
	
	private void setConntected(final boolean isConn){
		if(!Display.getDefault().isDisposed()){
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					isConnected = isConn ;
					if(isConnected){
						buttonConnectDisconnect.setText("disconnect") ;
						labelConnectionStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));
						labelConnectionStatus.setText("conntected: "+serverInfo) ;
						serverInfo="" ;
					}else{
						buttonConnectDisconnect.setText("connect") ;
						labelConnectionStatus.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
						labelConnectionStatus.setText("not conntected: "+serverInfo) ;
					}
				}
			});
		}
	}

	/**
	 * This method initializes groupMatrixChanger	
	 *
	 */
	private void createGroupMatrixChanger() {
		groupMatrixChanger = new Group(sShell, SWT.NONE);
		groupMatrixChanger.setLayout(null);
		groupMatrixChanger.setText("Matrix updater");
		groupMatrixChanger.setBounds(new Rectangle(13, 183, 411, 179));
		labelHostname = new Label(groupMatrixChanger, SWT.NONE);
		labelHostname.setBounds(new Rectangle(14, 123, 84, 15));
		labelHostname.setText("Host name");
		labelPortNumber = new Label(groupMatrixChanger, SWT.NONE);
		labelPortNumber.setBounds(new Rectangle(231, 124, 61, 15));
		labelPortNumber.setText("Port");
		textMatrixUpdaterHost = new Text(groupMatrixChanger, SWT.BORDER);
		textMatrixUpdaterHost.setBounds(new Rectangle(13, 142, 164, 21));
		textMatrixUpdaterHost.setText("localhost");
		textMatrixUpdaterPort = new Text(groupMatrixChanger, SWT.BORDER);
		textMatrixUpdaterPort.setBounds(new Rectangle(231, 142, 76, 21));
		textMatrixUpdaterPort.setText("1986");
		buttonMatrixUpdaterConnectDisconnect = new Button(groupMatrixChanger, SWT.NONE);
		buttonMatrixUpdaterConnectDisconnect.setBounds(new Rectangle(323, 138, 73, 25));
		buttonMatrixUpdaterConnectDisconnect.setText("connect");
		buttonMatrixUpdaterConnectDisconnect
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(!isConnectedMatrixUpdater)
					connectionMatrixUpdaterStart() ;
				else
					connectionMatrixUpdaterEnd() ;
			}
		});
		labelMatrixUpdaterConnStatus = new Label(groupMatrixChanger, SWT.NONE);
		labelMatrixUpdaterConnStatus.setBounds(new Rectangle(12, 26, 107, 15));
		labelMatrixUpdaterConnStatus.setText("Connection status:");
		labelMatrixUpdaterStatus = new Label(groupMatrixChanger, SWT.NONE);
		labelMatrixUpdaterStatus.setBounds(new Rectangle(140, 26, 264, 15));
		labelMatrixUpdaterStatus.setText("not connected");
		buttonUpdateMatrix = new Button(groupMatrixChanger, SWT.NONE);
		buttonUpdateMatrix.setBounds(new Rectangle(290, 85, 112, 25));
		buttonUpdateMatrix.setText("send update");
		buttonUpdateMatrix
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
						synchronized(changerThread){
							changerThread.notify();
						}
					}
				});
	}

	public static void main(String[] args) {
		Display display = Display.getDefault();
		ResponseClient thisClass = new ResponseClient();
		thisClass.createSShell();
		thisClass.sShell.open();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();

		try {
			if(thisClass.kkSocket!=null&&thisClass.kkSocket.isClosed()==false)
				thisClass.kkSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
