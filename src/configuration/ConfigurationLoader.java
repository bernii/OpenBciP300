package configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import astart.ProjectStarter;

import display.ControlPanel;


public class ConfigurationLoader {

	private static XMLBinding binding ;
	private static void init(){
		binding = new XMLBinding();
		binding.setAlias(ElementMatrixDefinition.class, "matrixDef");
		binding.setAlias(ElementNormal.class, "simple");
		binding.setAlias(ElementFrequnecyFilter.class, "frequnecyFilter");
		binding.setAlias(ElementClassificatorConf.class, "classificatorConf");
		binding.setAlias(ElementChannelsWeights.class, "channelsWeights");
		binding.setAlias(ElementChannelsCalibration.class, "channelsCalibration");
		binding.setAlias(ElementChannelsVisibility.class, "channelsVisibility");
		binding.setClassAttribute("type"); // Use "type" instead of "class" for class attribute.
	}

	public static void loadMerge(String filename){
		if(binding==null)
			init() ;
		Configuration a = null ;
		try {
			XMLObjectReader reader = XMLObjectReader.newInstance(new FileInputStream(filename));
			reader.setBinding(binding);
			a = reader.read("Configuration", Configuration.class);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		for(IConfigurationElement elem: a.getElements()){
			ProjectStarter.getConf().addElement(elem);
		}
	}
	
	public static Configuration load(){
		return load("conf.xml.txt") ;
	}
	public static Configuration load(String fileName){
		if(binding==null)
			init() ;
		Configuration a = null ;
		try {
			XMLObjectReader reader = XMLObjectReader.newInstance(new FileInputStream(fileName));
			reader.setBinding(binding);
			a = reader.read("Configuration", Configuration.class);
			reader.close();

			reader = XMLObjectReader.newInstance(new FileInputStream("conf_freq.txt"));
			reader.setBinding(binding);
			a.merge(reader.read("Configuration", Configuration.class));
			reader.close();

			reader = XMLObjectReader.newInstance(new FileInputStream("conf_chanWeights.txt"));
			reader.setBinding(binding);
			a.merge(reader.read("Configuration", Configuration.class));
			reader.close();
			
			reader = XMLObjectReader.newInstance(new FileInputStream("conf_chanCalibration.txt"));
			reader.setBinding(binding);
			a.merge(reader.read("Configuration", Configuration.class));
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		return a ;
	}

	public static void save(String filename, int whatToWrite) {
		String newname = new String(filename) ;
		if(binding==null)
			init() ;
		File f = new File(newname);
		int i = 0 ;
		if(f.exists()){
			f.renameTo(new File(newname+".old"));
		}
		XMLObjectWriter writer;
		try {
			writer = XMLObjectWriter.newInstance(new FileOutputStream(filename));
			writer.setBinding(binding); 
			writer.setIndentation("\t"); // use tabulation for indentation

			System.out.println("[ConfigurationLoader] getting elements of type "+whatToWrite);
			writer.write(ProjectStarter.getConf().getConfWithElements(whatToWrite), "Configuration", Configuration.class);
			writer.close(); 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}

	public static void save(String filename) {
		String newname = new String(filename) ;
		if(binding==null)
			init() ;
		File f = new File(newname);
		int i = 0 ;
		if(f.exists()){
			f.renameTo(new File(newname+".old"));
		}

		XMLObjectWriter writer;
		try {
			writer = XMLObjectWriter.newInstance(new FileOutputStream(filename));
			writer.setBinding(binding);
			writer.setIndentation("\t"); // use tabulation for indentation
			writer.write(ProjectStarter.getConf(), "Configuration", Configuration.class);
			writer.close(); 

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}

	}
}
