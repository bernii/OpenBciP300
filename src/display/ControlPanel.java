package display;

import gnu.io.CommPortIdentifier;
import hardware.IDriverListener;
import hardware.IProtocol;
import hardware.ProtocolFactory;

import java.util.Enumeration;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import signalProcessing.ISignalAccumulatorListener;
import signalProcessing.ISignalAccumulatorWinnerListener;
import astart.ProjectStarter;
import configuration.ClassificatorConfElem;
import configuration.Configuration;
import configuration.ConfigurationLoader;
import configuration.ElementChannelsCalibration;
import configuration.ElementChannelsVisibility;
import configuration.ElementChannelsWeights;
import configuration.ElementClassificatorConf;
import configuration.ElementFrequnecyFilter;
import configuration.ElementMatrixDefinition;
import configuration.IConfigurationElement;

import org.eclipse.swt.layout.GridLayout;

public class ControlPanel implements ISignalAccumulatorWinnerListener {

	private Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="10,10"
	private Label labelStatData = null;
	private Menu menuBar = null;
	private Group groupMatrixResponses = null;
	private Group groupUserMatrix = null;
	private Group groupOptions = null;
	private Canvas canvasUserVisibleMatrix = null;
	private Canvas canvasMatrixResponses = null;

	public Text getTextOutput(){
		return textOutput ;
	}
	public IDriverListener getCanvasRawEegData(){
		return (IDriverListener) canvasRawEegData ;
	}

	public IDisplayUpdaterListener getCanvasUserVisibleMatrix(){
		return (IDisplayUpdaterListener) canvasUserVisibleMatrix ;
	}

	public ISignalAccumulatorListener getCanvasMatrixResponses(){
		return (ISignalAccumulatorListener) canvasMatrixResponses ;
	}

	public RawEegDataCanvasScale getRawEeegDataCanvasScale(){
		return (RawEegDataCanvasScale) canvasRawEegDataScale ;
	}

	public Canvas getCanvasRsquare(){
		return canvasRsquare ;
	}

	public ControlPanel(Shell s) {
		startSShell(s);
		sShell.open();
	}
	private void startSShell(Shell s) {
		checkForConfUpdate = false ;
		sShell = new Shell();
		sShell.setText("jP300 v."+ProjectStarter.VERSION+" - Control Panel");
		sShell.setSize(new Point(864, 852));

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		sShell.setLayout(gridLayout);

		menuBar = new Menu(sShell, SWT.BAR);
		MenuItem pushFile = new MenuItem(menuBar, SWT.PUSH);
		pushFile.setText("File");
		MenuItem pushSettings = new MenuItem(menuBar, SWT.PUSH);
		pushSettings.setText("Settings");
		pushSettings
		.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				checkForConfUpdate = false ;
				createSShellSettings();
				sShellSettings.open() ;
				checkForConfUpdate = true ;
			}
			public void widgetDefaultSelected(
					org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem pushAbout = new MenuItem(menuBar, SWT.PUSH);
		pushAbout.setText("About");
		pushAbout.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				checkForConfUpdate = false ;
				createSShellAbout();
				sShellAbout.open() ;
				checkForConfUpdate = true ;
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		sShell.setMenuBar(menuBar);
		labelStatData = new Label(sShell, SWT.NONE);
		labelStatData.setBounds(new Rectangle(9, 4, 642, 15));
		labelStatData.setText("Stats: xxx samples/s COMxxx xxx channels | protocol xxxx");

		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2 ;
		labelStatData.setLayoutData(gridData);

		createTabFolderCanvases();
		createGroupOptions();
		createGroupUserMatrix();
		createTabFolder();
		createGroupMatrixResponses();

		updateDataFromConfiguration() ;
		checkForConfUpdate = true ;
	}
	//	
	public ControlPanel() {

	}

	/**
	 * This method initializes groupMatrixResponses	
	 *
	 */
	private void createGroupMatrixResponses() {
		groupMatrixResponses = new Group(sShell, SWT.NONE);
		groupMatrixResponses.setLayout(new FillLayout());
		groupMatrixResponses.setText("Matrix Responses");
		GridData gridData = new GridData();
		gridData.widthHint = 492;
		gridData.heightHint = 165;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2 ;
		groupMatrixResponses.setLayoutData(gridData);

		createCanvasMatrixResponses();
		groupMatrixResponses.setBounds(new Rectangle(345, 544, 492, 165));
	}

	/**
	 * This method initializes groupUserMatrix	
	 *
	 */
	private void createGroupUserMatrix() {
		groupUserMatrix = new Group(sShell, SWT.NONE);
		groupUserMatrix.setLayout(new FillLayout());
		GridData gridData = new GridData();
		gridData.widthHint = 333;
		gridData.heightHint = 295;
		gridData.verticalSpan = 2 ;
		groupUserMatrix.setLayoutData(gridData);

		groupUserMatrix.setText("User Visible Matrix");
		createCanvasUserVisibleMatrix();
		groupUserMatrix.setBounds(new Rectangle(9, 414, 333, 295));
	}

