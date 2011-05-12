package configuration;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import javolution.util.FastMap;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

public class Configuration implements Serializable , XMLSerializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int SOURCE_RANDOM = 0 ;
	public static final int SOURCE_HDD = 1 ;
	public static final int SOURCE_COM = 2 ;
	
	public static final int CLASSIFICATION_WINDOW = 0 ;
	public static final int CLASSIFICATION_WEIGHTS = 1 ;
	public static final int CLASSIFICATION_AVG_IN_WINDOW = 2 ;
		
	public static String getSourceAsString(int type){
		switch(type){
			case SOURCE_RANDOM:
				return "Random" ;
			case SOURCE_HDD:
				return "Hdd" ;	
			case SOURCE_COM:
				return "COM/USB" ;
		}
		return null ;
	}
	protected static final XMLFormat<Configuration> XML = new XMLFormat<Configuration>(Configuration.class) {
        public void write(Configuration g, OutputElement xml) throws XMLStreamException {
        	for (FastMap.Entry<String, IConfigurationElement> e = g.elements.head(), end = g.elements.tail(); (e = e.getNext()) != end;) {
                xml.add(e.getValue(),"element") ;
           }
        }
        public void read(InputElement xml, Configuration g) throws XMLStreamException {
        	while(xml.hasNext()){
        		g.addElement((IConfigurationElement) xml.get("element"));
        	}
       }
   };
		
   private FastMap<String, IConfigurationElement> elements = new FastMap<String, IConfigurationElement>() ;
	public void addElement(IConfigurationElement elem) {
		System.out.println("[Configuration ] adding element[1]: "+elem.getName()+" = "+elem.getValue());		
		elements.put(elem.getName(),elem) ;
	}

	public int getValueInt(String parameterName){
		return elements.get(parameterName).getValue() ;
	}
	
	public String getValueString(String parameterName){
		return elements.get(parameterName).getValueString() ;
	}

	public void setValue(String string, int i) {
		elements.get(string).setValue(i);
	}
	
	public void setValue(String string, String i) {
		elements.get(string).setValue(i);
	}
		
	public ElementFrequnecyFilter[] getFilters(){
		FastTable<ElementFrequnecyFilter> out = new FastTable<ElementFrequnecyFilter>(); 
		for(IConfigurationElement elem: elements.values()){
			if(elem.getType()==IConfigurationElement.FREQFILTER)
				out.add((ElementFrequnecyFilter) elem) ;
		}
		return out.toArray(new ElementFrequnecyFilter[out.size()]);
	}
	
	transient private FastTable<IConfigurationListener> confListeners = new FastTable<IConfigurationListener>() ;

	public void addConfigurationListener(IConfigurationListener listner){
		confListeners.add(listner) ;
	}
	public void notifyListeners() {
		System.out.println("[Configuration:notifyListeners]");
		for(IConfigurationListener listner: confListeners)	
			listner.configurationChanged();
	}
	
	public FastTable<IConfigurationListener> getConfListeners(){
		return confListeners ;
	}
	
	public void setConfListeners(FastTable<IConfigurationListener> confListeners){
		this.confListeners  = confListeners;
	}

	public IConfigurationElement getElement(String name) {
		return elements.get(name);
	}
	
	public Collection<IConfigurationElement> getElements() {
		return elements.values() ;
	}

	public void updateElementName(String oldname, String newname) {
		IConfigurationElement elem = elements.get(oldname);
		elem.setName(newname) ;
		elements.put(newname,elem);
		elements.remove(oldname);
	}

	public void merge(Configuration configuration) {
		for(IConfigurationElement elem : configuration.getElements()){
			addElement(elem) ;
		}
	}

	public Configuration getConfWithElements(int elementType) {
		Configuration conf = new Configuration();
		for(IConfigurationElement elem : this.getElements()){
			if(elem.getType()==elementType)
				conf.addElement(elem);
		}
		return conf;
	}
}
