package configuration;

import java.io.Serializable;

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.XMLFormat.InputElement;
import javolution.xml.XMLFormat.OutputElement;
import javolution.xml.stream.XMLStreamException;

public abstract class IConfigurationElement implements Serializable, XMLSerializable {
	
	public static final int INT = 1;
	public static final int STRING = 2;
	public static final int FREQFILTER = 3;
	public static final int MATRIXDEFINITION = 4;
	public static final int CLASSIFICATOR_CONF = 5;
	public static final int CHANNELS_WEIGHTS = 6;
	public static final int CHANNELS_VISIBILITY = 7;
	public static final int CHANNELS_CALIBRATION = 8;
	
	private int type;
	private String name;
	private int value;
	private String stringVal;
	
	public IConfigurationElement(int type) {
		this.type = type ;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getValue() {
		return value;
	}
	public int getType() {
		return type;
	}
	public String getValueString() {
		return stringVal;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public void setValue(String value) {
		if(this.type==INT)
			this.value = Integer.parseInt(value);
		else
			this.stringVal = value ;
	}

	protected static final XMLFormat<IConfigurationElement> XML = new XMLFormat<IConfigurationElement>(IConfigurationElement.class) {
		public void write(IConfigurationElement g, OutputElement xml) throws XMLStreamException {
            xml.add(g.type, "type",Integer.class);
            xml.add(g.name, "name",String.class);
            xml.add(g.value, "value",Integer.class);
            xml.add(g.stringVal, "stringVal",String.class);
        }
        public void read(InputElement xml, IConfigurationElement g) throws XMLStreamException {
            g.type = xml.get("type",Integer.class);
            g.name = xml.get("name",String.class);
            g.value = xml.get("value",Integer.class);
            g.stringVal = xml.get("stringVal",String.class);
       }
   };
}