	/**
	 * This method initializes groupOptions	
	 *
	 */
	private void createGroupOptions() {
		groupOptions = new Group(sShell, SWT.NONE);
		groupOptions.setLayout(null);
		groupOptions.setText("Options");

		GridData gridData = new GridData();	
		groupOptions.setLayoutData(gridData);


		createGroupOptionsTiming();
		buttonStartStop = new Button(groupOptions, SWT.NONE);
		buttonStartStop.setBounds(new Rectangle(109, 371, 69, 25));
		buttonStartStop.setText("start");
		buttonStartStop
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(buttonStartStop.getText().compareTo("start")==0){
					buttonTraining.setEnabled(false);
					ProjectStarter.getInstance().startRunning();
					buttonStartStop.setText("stop");
				}else{
					ProjectStarter.getInstance().stopRunning();
					buttonStartStop.setText("start");
				}
			}
		});
		createGroupOptionsOther();
		buttonTraining = new Button(groupOptions, SWT.NONE);
		buttonTraining.setBounds(new Rectangle(10, 371, 65, 25));
		buttonTraining.setText("training");
		buttonSaveConfiguration = new Button(groupOptions, SWT.NONE);
		buttonSaveConfiguration.setBounds(new Rectangle(10, 334, 125, 25));
		buttonSaveConfiguration.setText("save configuration");
		buttonSaveConfiguration
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("widgetSelected() - save config "); // TODO Auto-generated Event stub widgetSelected()
				ConfigurationLoader.save("conf.xml.txt") ;
			}
		});
		buttonTraining
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textOutput.getText().length()>0||
						ProjectStarter.getConf().getValueInt("signalSource")==Configuration.SOURCE_HDD){
					buttonTraining.setEnabled(false);
					buttonStartStop.setEnabled(false);
					ProjectStarter.getInstance().startLearning();
				}else
					textOutput.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED)) ;
			}
		});

	}

	private Shell userVisibleMatrixShell ;
	/**
	 * This method initializes canvasUserVisibleMatrix	
	 *
	 */
	private void createCanvasUserVisibleMatrix() {
		canvasUserVisibleMatrix = new UserVisibleMatrixCanvas(groupUserMatrix, SWT.NO_BACKGROUND);
		buttonSeperateWindow = new Button(canvasUserVisibleMatrix, SWT.NONE);
		buttonSeperateWindow.setBounds(new Rectangle(canvasUserVisibleMatrix.getSize().x, 1, 25, 25));
		buttonSeperateWindow.setText("[]");
		buttonSeperateWindow
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("widgetSelected() - user visible matrix NEW WINDOW"); 
				userVisibleMatrixShell = new Shell();
				userVisibleMatrixShell.setText("jP300 - matrix");
				userVisibleMatrixShell.setSize(new Point(864, 774));
				userVisibleMatrixShell.setLayout(new FillLayout());

				/**
				 * TODO: disposeListener might be needed to remove listeners from 
				 * listener list in displayUpdater
				 */
				UserVisibleMatrixCanvas copyOfuserVisibleMatrix = new UserVisibleMatrixCanvas(userVisibleMatrixShell, SWT.NO_BACKGROUND);
				copyOfuserVisibleMatrix.addMyPaintListener() ;
				copyOfuserVisibleMatrix.updateMatrix(((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).getChars());
				copyOfuserVisibleMatrix.setHighlightedPositions(((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).getHighlightedPositions());
				ProjectStarter.getDisplayUpdater().addListener(copyOfuserVisibleMatrix) ;

				userVisibleMatrixShell.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent event) {
						switch (event.keyCode) {

						case SWT.ESC:
							userVisibleMatrixShell.close() ;
							break;

						case 32:
							userVisibleMatrixShell.close() ;
							userVisibleMatrixShell = new Shell(SWT.NO_TRIM | SWT.ON_TOP);
							userVisibleMatrixShell.setLayout(new FillLayout());
							userVisibleMatrixShell.addKeyListener(new KeyAdapter() {
								public void keyPressed(KeyEvent event) {
									switch (event.keyCode) {

									default:
										userVisibleMatrixShell.close() ;
									break;
									}
								}
							});
							userVisibleMatrixShell.setBounds(Display.getDefault().getPrimaryMonitor().getBounds());
							UserVisibleMatrixCanvas copyOfuserVisibleMatrix = new UserVisibleMatrixCanvas(userVisibleMatrixShell, SWT.NO_BACKGROUND);
							copyOfuserVisibleMatrix.addMyPaintListener() ;
							copyOfuserVisibleMatrix.updateMatrix(((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).getChars());
							copyOfuserVisibleMatrix.setHighlightedPositions(((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).getHighlightedPositions());									
							ProjectStarter.getDisplayUpdater().addListener(copyOfuserVisibleMatrix) ;
							userVisibleMatrixShell.open() ;

							break;

						default:
							System.out.println(event.keyCode);
						break ;
						}
					}
				});
				userVisibleMatrixShell.open() ;
			}
		});
		((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).addMyPaintListener() ;
	}
	/**
	 * This method initializes canvasMatrixResponses	
	 *
	 */
	private void createCanvasMatrixResponses() {
		canvasMatrixResponses = new MatrixResponsesCanvas(groupMatrixResponses, SWT.NO_BACKGROUND);
		((MatrixResponsesCanvas) canvasMatrixResponses).addMyPaintListener() ;
		HighlightedPositionsPainter positionPainter = new HighlightedPositionsPainter((UserVisibleMatrixCanvas) canvasUserVisibleMatrix);
		((MatrixResponsesCanvas) canvasMatrixResponses).setPositionPainter(positionPainter) ;
		canvasMatrixResponses.redraw() ;
	}


	private void updateTimes(){
		if(textInterspaceMaxTime.getText()!=""&&textInterspaceMinTime.getText()!=""&&
				Integer.parseInt(textInterspaceMaxTime.getText())>Integer.parseInt(textInterspaceMinTime.getText())){
			ProjectStarter.getConf().setValue("interspaceTimeMax",Integer.parseInt(textInterspaceMaxTime.getText()));
			ProjectStarter.getConf().setValue("interspaceTimeMin",Integer.parseInt(textInterspaceMinTime.getText()));
			updateConfiguration() ;
			textInterspaceMaxTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE)) ;
			textInterspaceMinTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE)) ;
		}else{
			textInterspaceMaxTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED)) ;
			textInterspaceMinTime.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED)) ;
		}
	}

	/**
	 * This method initializes groupOptionsTiming	
	 *
	 */
	private void createGroupOptionsTiming() {
		groupOptionsTiming = new Group(groupOptions, SWT.NONE);
		groupOptionsTiming.setLayout(null);
		groupOptionsTiming.setText("Matrix display");
		groupOptionsTiming.setBounds(new Rectangle(9, 25, 169, 216));
		labelPresentation = new Label(groupOptionsTiming, SWT.NONE);
		labelPresentation.setBounds(new Rectangle(10, 26, 75, 15));
		labelPresentation.setText("Presentation:");
		labelInterspaceMax = new Label(groupOptionsTiming, SWT.NONE);
		labelInterspaceMax.setBounds(new Rectangle(10, 53, 91, 15));
		labelInterspaceMax.setText("Interspace max:");

		textPresentationTime = new Text(groupOptionsTiming, SWT.BORDER);
		textPresentationTime.setBounds(new Rectangle(107, 23, 58, 21));
		textPresentationTime
		.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				ProjectStarter.getConf().setValue("presentationTime",Integer.parseInt(textPresentationTime.getText()));
				updateConfiguration() ;
			}
		});
		textInterspaceMaxTime = new Text(groupOptionsTiming, SWT.BORDER);
		textInterspaceMaxTime.setBounds(new Rectangle(107, 48, 58, 21));
		textInterspaceMaxTime
		.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				updateTimes();
			}
		});


		labelInterspaceMin = new Label(groupOptionsTiming, SWT.NONE);
		labelInterspaceMin.setBounds(new Rectangle(10, 81, 90, 15));
		labelInterspaceMin.setText("Interspace min:");
		textInterspaceMinTime = new Text(groupOptionsTiming, SWT.BORDER);
		textInterspaceMinTime.setBounds(new Rectangle(107, 77, 57, 21));
		textInterspaceMinTime
		.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				updateTimes();
			}
		});
		labelRepeats = new Label(groupOptionsTiming, SWT.NONE);
		labelRepeats.setBounds(new Rectangle(10, 131, 66, 15));
		labelRepeats.setText("Repeats:");
		textRepeatsNumber = new Text(groupOptionsTiming, SWT.BORDER);
		textRepeatsNumber.setBounds(new Rectangle(107, 126, 57, 21));
		labelAlltimesInMs = new Label(groupOptionsTiming, SWT.NONE);
		labelAlltimesInMs.setBounds(new Rectangle(72, 103, 89, 15));
		labelAlltimesInMs.setText("All times in [ms]");
		labelWaitBeforeStart = new Label(groupOptionsTiming, SWT.NONE);
		labelWaitBeforeStart.setBounds(new Rectangle(10, 169, 74, 15));
		labelWaitBeforeStart.setText("Wait before:");
		labelWaitBetween = new Label(groupOptionsTiming, SWT.NONE);
		labelWaitBetween.setBounds(new Rectangle(10, 194, 80, 15));
		labelWaitBetween.setText("Wait between:");
		textWaitBefore = new Text(groupOptionsTiming, SWT.BORDER);
		textWaitBefore.setBounds(new Rectangle(107, 159, 57, 21));
		textWaitBefore.setText("300");
		textWaitBefore.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				ProjectStarter.getConf().setValue("waitBeforeTime",Integer.parseInt(textWaitBefore.getText()));
				updateConfiguration() ;
			}
		});
		textWaitBetween = new Text(groupOptionsTiming, SWT.BORDER);
		textWaitBetween.setBounds(new Rectangle(107, 188, 57, 21));
		textWaitBetween.setText("300");
		textWaitBetween.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				ProjectStarter.getConf().setValue("waitBetweenTime",Integer.parseInt(textWaitBetween.getText()));
				updateConfiguration() ;
			}
		});
		textRepeatsNumber
		.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				ProjectStarter.getConf().setValue("signalRepeats",Integer.parseInt(textRepeatsNumber.getText()));
				updateConfiguration() ;
			}
		});
	}

	public void updateDataFromConfiguration() {
		System.out.println("[ControlPanel] conf: "+ProjectStarter.getConf());

		int samplesPerSecond = 1000/ProjectStarter.getConf().getValueInt("samplingFrequency") ;

		labelStatData.setText("Stats: "+samplesPerSecond+" samples/s "+Configuration.getSourceAsString(ProjectStarter.getConf().getValueInt("signalSource"))+" "+ProjectStarter.getConf().getValueInt("numberOfChannels")+" channels | protocol "+ProjectStarter.getConf().getValueString("protocolType"));

		textPresentationTime.setText(""+ProjectStarter.getConf().getValueInt("presentationTime")) ;
		textInterspaceMaxTime.setText(""+ProjectStarter.getConf().getValueInt("interspaceTimeMax")) ;
		textInterspaceMinTime.setText(""+ProjectStarter.getConf().getValueInt("interspaceTimeMin")) ;
		textWaitBefore.setText(""+ProjectStarter.getConf().getValueInt("waitBeforeTime")) ;
		textWaitBetween.setText(""+ProjectStarter.getConf().getValueInt("waitBetweenTime")) ;
		textResponseSpan.setText(""+ProjectStarter.getConf().getValueInt("responseSpan")) ;
		textRepeatsNumber.setText(""+ProjectStarter.getConf().getValueInt("signalRepeats")) ;
		textOptionsDelay.setText(""+ProjectStarter.getConf().getValueInt("responseDelay")) ;
		textUserName.setText(""+ProjectStarter.getConf().getValueString("subjectName")) ;
		textSubjectSession.setText(""+ProjectStarter.getConf().getValueInt("sessionId")) ;

		textClassificationAvrageFrom.setText(""+ProjectStarter.getConf().getValueInt("classificationAvrageFrom"));
		textClassificationAvrageTo.setText(""+ProjectStarter.getConf().getValueInt("classificationAvrageTo"));

		if(comboSettingsSource!=null&&!comboSettingsSource.isDisposed()){
			comboSettingsSource.select(ProjectStarter.getConf().getValueInt("signalSource"));
			if(ProjectStarter.getConf().getValueInt("signalSource")==Configuration.SOURCE_COM){
				comboSettingsComPort.setEnabled(true);
			}
			int a ;
			if((a = comboSettingsComPort.indexOf(ProjectStarter.getConf().getValueString("ComPortIdentifier"))) !=-1)
				comboSettingsComPort.select(a) ;
		}
		if(comboSettingsProtocol!=null&&!comboSettingsProtocol.isDisposed()){
			int i = 0 ;
			for(String item: comboSettingsProtocol.getItems()){
				if(item.equalsIgnoreCase(ProjectStarter.getConf().getValueString("protocolType")))
					comboSettingsProtocol.select(i);
				i++ ;
			}

		}

		checkBoxApplyToInputs.setSelection(ProjectStarter.getConf().getValueInt("applySpatialFilterToInput")==1?true:false);


		if(textSettingsSelectFile!=null&&!textSettingsSelectFile.isDisposed())
			textSettingsSelectFile.setText(""+ProjectStarter.getConf().getValueString("recordedFile")) ;

		if(checkBoxOutputTcpip!=null&&!checkBoxOutputTcpip.isDisposed())
			checkBoxOutputTcpip.setSelection(ProjectStarter.getConf().getValueInt("startTcpIpServer")==1?true:false);

		if(checkBoxOutputFile!=null&&!checkBoxOutputFile.isDisposed())
			checkBoxOutputFile.setSelection(ProjectStarter.getConf().getValueInt("logClassificationToFile")==1?true:false);

		if(textSettingsTcpIpPort!=null&&!textSettingsTcpIpPort.isDisposed())
			textSettingsTcpIpPort.setText(""+ProjectStarter.getConf().getValueInt("tcpIpServerPort"));

		if(textSettingsTcpIpInputPort!=null&&!textSettingsTcpIpInputPort.isDisposed())
			textSettingsTcpIpInputPort.setText(""+ProjectStarter.getConf().getValueInt("tcpIpServerInputPort"));

		if(checkBoxTcpiIpInput!=null&&!checkBoxTcpiIpInput.isDisposed())
			checkBoxTcpiIpInput.setSelection(ProjectStarter.getConf().getValueInt("startTcpIpInputServer")==1?true:false);


		if(textSamplingFrequnecy!=null&&!textSamplingFrequnecy.isDisposed())
			textSamplingFrequnecy.setText(""+ProjectStarter.getConf().getValueInt("samplingFrequency"));

		if(textSettingsNumOfChannels!=null&&!textSettingsNumOfChannels.isDisposed())
			textSettingsNumOfChannels.setText(""+ProjectStarter.getConf().getValueInt("numberOfChannels"));

		switch(ProjectStarter.getConf().getValueInt("classificationType")){
		case Configuration.CLASSIFICATION_WINDOW:
			radioButtonClassification.setSelection(true);
			radioButtonClassificationWeights.setSelection(false);
			radioButtonClassificationAvgInSpan.setSelection(false);
			groupWeightsBased.setVisible(false);
			groupMaxValueInWindow.setVisible(true);
			groupClassificationAvrageInSpan.setVisible(false);
			break;

		case Configuration.CLASSIFICATION_WEIGHTS:
			radioButtonClassification.setSelection(false);
			radioButtonClassificationWeights.setSelection(true);
			radioButtonClassificationAvgInSpan.setSelection(false);
			groupWeightsBased.setVisible(true);
			groupMaxValueInWindow.setVisible(false);
			groupClassificationAvrageInSpan.setVisible(false);
			break;

		case Configuration.CLASSIFICATION_AVG_IN_WINDOW:
			radioButtonClassification.setSelection(false);
			radioButtonClassificationWeights.setSelection(false);
			radioButtonClassificationAvgInSpan.setSelection(true);
			groupWeightsBased.setVisible(false);
			groupMaxValueInWindow.setVisible(false);
			groupClassificationAvrageInSpan.setVisible(true);
			if(ProjectStarter.getConf().getValueString("classificationAvrageType").compareTo("max")==0){
				radioButtonClassificationAvrageMAx.setSelection(true);
				radioButtonClassificationAvrageMin.setSelection(false);
			}else{
				radioButtonClassificationAvrageMAx.setSelection(false);
				radioButtonClassificationAvrageMin.setSelection(true);
			}
			break;

		}
		tableClassificationTimeAndWeights.removeAll();
		for(final ClassificatorConfElem elem : ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true)){
			TableItem item = new TableItem (tableClassificationTimeAndWeights, SWT.NONE);
			item.setText(0,""+elem.span); item.setText(1,""+elem.weight);

			final TableEditor editor = new TableEditor(tableClassificationTimeAndWeights);
			final Button button = new Button(tableClassificationTimeAndWeights, SWT.NONE);
			button.setText("x");
			button.pack();
			button.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams"))
					.removeParam(elem);
					updateDataFromConfiguration();
				}
			});
			editor.minimumWidth = button.getSize().x;
			editor.horizontalAlignment = SWT.CENTER;
			editor.setEditor(button, item, 2);

			item.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					editor.getEditor().dispose();
					editor.dispose();
				}
			});

		}
		canvasWeightsGraph.redraw();

	}

	private boolean checkForConfUpdate = false ;
	private void updateConfiguration() {
		System.out.println("User changed configuration data - updating...");
		if(checkForConfUpdate)
			ProjectStarter.getConf().notifyListeners() ;
	}

	/**
	 * This method initializes groupOptionsOther	
	 *
	 */
	private void createGroupOptionsOther() {
		groupOptionsOther = new Group(groupOptions, SWT.NONE);
		groupOptionsOther.setLayout(null);
		groupOptionsOther.setText("Subject");
		groupOptionsOther.setBounds(new Rectangle(9, 242, 170, 81));

		labelUserName = new Label(groupOptionsOther, SWT.NONE);
		labelUserName.setBounds(new Rectangle(6, 36, 78, 15));
		labelUserName.setText("Subject name:");
		textUserName = new Text(groupOptionsOther, SWT.BORDER);
		textUserName.setBounds(new Rectangle(93, 30, 68, 21));
		textUserName.setText("test");
		labelSubjectSession = new Label(groupOptionsOther, SWT.NONE);
		labelSubjectSession.setBounds(new Rectangle(6, 61, 51, 15));
		labelSubjectSession.setText("Session:");
		textSubjectSession = new Text(groupOptionsOther, SWT.BORDER);
		textSubjectSession.setBounds(new Rectangle(93, 56, 68, 21));
		textSubjectSession.setText("0");
		textUserName.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				ProjectStarter.getConf().getElement("subjectName").setValue(textUserName.getText());
				updateConfiguration() ;
			}
		});

		textSubjectSession.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				ProjectStarter.getConf().getElement("sessionId").setValue(Integer.parseInt(textSubjectSession.getText()));
				updateConfiguration() ;
			}
		});

	}

	/**
	 * This method initializes sShellAbout	
	 *
	 */
	private void createSShellAbout() {
		sShellAbout = new Shell();
		sShellAbout.setLayout(null);
		sShellAbout.setText("About");
		sShellAbout.setSize(new Point(223, 232));
		labelAboutCaption = new Label(sShellAbout, SWT.NONE);
		labelAboutCaption.setBounds(new Rectangle(7, 9, 191, 27));
		labelAboutCaption.setFont(new Font(Display.getDefault(), "Segoe UI", 9, SWT.BOLD));
		labelAboutCaption.setText("jP300");
		labelAboutDescription = new Label(sShellAbout, SWT.WRAP);
		labelAboutDescription.setBounds(new Rectangle(7, 44, 191, 121));
		labelAboutDescription.setText("This program was made for purpose of my master thesis.\n\n author Bernard Kobos, 2008");
		buttonAboutExit = new Button(sShellAbout, SWT.NONE);
		buttonAboutExit.setBounds(new Rectangle(148, 167, 50, 25));
		buttonAboutExit.setText("Exit");
		buttonAboutExit
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				sShellAbout.close();
			}
		});
	}

	/**
	 * This method initializes sShellSettings	
	 *
	 */
	private void createSShellSettings() {
		sShellSettings = new Shell();
		sShellSettings.setLayout(null);
		sShellSettings.setText("General settings");
		createGroupSettingsSource();
		sShellSettings.setSize(new Point(371, 455));
		buttonSettingsOk = new Button(sShellSettings, SWT.NONE);
		buttonSettingsOk.setBounds(new Rectangle(284, 389, 41, 25));
		buttonSettingsOk.setText("Ok");
		createGroupSettingsOutput();
		createGroupMatrixDefinition();
		updateDataFromConfiguration();

		buttonSettingsOk
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("[ControLPanel,Settings] ok button source: "+comboSettingsSource.getSelectionIndex()); 
				ProjectStarter.getConf().setValue("signalSource",comboSettingsSource.getSelectionIndex());
				ProjectStarter.getConf().setValue("protocolType",comboSettingsProtocol.getText());
				ProjectStarter.getConf().setValue("recordedFile",textSettingsSelectFile.getText());

				if(checkBoxOutputFile.getSelection())
					ProjectStarter.getConf().setValue("logClassificationToFile",1);
				else
					ProjectStarter.getConf().setValue("logClassificationToFile",0);

				if(checkBoxOutputTcpip.getSelection())
					ProjectStarter.getConf().setValue("startTcpIpServer",1);
				else
					ProjectStarter.getConf().setValue("startTcpIpServer",0);

				ProjectStarter.getConf().setValue("tcpIpServerPort",Integer.parseInt(textSettingsTcpIpPort.getText()));

				ProjectStarter.getConf().setValue("samplingFrequency",Integer.parseInt(textSamplingFrequnecy.getText()));

				ProjectStarter.getConf().setValue("numberOfChannels",Integer.parseInt(textSettingsNumOfChannels.getText()));

				ProjectStarter.getConf().setValue("ComPortIdentifier",comboSettingsComPort.getText());

				if(checkBoxTcpiIpInput.getSelection())
					ProjectStarter.getConf().setValue("startTcpIpInputServer",1);
				else
					ProjectStarter.getConf().setValue("startTcpIpInputServer",0);
				updateDataFromConfiguration();
				updateConfiguration();
				sShellSettings.close();
			}
		});
	}

	/**
	 * This method initializes groupSettingsSource	
	 *
	 */
	private void createGroupSettingsSource() {
		groupSettingsSource = new Group(sShellSettings, SWT.NONE);
		groupSettingsSource.setLayout(null);
		groupSettingsSource.setText("Signal source");
		createComboSettingsSource();
		groupSettingsSource.setBounds(new Rectangle(5, 5, 344, 195));
		labelSettingsSource = new Label(groupSettingsSource, SWT.NONE);
		labelSettingsSource.setBounds(new Rectangle(11, 27, 111, 15));
		labelSettingsSource.setText("Select signal source:");
		buttonSettingsSelectFile = new Button(groupSettingsSource, SWT.NONE);
		buttonSettingsSelectFile.setBounds(new Rectangle(215, 106, 87, 25));
		buttonSettingsSelectFile.setText("browse...");
		buttonSettingsSelectFile
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("[ControlPanel] BROWSE FILE TO LOAD"); 
				FileDialog dialog = new FileDialog(sShellSettings, SWT.OPEN);
				dialog
				.setFilterNames(new String[] { "jP300 files", "All Files (*.*)" });
				dialog.setFilterExtensions(new String[] { "*.jp300", "*.*" }); 
				String text = dialog.open();
				if(text!=null)
					textSettingsSelectFile.setText(text);
			}
		});
		textSettingsSelectFile = new Text(groupSettingsSource, SWT.BORDER);
		textSettingsSelectFile.setBounds(new Rectangle(11, 106, 194, 25));
		labelSourceProtocol = new Label(groupSettingsSource, SWT.NONE);
		labelSourceProtocol.setBounds(new Rectangle(10, 71, 112, 15));
		labelSourceProtocol.setText("Protocol:");
		createComboSettingsProtocol();
		labelSamplingFreqnecy = new Label(groupSettingsSource, SWT.NONE);
		labelSamplingFreqnecy.setBounds(new Rectangle(16, 147, 126, 15));
		labelSamplingFreqnecy.setText("Sampling frequency:");
		textSamplingFrequnecy = new Text(groupSettingsSource, SWT.BORDER);
		textSamplingFrequnecy.setBounds(new Rectangle(226, 142, 60, 21));
		textSamplingFrequnecy.setText("200");
		labelSettingsHerz = new Label(groupSettingsSource, SWT.NONE);
		labelSettingsHerz.setBounds(new Rectangle(291, 147, 21, 15));
		labelSettingsHerz.setText("Hz");
		createComboSettingsComPort();
		labelSettingsChannels = new Label(groupSettingsSource, SWT.NONE);
		labelSettingsChannels.setBounds(new Rectangle(16, 169, 116, 15));
		labelSettingsChannels.setText("Number of channels:");
		textSettingsNumOfChannels = new Text(groupSettingsSource, SWT.BORDER);
		textSettingsNumOfChannels.setBounds(new Rectangle(225, 167, 60, 21));
		textSettingsNumOfChannels.setText("6");
	}

	/**
	 * This method initializes comboSettingsSource	
	 *
	 */
	private void createComboSettingsSource() {
		comboSettingsSource = new Combo(groupSettingsSource, SWT.NONE);
		comboSettingsSource.setText("random");
		comboSettingsSource.setBounds(new Rectangle(131, 21, 107, 23));
		comboSettingsSource.add("random");
		comboSettingsSource.add("hard drive");
		comboSettingsSource.add("USB/COM");
		comboSettingsSource.select(2);

		comboSettingsSource.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(comboSettingsSource.getSelectionIndex()==Configuration.SOURCE_COM)
					comboSettingsComPort.setEnabled(true) ;
			}
		});
	}

	/**
	 * This method initializes groupSettingsOutput	
	 *
	 */
	private void createGroupSettingsOutput() {
		groupSettingsOutput = new Group(sShellSettings, SWT.NONE);
		groupSettingsOutput.setLayout(null);
		groupSettingsOutput.setText("Classification output");
		groupSettingsOutput.setBounds(new Rectangle(6, 295, 340, 88));
		checkBoxOutputFile = new Button(groupSettingsOutput, SWT.CHECK);
		checkBoxOutputFile.setBounds(new Rectangle(11, 23, 81, 16));
		checkBoxOutputFile.setText("log to file");
		checkBoxOutputTcpip = new Button(groupSettingsOutput, SWT.CHECK);
		checkBoxOutputTcpip.setBounds(new Rectangle(11, 49, 132, 16));
		checkBoxOutputTcpip.setText("TCP/IP server on port");
		textSettingsTcpIpPort = new Text(groupSettingsOutput, SWT.BORDER);
		textSettingsTcpIpPort.setBounds(new Rectangle(146, 46, 52, 21));
		textSettingsTcpIpPort.setText("1984");
	}

	/**
	 * This method initializes comboSettingsProtocol	
	 *
	 */
	private void createComboSettingsProtocol() {
		comboSettingsProtocol = new Combo(groupSettingsSource, SWT.NONE);
		comboSettingsProtocol.setBounds(new Rectangle(195, 61, 112, 23));
		for(IProtocol proto:  ProtocolFactory.getProtocols()){
			if(proto.getType()!=null)
				comboSettingsProtocol.add(proto.getType());
		}
		comboSettingsProtocol.select(0);

	}

	/**
	 * This method initializes tabFolder	
	 *
	 */
	private void createTabFolder() {
		tabFolder = new TabFolder(sShell, SWT.NONE);

		GridData gridData = new GridData();
		gridData.widthHint = 487;
		gridData.heightHint = 131;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2 ;
		tabFolder.setLayoutData(gridData);

		createCompositeFreqFilters();
		createCompositeOutput();
		tabFolder.setBounds(new Rectangle(349, 409, 487, 131));
		TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
		tabItem2.setText("Output");
		tabItem2.setControl(compositeOutput);
		TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
		tabItem.setText("Freq filters");
		tabItem.setControl(compositeFreqFilters);		
	}

	/**
	 * This method initializes compositeFreqFilters	
	 *
	 */
	Button[] freqFiltersCheckBoxes ;
	Button[] freqFiltersEdit ;
	private void createCompositeFreqFilters() {
		// createSShellFreqFilterEdit
		compositeFreqFilters = new Composite(tabFolder, SWT.NONE);
		compositeFreqFilters.setLayout(null);
		buttonAddFreqencyFilter = new Button(compositeFreqFilters, SWT.NONE);
		buttonAddFreqencyFilter.setBounds(new Rectangle(459, 83, 15, 15));
		buttonAddFreqencyFilter.setText("+");
		buttonFreqFiltersLoad = new Button(compositeFreqFilters, SWT.NONE);
		buttonFreqFiltersLoad.setBounds(new Rectangle(407, 83, 46, 15));
		buttonFreqFiltersLoad.setText("load...");
		buttonFreqFilterSave = new Button(compositeFreqFilters, SWT.NONE);
		buttonFreqFilterSave.setBounds(new Rectangle(325, 83, 80, 15));
		buttonFreqFilterSave.setText("save...");
		buttonFreqFilterSave
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog fd = new FileDialog(sShell, SWT.SAVE);
				fd.setText("Save");
				fd.setFileName("conf_freq.txt");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Writing "+selected+" to file ");
					ConfigurationLoader.save(selected,IConfigurationElement.FREQFILTER);
				}
			}
		});
		buttonFreqFiltersLoad
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("widgetSelected() TODO1"); // TODO Auto-generated Event stub widgetSelected()
				FileDialog fd = new FileDialog(sShell, SWT.OPEN);
				fd.setText("Open");
				fd.setFileName("conf_freq.txt");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Opening "+selected);
					ConfigurationLoader.loadMerge(selected);
					updateFreqFilters();
					updateConfiguration() ;
				}
			}
		});
		buttonAddFreqencyFilter
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				createSShellFreqFilterEdit() ;
				sShellFreqFilterEdit.open();
				int i = 0 ;
				String text = "New filter name" ;
				while(ProjectStarter.getConf().getElement(text+" "+i)!=null)
					i++ ;
				textEditFreqFilterName.setText(text+" "+i);
				sShellFreqFilterEdit.setData("createNew",new Boolean(true));
			}
		});
		updateFreqFilters() ;
	}

	private void updateFreqFilters() {
		if(freqFiltersCheckBoxes!=null){
			for(int i=0;i<freqFiltersCheckBoxes.length;i++){
				freqFiltersCheckBoxes[i].dispose();
				freqFiltersEdit[i].dispose();
			}
		}
		freqFiltersCheckBoxes = new Button[ProjectStarter.getConf().getFilters().length];
		freqFiltersEdit = new Button[ProjectStarter.getConf().getFilters().length];
		System.out.println("[ControlPanel] creating checkboxes for "+freqFiltersCheckBoxes.length+" filters");
		int i = 0 ;
		int ix = 0 ;
		int iy = 0 ;
		int stepX = 250 ;
		for(final ElementFrequnecyFilter elem : ProjectStarter.getConf().getFilters()){
			if(i!=0&&i%5==0){
				ix++ ;
				iy = 0 ;
			}
			Button btn1 = new Button(compositeFreqFilters, SWT.NONE);
			btn1.setBounds(new Rectangle(5+ix*stepX, 13+16*iy, 16, 15));
			btn1.setText("e");
			btn1.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					createSShellFreqFilterEdit();
					sShellFreqFilterEdit.open() ;
					textEditFreqFilterName.setText(elem.getName()) ;
					textAreaEditFreqFilterCoeffA.setText(elem.getValuesAsString(ElementFrequnecyFilter.COEFF_A)) ;
					textAreaEditFrexFilterCoeffB.setText(elem.getValuesAsString(ElementFrequnecyFilter.COEFF_B)) ;
					sShellFreqFilterEdit.setData("editedElem",elem.getName());
				}
			});
			freqFiltersEdit[i] = btn1 ;

			Button btn = new Button(compositeFreqFilters, SWT.CHECK);
			btn.setBounds(new Rectangle(21+ix*stepX, 13+16*iy, 184, 16));
			btn.setText(elem.getName());
			boolean selection = elem.getValue()==1?true:false ;
			btn.setSelection(selection);
			btn.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					if(((Button)e.widget).getSelection())
						ProjectStarter.getConf().setValue(((Button)e.widget).getText(),1);
					else
						ProjectStarter.getConf().setValue(((Button)e.widget).getText(),0);
					//					System.out.println("[ControLPanel,Settings]  source: "+conf.getValueInt("signalSource")); 
					updateConfiguration();
				}
			});
			freqFiltersCheckBoxes[i++] = btn ;
			iy++;
		}
	}

	/**
	 * This method initializes compositeOutput	
	 *
	 */
	private void createCompositeOutput() {
		compositeOutput = new Composite(tabFolder, SWT.NONE);
		compositeOutput.setLayout(null);
		textOutput = new Text(compositeOutput, SWT.BORDER);
		textOutput.setText("");
		textOutput.setBounds(new Rectangle(6, 10, 456, 21));
		textOutput.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				textOutput.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE)) ;
			}
		});
	}

	private void initializeCalibration(){
		int chanNum = ProjectStarter.getConf().getValueInt("numberOfChannels");//.getChannelsNumber() ;
		float valPerElecrode = 0.0f ;


		ElementChannelsCalibration weightsConf = (ElementChannelsCalibration)ProjectStarter.getConf().getElement("Channels calibration"); 
		// if there is not enough coefficients in config file - add default weight
		for(int row =0 ; row <chanNum ; row++ )
			for(int i=0;i<chanNum;i++)
				weightsConf.setIfEmpty(chanNum,1,0) ;

		tableCalibration.removeAll();
		for (int i = 0; i < 3; i++) {
			new TableColumn(tableCalibration, SWT.NONE);
		}
		for (int row = 0; row < chanNum+1; row++) {
			if(row==0){
				TableItem item = new TableItem(tableCalibration, SWT.NONE);
				item.setText(1, "Gain ");
				item.setText(2, "Offset ");
			}else{
				double[] rowData = weightsConf.getChan(row-1);
				TableItem item = new TableItem(tableCalibration, SWT.NONE);
				item.setText(0, "Chan "+(row-1) );
				for(int a = 0 ; a < 2 ; a++)
					item.setText(a+1,""+rowData[a]);
			}
		}
		for (int i = 0; i < 3; i++) {
			tableCalibration.getColumn(i).pack();
			tableCalibration.getColumn(i).setResizable(true);
		}


		final TableEditor editor = new TableEditor(tableCalibration);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		tableCalibration.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = tableCalibration.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = tableCalibration.getTopIndex();
				while (index < tableCalibration.getItemCount()) {
					boolean visible = false;
					final TableItem item = tableCalibration.getItem(index);
					for (int i = 0; i < tableCalibration.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)&&index!=0&&i!=0) {
							final int column = i;
							final int row = index;
							final Text text = new Text(tableCalibration, SWT.NONE);
							Listener textListener = new Listener() {
								public void handleEvent(final Event e) {
									switch (e.type) {
									case SWT.FocusOut:
										item.setText(column, text.getText());
										System.out.println("TEXT ZMIENIONY NA "+text.getText()+" x = "+column+" y= "+row);
										((ElementChannelsCalibration)ProjectStarter.getConf().getElement("Channels calibration")).setValue(row-1,column-1,Double.parseDouble( text.getText()));
										text.dispose();
										updateConfiguration();
										break;
									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											item.setText(column, text.getText());
											((ElementChannelsCalibration)ProjectStarter.getConf().getElement("Channels calibration")).setValue(row-1,column-1,Double.parseDouble( text.getText()));
											text.dispose();
											updateConfiguration();
											//FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											text.dispose();
											e.doit = false;
										}
										break;
									}
								}
							};
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							editor.setEditor(text, item, i);
							text.setText(item.getText(i));
							text.selectAll();
							text.setFocus();
							return;
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
			}
		});
	}
	/**
	 * This method initializes groupDimensionalCoefficients	
	 *
	 */
	private void initializeDimmFilters(){
		int chanNum = ProjectStarter.getConf().getValueInt("numberOfChannels");
		float valPerElecrode = 0.0f ;

		ElementChannelsWeights weightsConf = (ElementChannelsWeights)ProjectStarter.getConf().getElement("Channels weights"); 
		System.out.println("[ControlPanel] Number of channels : "+chanNum+" number of weights in config: "+weightsConf.getMatrixDimensions().x + "*"  +weightsConf.getMatrixDimensions().y) ;
		// if there is not enough coefficients in config file - add default weight
		for(int row =0 ; row <chanNum ; row++ )
			for(int i=0;i<chanNum;i++)
				weightsConf.setIfEmpty(i,row, valPerElecrode) ;

		System.out.println("[ControlPanel] Number of channels : "+chanNum+" number of weights in config: "+weightsConf.getMatrixDimensions().x + "*"  +weightsConf.getMatrixDimensions().y) ;
		tableSpatialFilters.removeAll();

		for (int i = 0; i < chanNum+1; i++) {
			new TableColumn(tableSpatialFilters, SWT.NONE);
		}

		for (int row = 0; row < chanNum+1; row++) {
			if(row==0){
				TableItem item = new TableItem(tableSpatialFilters, SWT.NONE);
				for(int a=0;a<chanNum+1;a++){
					item.setText(a+1, "Chan "+a);
				}
			}else{
				Double[] rowData = weightsConf.getRow(row-1);
				TableItem item = new TableItem(tableSpatialFilters, SWT.NONE);
				item.setText(0, "Chan "+(row-1) );
				for(int a = 0 ; a < chanNum ; a++)
					item.setText(a+1,""+rowData[a]);
			}
		}
		for (int i = 0; i < chanNum+1; i++) {
			tableSpatialFilters.getColumn(i).pack();
			tableSpatialFilters.getColumn(i).setResizable(true);
		}


		final TableEditor editor = new TableEditor(tableSpatialFilters);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		tableSpatialFilters.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = tableSpatialFilters.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = tableSpatialFilters.getTopIndex();
				while (index < tableSpatialFilters.getItemCount()) {
					boolean visible = false;
					final TableItem item = tableSpatialFilters.getItem(index);
					for (int i = 0; i < tableSpatialFilters.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)&&index!=0&&i!=0) {
							final int column = i;
							final int row = index;
							final Text text = new Text(tableSpatialFilters, SWT.NONE);
							Listener textListener = new Listener() {
								public void handleEvent(final Event e) {
									switch (e.type) {
									case SWT.FocusOut:
										item.setText(column, text.getText());
										System.out.println("TEXT ZMIENIONY NA "+text.getText()+" x = "+column+" y= "+row);
										((ElementChannelsWeights)ProjectStarter.getConf().getElement("Channels weights")).setValue(row-1,column-1,Double.parseDouble( text.getText()));
										text.dispose();
										updateConfiguration();
										break;
									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											item.setText(column, text.getText());
											//FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											text.dispose();
											e.doit = false;
										}
										break;
									}
								}
							};
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							editor.setEditor(text, item, i);
							text.setText(item.getText(i));
							text.selectAll();
							text.setFocus();
							return;
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
			}
		});
	}

	public void initialize(){
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if(!isDisposed()){
					initializeDimmFilters(); 
					initializeCheckBoxesChannelVisible();
					ProjectStarter.getConf().notifyListeners();
					updateCheckBoxesPosition();
				}
			}
		});

	}

	/**
	 * This method initializes tabFolderCanvases	
	 *
	 */
	private void createTabFolderCanvases() {
		tabFolderCanvases = new TabFolder(sShell, SWT.NONE);
		tabFolderCanvases.setLocation(new Point(5, 25));
		tabFolderCanvases.setSize(new Point(500, 450));
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.widthHint = 638 ;
		gridData.heightHint = 379 ;
		gridData.grabExcessHorizontalSpace = true;
		tabFolderCanvases.setLayoutData(gridData);

		Listener listener = new Listener () {
			public void handleEvent (Event e) {
				if(ProjectStarter.getControlPanel()!=null){				      
					Rectangle my = new Rectangle( 0, tabFolderCanvases.getBounds().height-40, 
							tabFolderCanvases.getBounds().width, 14);
					canvasRawEegDataScale.setBounds(my);
					ProjectStarter.getControlPanel().updateCheckBoxesPosition() ;
				}
			}
		};

		tabFolderCanvases.addListener (SWT.Resize, listener);
		createCanvasRawEegData();
		createCanvasRsquare();
		createCompositeClassification();
		createCompositeSpatialFilters();
		createCompositeOptions();
		createCompositeDisplay();
		TabItem tabItem3 = new TabItem(tabFolderCanvases, SWT.NONE);
		tabItem3.setText("Main panel");


		TabItem tabItem3a = new TabItem(tabFolderCanvases, SWT.NONE);
		tabItem3a.setText("EEG signals");
		tabItem3a.setControl(canvasRawEegData);
		tabItem3.setControl(canvasRawEegData);

		TabItem tabItem4 = new TabItem(tabFolderCanvases, SWT.NONE);
		tabItem4.setText("R-square");
		tabItem4.setControl(canvasRsquare);
		TabItem tabItem5 = new TabItem(tabFolderCanvases, SWT.NONE);
		tabItem5.setText("Classification");
		tabItem5.setControl(compositeClassification);
		TabItem tabItem6 = new TabItem(tabFolderCanvases, SWT.NONE);
		tabItem6.setText("Spatial filters");
		tabItem6.setControl(compositeSpatialFilters);

		TabItem tabItem7 = new TabItem(tabFolderCanvases, SWT.NONE);
		tabItem7.setText("Options");
		tabItem7.setControl(compositeOptions);

		TabItem tabItem2 = new TabItem(tabFolderCanvases, SWT.NONE);
		tabItem2.setText("Display");
		tabItem2.setControl(compositeDisplay);

		tabFolderCanvases.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("widgetSelected() - TABS "+((TabItem)e.item).getText());

				//RESET EVERYTHING
				groupUserMatrix.setParent(sShell);
				textOutput.setParent(compositeOutput);
				textOutput.setBounds(new Rectangle(6, 10, 456, 21));

				groupUserMatrix.moveAbove(tabFolder);
				GridData grid = new GridData();
				groupOptions.setLayoutData(grid);

				GridData gridData = new GridData();
				gridData.widthHint = 333;
				gridData.heightHint = 295;
				gridData.verticalSpan = 2 ;
				gridData.horizontalAlignment = SWT.LEFT;
				groupUserMatrix.setLayoutData(gridData);

				gridData = new GridData();
				gridData.widthHint = 492;
				gridData.heightHint = 165;
				gridData.horizontalAlignment = GridData.FILL;
				gridData.grabExcessHorizontalSpace = true;
				gridData.horizontalSpan = 2 ;
				groupMatrixResponses.setLayoutData(gridData);

				gridData = new GridData();
				gridData.widthHint = 487;
				gridData.heightHint = 131;
				gridData.horizontalAlignment = GridData.FILL;
				gridData.grabExcessHorizontalSpace = true;
				gridData.horizontalSpan = 2 ;
				tabFolder.setLayoutData(gridData);

				groupOptions.setVisible(true);
				groupUserMatrix.setVisible(true);
				groupMatrixResponses.setVisible(true);
				tabFolder.setVisible(true);


				if(((TabItem)e.item).getText().equalsIgnoreCase("R-square")){
					// TURN OFF EVERYTHING EXCEPT RSQUARE	
					grid = new GridData();
					grid.exclude = true ;
					groupOptions.setLayoutData(grid);
					groupUserMatrix.setLayoutData(grid);
					groupMatrixResponses.setLayoutData(grid);
					tabFolder.setLayoutData(grid);

					groupOptions.setVisible(false);
					groupUserMatrix.setVisible(false);
					groupMatrixResponses.setVisible(false);
					tabFolder.setVisible(false);

				}else if(((TabItem)e.item).getText().equalsIgnoreCase("EEG signals")){
					groupUserMatrix.setParent(sShell);
					grid = new GridData();
					grid.exclude = true ;
					groupOptions.setLayoutData(grid);
					groupUserMatrix.setLayoutData(grid);
					groupMatrixResponses.setLayoutData(grid);
					tabFolder.setLayoutData(grid);

					groupOptions.setVisible(false);
					groupUserMatrix.setVisible(false);
					groupMatrixResponses.setVisible(false);
					tabFolder.setVisible(false);
					//
				}else if(((TabItem)e.item).getText().equalsIgnoreCase("Display")){
					grid = new GridData();
					grid.exclude = true ;
					groupMatrixResponses.setLayoutData(grid);
					tabFolder.setLayoutData(grid);
					groupMatrixResponses.setVisible(false);
					tabFolder.setVisible(false);
					textOutput.setParent(compositeDisplay);
					gridData = new GridData();
					gridData.widthHint = 500;
					gridData.heightHint = 21;
					gridData.horizontalAlignment = GridData.FILL;
					gridData.grabExcessHorizontalSpace = true;
					textOutput.setLayoutData(gridData);

					groupUserMatrix.setParent(compositeDisplay);
					gridData = new GridData();
					Point largeSize = ((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).getLargeSize() ;
					if(largeSize.x!=0){
						gridData.heightHint = largeSize.y ;
						gridData.widthHint = largeSize.x ;
					}else{
						gridData.widthHint = 333;
						gridData.heightHint = 295;
					}
					gridData.horizontalAlignment = GridData.CENTER;
					gridData.verticalAlignment = GridData.CENTER;
					groupUserMatrix.setLayoutData(gridData);
				}else{


				}
				boolean maximized = sShell.getMaximized() ;
				Point size = sShell.getSize() ;
				sShell.pack();
				if(maximized)
					sShell.setMaximized(maximized);
				else
					sShell.setSize(size);
			}
		});

	}

	/**
	 * This method initializes canvasRawEegData	
	 *
	 */
	private Button[] checkBoxesChannelVisible ;
	private Composite compositeOptions;
	private Table tableCalibration;
	private Button buttonLoadCalibration;
	private Button buttonSaveCalibration;
	private Button checkboxModifyCalibration;
	private Group groupMatrixDisplayOptions;
	private Color matrixBkgColor;
	private Color normalColor;
	private Color activeColor;
	private Text textFieldHorizontalMargin;
	private Text textFieldVerticalMargin;
	private Text textFieldFontSize;

	public boolean getCheckBoxVisibleValue(int channelNumber){
		return checkBoxesChannelVisible[channelNumber].getSelection() ;
	}

	public boolean[] getCheckBoxesVisibleValues(){
		boolean out[] = new boolean[checkBoxesChannelVisible.length];
		for(int i =0 ; i<out.length;i++)
			out[i] = getCheckBoxVisibleValue(i);
		return out ;
	}

	public void updateCheckBoxesPosition(){
		int numberOfChans = ProjectStarter.getConf().getValueInt("numberOfChannels") ;
		int canvasHeight = tabFolderCanvases.getBounds().height-8 ;
		int stepY = canvasHeight/numberOfChans ;
		for(int i=0;i<numberOfChans;i++){
			checkBoxesChannelVisible[i].setLocation(tabFolderCanvases.getBounds().width-40,6+stepY*i);
		}
		buttonMaginifierPlus.setLocation(4, tabFolderCanvases.getBounds().height-60);
		buttonMagnifierMinus.setLocation(36, tabFolderCanvases.getBounds().height-60);
		buttonMagnifySignalAmpPlus.setLocation(tabFolderCanvases.getBounds().width-60,6);
		buttonMagnifySignalAmpMinux.setLocation(tabFolderCanvases.getBounds().width-20, 6);
	}

	private void initializeCheckBoxesChannelVisible(){
		int numberOfChans = ProjectStarter.getConf().getValueInt("numberOfChannels") ;
		checkBoxesChannelVisible = new Button[numberOfChans] ;
		int canvasHeight = tabFolderCanvases.getBounds().height-8 ;
		int stepY = canvasHeight/numberOfChans ;
		for(int i=0;i<numberOfChans;i++){
			Button btn = new Button(canvasRawEegData, SWT.CHECK);
			btn.setSelection(true);
			if(!((ElementChannelsVisibility) ProjectStarter.getConf().getElement("Channels visibility")).hasValue(i))
				btn.setSelection(false);
			btn.setBounds(new Rectangle(604, 6+stepY*i, 10, 10));
			btn.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
					updateConfiguration() ;
				}
			});
			checkBoxesChannelVisible[i] = btn ;
		}
	}

	private void createCanvasRawEegData() {
		canvasRawEegData = new RawEegCanvas(tabFolderCanvases, SWT.NO_BACKGROUND);
		buttonMaginifierPlus = new Button(canvasRawEegData, SWT.NONE);
		buttonMaginifierPlus.setBounds(new Rectangle(4, 307, 27, 25));
		buttonMaginifierPlus.setText("-");
		buttonMaginifierPlus
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("widgetSelected() - magnify PLUS");
				int newval = ProjectStarter.getConf().getValueInt("scaleFactorX") + 1;
				ProjectStarter.getConf().setValue("scaleFactorX",newval);
				updateConfiguration() ;
			}
		});
		buttonMagnifierMinus = new Button(canvasRawEegData, SWT.NONE);
		buttonMagnifierMinus.setBounds(new Rectangle(36, 307, 27, 25));
		buttonMagnifierMinus.setText("+");
		createCanvasRawEegDataScale();
		checkBoxChannelVisible = new Button(canvasRawEegData, SWT.CHECK);
		checkBoxChannelVisible.setBounds(new Rectangle(604, 6, 10, 10));
		checkBoxChannelVisible.setVisible(false);
		buttonMagnifySignalAmpPlus = new Button(canvasRawEegData, SWT.NONE);
		buttonMagnifySignalAmpPlus.setBounds(new Rectangle(618, 6, 10, 10));
		buttonMagnifySignalAmpPlus.setFont(new Font(Display.getDefault(), "Segoe UI", 8, SWT.NORMAL));
		buttonMagnifySignalAmpPlus.setText("+");
		buttonMagnifySignalAmpPlus
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				canvasRawEegData.setScaleY(canvasRawEegData.getScaleY()+1);
			}
		});
		buttonMagnifySignalAmpMinux = new Button(canvasRawEegData, SWT.NONE);
		buttonMagnifySignalAmpMinux.setBounds(new Rectangle(588, 6, 10, 10));
		buttonMagnifySignalAmpMinux.setFont(new Font(Display.getDefault(), "Segoe UI", 8, SWT.NORMAL));
		buttonMagnifySignalAmpMinux.setText("-");
		buttonMagnifySignalAmpMinux
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("AMPLITUDEDOWN widgetSelected()"); 
				canvasRawEegData.setScaleY(canvasRawEegData.getScaleY()-1);
			}
		});
		buttonMagnifierMinus
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				System.out.println("widgetSelected() - magnify MINUS"); 
				int newval = ProjectStarter.getConf().getValueInt("scaleFactorX") - 1;
				if(newval<0)
					newval = 0 ;
				ProjectStarter.getConf().setValue("scaleFactorX",newval);
				updateConfiguration() ;
			}
		});
		canvasRawEegData.addMyPaintListener() ;
	}

	/**
	 * This method initializes canvasRsquare	
	 *
	 */
	private void createCanvasRsquare() {
		canvasRsquare = new RsquareCanvas(tabFolderCanvases,SWT.NO_BACKGROUND);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		canvasRsquare.setLayout(gridLayout);
		((RsquareCanvas) canvasRsquare).addMyPaintListener() ;
	}

	/**
	 * This method initializes groupMatrixDefinition	
	 *
	 */
	private void createGroupMatrixDefinition() {
		groupMatrixDefinition = new Group(sShellSettings, SWT.NONE);
		groupMatrixDefinition.setLayout(null);
		groupMatrixDefinition.setText("Matrix definition");
		groupMatrixDefinition.setBounds(new Rectangle(5, 212, 342, 79));
		checkBoxTcpiIpInput = new Button(groupMatrixDefinition, SWT.CHECK);
		checkBoxTcpiIpInput.setBounds(new Rectangle(15, 40, 134, 16));
		checkBoxTcpiIpInput.setText("TCP/IP input on port");
		textSettingsTcpIpInputPort = new Text(groupMatrixDefinition, SWT.BORDER);
		textSettingsTcpIpInputPort.setBounds(new Rectangle(150, 36, 56, 21));
		textSettingsTcpIpInputPort.setText("1986");
	}
	/**
	 * This method initializes canvasRawEegDataScale	
	 *
	 */
	private void createCanvasRawEegDataScale() {
		canvasRawEegDataScale = new RawEegDataCanvasScale(canvasRawEegData, SWT.NONE);
		canvasRawEegDataScale.setBounds(new Rectangle(0, 337, 630, 14));
	}
	/**
	 * This method initializes comboSettingsComPort	
	 *
	 */
	private void createComboSettingsComPort() {
		comboSettingsComPort = new Combo(groupSettingsSource, SWT.NONE);
		comboSettingsComPort.setEnabled(false);
		comboSettingsComPort.setBounds(new Rectangle(247, 21, 91, 23));
		Enumeration ports = CommPortIdentifier.getPortIdentifiers();       
		while(ports.hasMoreElements())
			comboSettingsComPort.add(((CommPortIdentifier)ports.nextElement()).getName()) ;
	}
	/**
	 * This method initializes compositeClassification	
	 *
	 */
	private void createCompositeClassification() {
		compositeClassification = new Composite(tabFolderCanvases, SWT.NONE);
		compositeClassification.setLayout(null);


		createGroupClassificationOptions();
		createGroupWeightsBased();
		createGroupMaxValueInWindow();
		createGroupClassificationAvrageInSpan();

	}
	/**
	 * This method initializes canvasWeightsGraph	
	 *
	 */
	private void createCanvasWeightsGraph() {
		canvasWeightsGraph = new WeightsGraphCanvas(groupWeightsBased, SWT.NONE);
		canvasWeightsGraph.setBounds(new Rectangle(17, 21, 405, 193));
		((WeightsGraphCanvas) canvasWeightsGraph).addMyPaintListener();
	}
	/**
	 * This method initializes groupClassificationOptions	
	 *
	 */
	private void createGroupClassificationOptions() {
		groupClassificationOptions = new Group(compositeClassification, SWT.NONE);
		groupClassificationOptions.setLayout(null);
		groupClassificationOptions.setText("Classification method");
		groupClassificationOptions.setBounds(new Rectangle(14, 14, 609, 74));

		radioButtonClassification = new Button(groupClassificationOptions, SWT.RADIO);
		radioButtonClassification.setBounds(new Rectangle(27, 18, 176, 16));
		radioButtonClassification.setText("max value in window");
		radioButtonClassification
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				ProjectStarter.getConf().setValue("classificationType",Configuration.CLASSIFICATION_WINDOW);
				updateDataFromConfiguration();
			}
		});
		radioButtonClassificationWeights = new Button(groupClassificationOptions, SWT.RADIO);
		radioButtonClassificationWeights.setBounds(new Rectangle(27
				, 40, 176, 16));
		radioButtonClassificationWeights.setSelection(true);
		radioButtonClassificationWeights.setText("weights based");
		radioButtonClassificationAvgInSpan = new Button(groupClassificationOptions, SWT.RADIO);
		radioButtonClassificationAvgInSpan.setBounds(new Rectangle(215, 19, 107, 16));
		radioButtonClassificationAvgInSpan.setText("avrage in span");
		radioButtonClassificationAvgInSpan.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				ProjectStarter.getConf().setValue("classificationType",Configuration.CLASSIFICATION_AVG_IN_WINDOW);
				updateDataFromConfiguration();
			}
		});
		radioButtonClassificationWeights
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				ProjectStarter.getConf().setValue("classificationType",Configuration.CLASSIFICATION_WEIGHTS);
				updateDataFromConfiguration();
			}
		});
		radioButtonClassificationAvrageMin = new Button(groupClassificationOptions, SWT.RADIO);
		radioButtonClassificationAvrageMin.setBounds(new Rectangle(487, 38, 113, 16));
		radioButtonClassificationAvrageMin.setText("minimum");
		radioButtonClassificationAvrageMin.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				ProjectStarter.getConf().setValue("classificationAvrageType","min");
				updateDataFromConfiguration();
			}
		});
		radioButtonClassificationAvrageMAx = new Button(groupClassificationOptions, SWT.RADIO);
		radioButtonClassificationAvrageMAx.setBounds(new Rectangle(487, 18, 101, 16));
		radioButtonClassificationAvrageMAx.setText("maximum");
		radioButtonClassificationAvrageMAx.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				ProjectStarter.getConf().setValue("classificationAvrageType","max");
				updateDataFromConfiguration();
			}
		});
	}
	/**
	 * This method initializes groupWeightsBased	
	 *
	 */
	private void createGroupWeightsBased() {
		groupWeightsBased = new Group(compositeClassification, SWT.NONE);
		groupWeightsBased.setLayout(null);
		groupWeightsBased.setText("weights based");
		groupWeightsBased.setVisible(false);
		groupWeightsBased.setBounds(new Rectangle(14, 95, 610, 251));

		createCanvasWeightsGraph();
		tableClassificationTimeAndWeights = new Table(groupWeightsBased, SWT.NONE);
		tableClassificationTimeAndWeights.setHeaderVisible(true);
		tableClassificationTimeAndWeights.setLinesVisible(true);
		tableClassificationTimeAndWeights.setBounds(new Rectangle(417, 23, 185, 192));
		buttonClassificationAddNewWeight = new Button(groupWeightsBased, SWT.NONE);
		buttonClassificationAddNewWeight.setBounds(new Rectangle(510, 219, 94, 25));
		buttonClassificationAddNewWeight.setText("add new weight");
		buttonClassificationWeightsLoad = new Button(groupWeightsBased, SWT.NONE);
		buttonClassificationWeightsLoad.setBounds(new Rectangle(432, 219, 70, 25));
		buttonClassificationWeightsLoad.setText("load...");
		buttonClassificationWeightsSave = new Button(groupWeightsBased, SWT.NONE);
		buttonClassificationWeightsSave.setBounds(new Rectangle(361, 219, 68, 25));
		buttonClassificationWeightsSave.setText("save...");
		buttonClassificationWeightsSave
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog fd = new FileDialog(sShell, SWT.SAVE);
				fd.setText("Save");
				fd.setFileName("conf_classificatorWeights.txt");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Writing "+selected+" to file ");
					ConfigurationLoader.save(selected,IConfigurationElement.CLASSIFICATOR_CONF);
				}
			}
		});
		buttonClassificationWeightsLoad
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog fd = new FileDialog(sShell, SWT.OPEN);
				fd.setText("Open");
				fd.setFileName("conf_classificatorWeights.txt");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Opening "+selected);
					ConfigurationLoader.loadMerge(selected);
					updateDataFromConfiguration();
					updateConfiguration() ;
				}
			}
		});

		buttonClassificationAddNewWeight
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				ClassificatorConfElem param = new ClassificatorConfElem();
				param.weight = 1.0F ; param.span= 300 ;
				((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).addNewParam(param);
				updateDataFromConfiguration();
			}
		});
		TableColumn tableColumn = new TableColumn(tableClassificationTimeAndWeights, SWT.NONE);
		tableColumn.setWidth(80);
		tableColumn.setText("time [ms]");

		TableColumn tableColumn1 = new TableColumn(tableClassificationTimeAndWeights, SWT.NONE);
		tableColumn1.setWidth(80);
		tableColumn1.setText("weight");

		TableColumn tableColumn2 = new TableColumn(tableClassificationTimeAndWeights, SWT.NONE);
		tableColumn2.setWidth(tableClassificationTimeAndWeights.getBounds().width-160);
		tableColumn2.setText("");

		for(final ClassificatorConfElem elem : ((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).getClassificationParams(true)){
			TableItem item = new TableItem (tableClassificationTimeAndWeights, SWT.NONE);
			item.setText(0,""+elem.span); item.setText(1,""+elem.weight);
		}

		final TableEditor editor = new TableEditor(tableClassificationTimeAndWeights);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		tableClassificationTimeAndWeights.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				Rectangle clientArea = tableClassificationTimeAndWeights.getClientArea();
				Point pt = new Point(event.x, event.y);
				int index = tableClassificationTimeAndWeights.getTopIndex();
				boolean found = false ;
				while (index < tableClassificationTimeAndWeights.getItemCount()) {
					boolean visible = false;
					final TableItem item = tableClassificationTimeAndWeights.getItem(index);
					for (int i = 0; i < tableClassificationTimeAndWeights.getColumnCount(); i++) {
						Rectangle rect = item.getBounds(i);
						if (rect.contains(pt)) {
							found = true ;
							final int column = i;
							final Text text = new Text(tableClassificationTimeAndWeights, SWT.NONE);
							Listener textListener = new Listener() {
								public void handleEvent(final Event e) {
									switch (e.type) {
									case SWT.FocusOut:
										if(column==0)
											((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).
											updateParam(Integer.parseInt(item.getText(0)),Float.parseFloat(item.getText(1)),
													Integer.parseInt(text.getText()),Float.parseFloat(item.getText(1)));
										else if(column==1)
											((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).
											updateParam(Integer.parseInt(item.getText(0)),Float.parseFloat(item.getText(1)),
													Integer.parseInt(item.getText(0)),Float.parseFloat(text.getText()));
										item.setText(column, text.getText());
										text.dispose();
										updateDataFromConfiguration();
										break;
									case SWT.Traverse:
										switch (e.detail) {
										case SWT.TRAVERSE_RETURN:
											if(column==0)
												((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).
												updateParam(Integer.parseInt(item.getText(0)),Float.parseFloat(item.getText(1)),
														Integer.parseInt(text.getText()),Float.parseFloat(item.getText(1)));
											else if(column==1)
												((ElementClassificatorConf) ProjectStarter.getConf().getElement("classificationParams")).
												updateParam(Integer.parseInt(item.getText(0)),Float.parseFloat(item.getText(1)),
														Integer.parseInt(item.getText(0)),Float.parseFloat(text.getText()));
											item
											.setText(column, text
													.getText());
											//FALL THROUGH
										case SWT.TRAVERSE_ESCAPE:
											text.dispose();
											updateDataFromConfiguration();
											e.doit = false;
											break;

										}
										break;
									}
								}
							};
							text.addListener(SWT.FocusOut, textListener);
							text.addListener(SWT.Traverse, textListener);
							editor.setEditor(text, item, i);
							text.setText(item.getText(i));
							text.selectAll();
							text.setFocus();
							return;
						}
						if (!visible && rect.intersects(clientArea)) {
							visible = true;
						}
					}
					if (!visible)
						return;
					index++;
				}
				if(!found){
					System.out.println("klik poza komrkami");
				}
			}
		});
	}
	/**
	 * This method initializes groupMaxValueInWindow	
	 *
	 */
	private void createGroupMaxValueInWindow() {
		groupMaxValueInWindow = new Group(compositeClassification, SWT.NONE);
		groupMaxValueInWindow.setLayout(null);
		groupMaxValueInWindow.setText("max value in window");
		groupMaxValueInWindow.setBounds(new Rectangle(379, 176, 248, 70));

		labelResponseLength = new Label(groupMaxValueInWindow, SWT.NONE);
		labelResponseLength.setBounds(new Rectangle(13, 19, 93, 15));
		labelResponseLength.setText("Response span:");

		textResponseSpan = new Text(groupMaxValueInWindow, SWT.BORDER);
		textResponseSpan.setBounds(new Rectangle(146, 14, 76, 21));

		textResponseSpan.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				ProjectStarter.getConf().setValue("responseSpan",Integer.parseInt(textResponseSpan.getText()));
				updateConfiguration() ;
			}
		});

		labelOptionsDelay = new Label(groupMaxValueInWindow, SWT.NONE);
		labelOptionsDelay.setBounds(new Rectangle(15, 42, 82, 15));
		labelOptionsDelay.setText("Delay:");
		textOptionsDelay = new Text(groupMaxValueInWindow, SWT.BORDER);
		textOptionsDelay.setBounds(new Rectangle(148, 38, 57, 21));
		textOptionsDelay.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				System.out.println("modifyText() - delay"); 
				updateConfiguration() ;
			}
		});
	}
	/**
	 * This method initializes sShellFreqFilterEdit	
	 *
	 */
	private void createSShellFreqFilterEdit() {
		sShellFreqFilterEdit = new Shell();
		sShellFreqFilterEdit.setLayout(null);
		sShellFreqFilterEdit.setText("Frequency filter parameters");
		sShellFreqFilterEdit.setSize(new Point(366, 342));
		labelEditFreqFilterName = new Label(sShellFreqFilterEdit, SWT.NONE);
		labelEditFreqFilterName.setBounds(new Rectangle(14, 29, 74, 15));
		labelEditFreqFilterName.setText("Filter name:");
		textEditFreqFilterName = new Text(sShellFreqFilterEdit, SWT.BORDER);
		textEditFreqFilterName.setBounds(new Rectangle(99, 25, 242, 21));
		buttonEditFreqFilterParams = new Button(sShellFreqFilterEdit, SWT.NONE);
		buttonEditFreqFilterParams.setBounds(new Rectangle(283, 275, 63, 25));
		buttonEditFreqFilterParams.setText("save");
		buttonEditFreqFilterParams
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {	
				System.out.println(" filter !!"+sShellFreqFilterEdit.getData("createNew"));
				if(sShellFreqFilterEdit.getData("createNew")!=null&&
						((Boolean)sShellFreqFilterEdit.getData("createNew")).booleanValue()==true){
					ElementFrequnecyFilter elem = new ElementFrequnecyFilter();
					elem.setName(textEditFreqFilterName.getText()) ;
					elem.setValue(ElementFrequnecyFilter.COEFF_A,textAreaEditFreqFilterCoeffA.getText()) ;
					elem.setValue(ElementFrequnecyFilter.COEFF_B,textAreaEditFrexFilterCoeffB.getText()) ;
					ProjectStarter.getConf().addElement(elem) ;
				}else{
					((ElementFrequnecyFilter)ProjectStarter.getConf().getElement((String)sShellFreqFilterEdit.getData("editedElem"))).setValue(ElementFrequnecyFilter.COEFF_A,textAreaEditFreqFilterCoeffA.getText());
					((ElementFrequnecyFilter)ProjectStarter.getConf().getElement((String)sShellFreqFilterEdit.getData("editedElem"))).setValue(ElementFrequnecyFilter.COEFF_B,textAreaEditFrexFilterCoeffB.getText());
					ProjectStarter.getConf().updateElementName((String)sShellFreqFilterEdit.getData("editedElem"),textEditFreqFilterName.getText());
				}
				sShellFreqFilterEdit.dispose();	
				updateFreqFilters();
			}
		});
		createGroupEditFreqFilterCoeff();
	}
	/**
	 * This method initializes groupEditFreqFilterCoeff	
	 *
	 */
	private void createGroupEditFreqFilterCoeff() {
		groupEditFreqFilterCoeff = new Group(sShellFreqFilterEdit, SWT.NONE);
		groupEditFreqFilterCoeff.setLayout(null);
		groupEditFreqFilterCoeff.setText("Coefficients");
		groupEditFreqFilterCoeff.setBounds(new Rectangle(13, 57, 326, 214));
		textAreaEditFreqFilterCoeffA = new Text(groupEditFreqFilterCoeff, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		textAreaEditFreqFilterCoeffA.setBounds(new Rectangle(17, 22, 295, 77));
		textAreaEditFrexFilterCoeffB = new Text(groupEditFreqFilterCoeff, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		textAreaEditFrexFilterCoeffB.setBounds(new Rectangle(17, 113, 293, 89));
	}

	private void updateUserVisibleMatrix(){
		((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).updateColors(matrixBkgColor,normalColor,activeColor);
		((UserVisibleMatrixCanvas) canvasUserVisibleMatrix).updateElementsPositions(new Point(Integer.parseInt(textFieldHorizontalMargin.getText()),Integer.parseInt(textFieldVerticalMargin.getText())),
				Integer.parseInt(textFieldFontSize.getText()));
		canvasUserVisibleMatrix.redraw() ;
		//
	}

	private void createCompositeOptions(){
		compositeOptions = new Composite(tabFolderCanvases, SWT.NONE);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeOptions.setLayout(gridLayout);

		checkboxModifyCalibration = new Button(compositeOptions,SWT.CHECK);
		checkboxModifyCalibration.setText("modify calibration");
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3 ;
		checkboxModifyCalibration.setLayoutData(gridData);

		tableCalibration = new Table(compositeOptions, SWT.BORDER );
		tableCalibration.setHeaderVisible(false);
		tableCalibration.setLinesVisible(true);
		gridData = new GridData();
		gridData.horizontalSpan = 2 ;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		tableCalibration.setLayoutData(gridData);


		//		group
		groupMatrixDisplayOptions = new Group(compositeOptions,SWT.NONE);
		groupMatrixDisplayOptions.setText("Matrix display");
		groupMatrixDisplayOptions.setBounds(new Rectangle(516, 324, 300, 200));
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 1 ;
		gridData.verticalSpan = 3 ;
		groupMatrixDisplayOptions.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		groupMatrixDisplayOptions.setLayout(gridLayout);
		//normalColor,activeColor
		matrixBkgColor = new Color(sShell.getDisplay(), new RGB(0, 0, 0));
		normalColor = new Color(sShell.getDisplay(), new RGB(50,50,50));
		activeColor = new Color(sShell.getDisplay(), new RGB(255,255,255));
		// Use a label full of spaces to show the color
		final Label colorLabel = new Label(groupMatrixDisplayOptions, SWT.NONE);
		colorLabel.setText("                              ");
		colorLabel.setBackground(matrixBkgColor);
		Button button = new Button(groupMatrixDisplayOptions, SWT.PUSH);
		button.setText("Matrix background...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ColorDialog dlg = new ColorDialog(sShell);
				dlg.setRGB(colorLabel.getBackground().getRGB());
				dlg.setText("Select color");
				RGB rgb = dlg.open();
				if (rgb != null) {
					matrixBkgColor.dispose();
					matrixBkgColor = new Color(sShell.getDisplay(), rgb);
					colorLabel.setBackground(matrixBkgColor);
					updateUserVisibleMatrix();
				}
			}
		});
		final Label colorLabel2 = new Label(groupMatrixDisplayOptions, SWT.NONE);
		colorLabel2.setText("                              ");
		colorLabel2.setBackground(normalColor);
		button = new Button(groupMatrixDisplayOptions, SWT.PUSH);
		button.setText("Text color...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ColorDialog dlg = new ColorDialog(sShell);
				dlg.setRGB(colorLabel2.getBackground().getRGB());
				dlg.setText("Select color");
				RGB rgb = dlg.open();
				if (rgb != null) {
					normalColor.dispose();
					normalColor = new Color(sShell.getDisplay(), rgb);
					colorLabel2.setBackground(normalColor);
					updateUserVisibleMatrix();
				}
			}
		});

		final Label colorLabel3 = new Label(groupMatrixDisplayOptions, SWT.NONE);
		colorLabel3.setText("                              ");
		colorLabel3.setBackground(activeColor);
		button = new Button(groupMatrixDisplayOptions, SWT.PUSH);
		button.setText("Text blink color...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				ColorDialog dlg = new ColorDialog(sShell);
				dlg.setRGB(colorLabel3.getBackground().getRGB());
				dlg.setText("Select color");
				RGB rgb = dlg.open();
				if (rgb != null) {
					activeColor.dispose();
					activeColor = new Color(sShell.getDisplay(), rgb);
					colorLabel3.setBackground(activeColor);
					updateUserVisibleMatrix();
				}
			}
		});

		ModifyListener listener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateUserVisibleMatrix(); ;
			}
		};

		Label textLabel = new Label(groupMatrixDisplayOptions, SWT.NULL);
		textLabel.setText("Text span - horizontal");
		textFieldHorizontalMargin = new Text(groupMatrixDisplayOptions, SWT.SINGLE | SWT.BORDER);
		textFieldHorizontalMargin.setText("33");
		gridData = new GridData();
		gridData.widthHint = 100 ;
		textFieldHorizontalMargin.setLayoutData(gridData);
		textFieldHorizontalMargin.addModifyListener(listener);

		textLabel = new Label(groupMatrixDisplayOptions, SWT.NULL);
		textLabel.setText("Text span - vertical");
		textFieldVerticalMargin = new Text(groupMatrixDisplayOptions, SWT.SINGLE | SWT.BORDER);
		textFieldVerticalMargin.setText("16");
		gridData = new GridData();
		gridData.widthHint = 100 ;
		textFieldVerticalMargin.setLayoutData(gridData);
		textFieldVerticalMargin.addModifyListener(listener);

		textLabel = new Label(groupMatrixDisplayOptions, SWT.NULL);
		textLabel.setText("Text font size");
		textFieldFontSize = new Text(groupMatrixDisplayOptions, SWT.SINGLE | SWT.BORDER);
		textFieldFontSize.setText("16");
		gridData = new GridData();
		gridData.widthHint = 100 ;
		textFieldFontSize.setLayoutData(gridData);
		textFieldFontSize.addModifyListener(listener);


		buttonLoadCalibration = new Button(compositeOptions, SWT.NONE);
		buttonLoadCalibration.setBounds(new Rectangle(516, 324, 89, 25));
		buttonLoadCalibration.setText("load...");
		buttonSaveCalibration = new Button(compositeOptions, SWT.NONE);
		buttonSaveCalibration.setBounds(new Rectangle(447, 324, 62, 25));
		buttonSaveCalibration.setText("save...");
		tableCalibration.setEnabled(false);

		buttonSaveCalibration
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog fd = new FileDialog(sShell, SWT.SAVE);
				fd.setText("Save");
				fd.setFileName("conf_chanCalibration.txt");
				//				        fd.setFilterPath("C:/");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Writing "+selected+" to file ");
					ConfigurationLoader.save(selected,IConfigurationElement.CHANNELS_CALIBRATION);
				}
			}
		});
		buttonSaveCalibration.setEnabled(false);
		buttonLoadCalibration
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog fd = new FileDialog(sShell, SWT.OPEN);
				fd.setText("Open");
				fd.setFileName("conf_chanCalibration.txt");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Opening "+selected);
					ConfigurationLoader.loadMerge(selected);
					initializeCalibration();
					updateConfiguration() ;
				}
			}
		});
		buttonLoadCalibration.setEnabled(false);
		checkboxModifyCalibration.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				tableCalibration.setEnabled(checkboxModifyCalibration.getSelection());
				buttonSaveCalibration.setEnabled(checkboxModifyCalibration.getSelection());
				buttonLoadCalibration.setEnabled(checkboxModifyCalibration.getSelection());
			}
		});

		initializeCalibration() ;
	}
	/**
	 * This method initializes compositeSpatialFilters	
	 *
	 */
	private void createCompositeSpatialFilters() {
		compositeSpatialFilters = new Composite(tabFolderCanvases, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		compositeSpatialFilters.setLayout(gridLayout);

		tableSpatialFilters = new Table(compositeSpatialFilters, SWT.BORDER );
		tableSpatialFilters.setHeaderVisible(false);
		tableSpatialFilters.setLinesVisible(true);
		GridData gridData = new GridData();
		gridData.horizontalSpan = 3 ;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		tableSpatialFilters.setLayoutData(gridData);
		buttonLoadSpatialFilters = new Button(compositeSpatialFilters, SWT.NONE);
		buttonLoadSpatialFilters.setBounds(new Rectangle(516, 324, 89, 25));
		buttonLoadSpatialFilters.setText("load...");
		buttonSpatialFiltersSave = new Button(compositeSpatialFilters, SWT.NONE);
		buttonSpatialFiltersSave.setBounds(new Rectangle(447, 324, 62, 25));
		buttonSpatialFiltersSave.setText("save...");
		checkBoxApplyToInputs = new Button(compositeSpatialFilters, SWT.CHECK);
		checkBoxApplyToInputs.setBounds(new Rectangle(18, 329, 406, 16));
		checkBoxApplyToInputs.setText("apply to input channels");
		checkBoxApplyToInputs
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				ProjectStarter.getConf().setValue("applySpatialFilterToInput",checkBoxApplyToInputs.getSelection()?1:0);
				updateConfiguration() ;
			}
		});
		buttonSpatialFiltersSave
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog fd = new FileDialog(sShell, SWT.SAVE);
				fd.setText("Save");
				fd.setFileName("conf_chanWeights.txt");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Writing "+selected+" to file ");
					ConfigurationLoader.save(selected,IConfigurationElement.CHANNELS_WEIGHTS);
				}
			}
		});
		buttonLoadSpatialFilters
		.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog fd = new FileDialog(sShell, SWT.OPEN);
				fd.setText("Open");
				fd.setFileName("conf_chanWeights.txt");
				String[] filterExt = { "*.txt"};
				fd.setFilterExtensions(filterExt);
				String selected = fd.open();
				if(selected!=null){
					System.out.println("[ControlPanel] Opening "+selected);
					ConfigurationLoader.loadMerge(selected);
					initializeDimmFilters();
					updateConfiguration() ;
				}
			}
		});
		initializeDimmFilters() ;
	}
	/**
	 * This method initializes groupClassificationAvrageInSpan	
	 *
	 */
	private void createGroupClassificationAvrageInSpan() {
		groupClassificationAvrageInSpan = new Group(compositeClassification,
				SWT.NONE);
		groupClassificationAvrageInSpan.setLayout(null);
		groupClassificationAvrageInSpan.setText("avrage in span");
		groupClassificationAvrageInSpan.setBounds(new Rectangle(74, 92, 330, 124));
		labelClassificationAvrageFrom = new Label(groupClassificationAvrageInSpan, SWT.NONE);
		labelClassificationAvrageFrom.setBounds(new Rectangle(10, 25, 101, 15));
		labelClassificationAvrageFrom.setText("Time start:");
		labelClassificationAvrageTo = new Label(groupClassificationAvrageInSpan, SWT.NONE);
		labelClassificationAvrageTo.setBounds(new Rectangle(10, 45, 102, 15));
		labelClassificationAvrageTo.setText("Time stop:");
		textClassificationAvrageTo = new Text(groupClassificationAvrageInSpan, SWT.BORDER);
		textClassificationAvrageTo.setBounds(new Rectangle(116, 41, 76, 21));
		textClassificationAvrageTo.setText("400");
		textClassificationAvrageTo.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				updateClassification();
			}
		});
		textClassificationAvrageFrom = new Text(groupClassificationAvrageInSpan, SWT.BORDER);
		textClassificationAvrageFrom.setBounds(new Rectangle(116, 19, 76, 21));
		textClassificationAvrageFrom.setText("350");
		textClassificationAvrageFrom.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				updateClassification();
			}
		});

	}

	private void updateClassification(){
		if(textClassificationAvrageTo.getText()!=""&&textClassificationAvrageFrom.getText()!=""&&
				Integer.parseInt(textClassificationAvrageTo.getText())>Integer.parseInt(textClassificationAvrageFrom.getText())){
			ProjectStarter.getConf().setValue("classificationAvrageTo",Integer.parseInt(textClassificationAvrageTo.getText()));
			ProjectStarter.getConf().setValue("classificationAvrageFrom",Integer.parseInt(textClassificationAvrageFrom.getText()));
			updateConfiguration() ;
			textClassificationAvrageTo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE)) ;
			textClassificationAvrageFrom.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE)) ;
		}else{
			textClassificationAvrageTo.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED)) ;
			textClassificationAvrageFrom.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_RED)) ;
		}
	}

	private void createCompositeDisplay(){
		compositeDisplay = new Composite(tabFolderCanvases, SWT.NONE);
		compositeDisplay.setLayout(new GridLayout());
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/* Before this is run, be sure to set up the launch configuration (Arguments->VM Arguments)
		 * for the correct SWT library path in order to run with the SWT dlls. 
		 * The dlls are located in the SWT plugin jar.  
		 * For example, on Windows the Eclipse SWT 3.1 plugin jar is:
		 *       installation_directory\plugins\org.eclipse.swt.win32_3.1.0.jar
		 */
		Display display = Display.getDefault();
		ControlPanel thisClass = new ControlPanel();
		thisClass.createSShell();
		thisClass.sShell.open();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/**
	 * This method initializes sShell
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("jP300 - Control Panel");
		sShell.setSize(new Point(864, 774));
		sShell.setLayout(null);
		menuBar = new Menu(sShell, SWT.BAR);
		MenuItem pushFile = new MenuItem(menuBar, SWT.PUSH);
		pushFile.setText("File");
		MenuItem pushSettings = new MenuItem(menuBar, SWT.PUSH);
		pushSettings.setText("Settings");
		pushSettings
		.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				createSShellSettings();
			}
			public void widgetDefaultSelected(
					org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem pushAbout = new MenuItem(menuBar, SWT.PUSH);
		pushAbout.setText("About");
		pushAbout.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				createSShellAbout();
			}
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
		});
		sShell.setMenuBar(menuBar);



		labelStatData = new Label(sShell, SWT.NONE);
		labelStatData.setBounds(new Rectangle(9, 4, 642, 15));
		labelStatData.setText("Stats: 300 samples/s COM3 6 channels | protocol OpenEEG P3");
		createGroupMatrixResponses();
		createGroupUserMatrix();
		createGroupOptions();
		createTabFolder();
		createTabFolderCanvases();
		updateDataFromConfiguration() ;
	}
	public boolean isDisposed() {
		return sShell.isDisposed() ;
	}
	private MatrixTextElement me ;
	private Group groupOptionsTiming = null;
	private Label labelPresentation = null;
	private Label labelInterspaceMax = null;
	private Label labelResponseLength = null;
	private Text textPresentationTime = null;
	private Text textInterspaceMaxTime = null;
	private Text textResponseSpan = null;
	private Button buttonStartStop = null;
	private Group groupOptionsOther = null;
	private Label labelRepeats = null;
	private Text textRepeatsNumber = null;
	private Shell sShellAbout = null;  //  @jve:decl-index=0:visual-constraint="877,10"
	private Label labelAboutCaption = null;
	private Label labelAboutDescription = null;
	private Button buttonAboutExit = null;
	private Label labelUserName = null;
	private Text textUserName = null;
	private Label labelOptionsDelay = null;
	private Text textOptionsDelay = null;
	private Shell sShellSettings = null;  //  @jve:decl-index=0:visual-constraint="878,246"
	private Group groupSettingsSource = null;
	private Button buttonSettingsOk = null;
	private Combo comboSettingsSource = null;
	private Label labelSettingsSource = null;
	private Button buttonSettingsSelectFile = null;
	private Text textSettingsSelectFile = null;
	private Group groupSettingsOutput = null;
	private Button checkBoxOutputFile = null;
	private Button checkBoxOutputTcpip = null;
	private Label labelSourceProtocol = null;
	private Combo comboSettingsProtocol = null;
	private TabFolder tabFolder = null;
	private Composite compositeFreqFilters = null;
	//	private Composite compositeDimensionalFilters = null;
	private Composite compositeOutput = null;
	private Text textOutput = null;
	private Button checkBoxPowerLineFilter = null;
	private Group groupDimensionalCoefficients = null;
	private Label labelElecrodeCoeff1 = null;
	private Text textElecrodeCoeff1 = null;
	private Text textSettingsTcpIpPort = null;
	private TabFolder tabFolderCanvases = null;
	private RawEegCanvas canvasRawEegData = null;
	private Canvas canvasRsquare = null;
	private Button buttonTraining = null;
	private Label labelInterspaceMin = null;
	private Text textInterspaceMinTime = null;
	private Button buttonMaginifierPlus = null;
	private Button buttonMagnifierMinus = null;
	private Button buttonSeperateWindow = null;
	private Label labelSamplingFreqnecy = null;
	private Text textSamplingFrequnecy = null;
	private Group groupMatrixDefinition = null;
	private Button checkBoxTcpiIpInput = null;
	private Text textSettingsTcpIpInputPort = null;
	private Canvas canvasRawEegDataScale = null;
	private Label labelSettingsHerz = null;
	private Combo comboSettingsComPort = null;
	private Composite compositeClassification = null;
	private Canvas canvasWeightsGraph = null;
	private Table tableClassificationTimeAndWeights = null;
	private Button buttonClassificationAddNewWeight = null;
	private Button radioButtonClassification = null;
	private Button radioButtonClassificationWeights = null;
	private Group groupClassificationOptions = null;
	private Group groupWeightsBased = null;
	private Group groupMaxValueInWindow = null;
	private Button checkBoxChannelVisible = null;
	private Button buttonMagnifySignalAmpPlus = null;
	private Button buttonMagnifySignalAmpMinux = null;
	private Shell sShellFreqFilterEdit = null;  //  @jve:decl-index=0:visual-constraint="891,715"
	private Label labelEditFreqFilterName = null;
	private Text textEditFreqFilterName = null;
	private Button buttonEditFreqFilterParams = null;
	private Group groupEditFreqFilterCoeff = null;
	private Text textAreaEditFreqFilterCoeffA = null;
	private Text textAreaEditFrexFilterCoeffB = null;
	private Button buttonAddFreqencyFilter = null;
	private Button buttonSaveConfiguration = null;
	private Label labelSettingsChannels = null;
	private Text textSettingsNumOfChannels = null;
	private Composite compositeSpatialFilters = null;
	private Table tableSpatialFilters = null;
	private Label labelAlltimesInMs = null;
	private Label labelSubjectSession = null;
	private Text textSubjectSession = null;
	private Label labelWaitBeforeStart = null;
	private Label labelWaitBetween = null;
	private Text textWaitBefore = null;
	private Text textWaitBetween = null;
	private Button buttonClassificationWeightsLoad = null;
	private Button buttonLoadSpatialFilters = null;
	private Button buttonFreqFiltersLoad = null;
	private Button buttonFreqFilterSave = null;
	private Button buttonSpatialFiltersSave = null;
	private Button buttonClassificationWeightsSave = null;
	private Button radioButtonClassificationAvgInSpan = null;
	private Group groupClassificationAvrageInSpan = null;
	private Label labelClassificationAvrageFrom = null;
	private Label labelClassificationAvrageTo = null;
	private Text textClassificationAvrageTo = null;
	private Text textClassificationAvrageFrom = null;
	private Button radioButtonClassificationAvrageMin = null;
	private Button radioButtonClassificationAvrageMAx = null;
	private Button checkBoxApplyToInputs = null;
	private Composite compositeDisplay = null ;
	public void indexesWon(int[] indexes) {
		me = ((UserVisibleMatrixCanvas) getCanvasUserVisibleMatrix()).getMatrixElementsIntersection(indexes);
		if(!Display.getDefault().isDisposed()){
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed())
						textOutput.setText(textOutput.getText()+" "+me.getText()) ;
				}
			});
		}
	}
	public String getSelectedComPort() {
		return comboSettingsComPort.getText() ;
	}
	public void unblockButtons() {
		if(!Display.getDefault().isDisposed()){
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if(!isDisposed()){
						buttonTraining.setEnabled(true);
						buttonStartStop.setEnabled(true);
					}
				}
			});
		}

	}

}
