package configuration;

import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

import org.eclipse.swt.graphics.Point;

public class ElementMatrixDefinition extends IConfigurationElement {

	public ElementMatrixDefinition() {
		super(IConfigurationElement.MATRIXDEFINITION);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	FastTable<FastTable<String>> matrixValues = new FastTable<FastTable<String>>() ;
	FastTable<String> matrixRow = new FastTable<String>() ;
	public void addNewRow(){
		if(matrixRow.size()>0){
			matrixValues.add(matrixRow);
			matrixRow = new FastTable<String>() ; 
		}
	}
	
	public void addValue(String value){
		matrixRow.add(value);
	}
	public String[][] getMatrixValues() {
		String[][] out = new String[matrixValues.size()][] ;
		for(int i=0;i<out.length;i++){
			FastTable<String> vals = matrixValues.get(i);
			out[i] = new String[vals.size()];
			for(int a = 0 ; a < vals.size(); a++)
				out[i][a] = vals.get(a);
		}
		return out ;
	}
	public Point getMatrixDimensions(){
		return new Point(matrixValues.get(0).size(),matrixValues.size()) ; 
	}
	
	
	protected static final XMLFormat<ElementMatrixDefinition> XML = new XMLFormat<ElementMatrixDefinition>(ElementMatrixDefinition.class) {
        public void write(ElementMatrixDefinition g, OutputElement xml) throws XMLStreamException {
        	IConfigurationElement.XML.write(g,xml);
        	for(String []row : g.getMatrixValues()){
        		xml.getStreamWriter().writeStartElement("row");
        		for(String elem: row){
        			xml.getStreamWriter().writeStartElement("val");
        			xml.addText(elem) ;
        			xml.getStreamWriter().writeEndElement();
        		}
        		xml.getStreamWriter().writeEndElement();
        	}
        }
        public void read(InputElement xml, ElementMatrixDefinition g) throws XMLStreamException {
        	IConfigurationElement.XML.read(xml,g);
            XMLStreamReader reader = xml.getStreamReader();
            boolean completed = false ;
            while (reader.getEventType() != XMLStreamConstants.END_DOCUMENT) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                    	if (reader.getLocalName().equals("val")) {
                            g.addValue(reader.getElementText().toString());
                       }
                    	break;
                    case XMLStreamConstants.END_ELEMENT:
                    	if (reader.getLocalName().equals("row")) {
                            g.addNewRow() ;
                       }
                    	if (reader.getLocalName().equals("element")) {
                            completed = true ;
                       }
                    	break ;
                }   
                if (completed) {
                    break ;
               }
            }
       }
   };

}
