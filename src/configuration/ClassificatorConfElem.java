package configuration;

import java.io.Serializable;

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.XMLFormat.InputElement;
import javolution.xml.XMLFormat.OutputElement;
import javolution.xml.stream.XMLStreamException;

public class ClassificatorConfElem implements Comparable, Serializable, XMLSerializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5596358470229349320L;
	public int span ;
	public float weight ;

	public int compareTo(Object arg0) {
		if(this.span>((ClassificatorConfElem)arg0).span)
			return 1 ;
		else if(this.span<((ClassificatorConfElem)arg0).span)
			return -1 ;
		return 0;
	}

	protected static final XMLFormat<ClassificatorConfElem> XML = new XMLFormat<ClassificatorConfElem>(ClassificatorConfElem.class) {
        public void write(ClassificatorConfElem g, OutputElement xml) throws XMLStreamException {
            xml.add(g.span, "span",Integer.class);
            xml.add(g.weight, "weight",Float.class);

        }
        public void read(InputElement xml, ClassificatorConfElem g) throws XMLStreamException {
            g.span = xml.get("span",Integer.class);
            g.weight = xml.get("weight",Float.class);
       }
   };
}