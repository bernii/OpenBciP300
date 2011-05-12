package configuration;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

public class ElementChannelsVisibility extends IConfigurationElement {
	
	public ElementChannelsVisibility() {
		super(IConfigurationElement.CHANNELS_VISIBILITY);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FastTable<Integer> indexes = new FastTable<Integer>() ;
	
	public void addValue(Integer value){
		indexes.add(value);	
	}
	
	public Integer[] getValues() {
		return indexes.toArray(new Integer[indexes.size()]);
	}
	
	public boolean hasValue(int index) {
		return indexes.contains(index);
	}
	
	protected static final XMLFormat<ElementChannelsVisibility> XML = new XMLFormat<ElementChannelsVisibility>(ElementChannelsVisibility.class) {
        public void write(ElementChannelsVisibility g, OutputElement xml) throws XMLStreamException {
        	IConfigurationElement.XML.write(g,xml);
        	for(Integer val: g.indexes)
            	xml.add(val, "index",Integer.class);

        }
        public void read(InputElement xml, ElementChannelsVisibility g) throws XMLStreamException {
        	IConfigurationElement.XML.read(xml,g);
            while(xml.hasNext()){
           		g.indexes.add(xml.get("index", Integer.class));
            }
       }
   };

}
