package configuration;

import java.util.StringTokenizer;

import org.eclipse.swt.graphics.Point;

import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.XMLFormat.InputElement;
import javolution.xml.XMLFormat.OutputElement;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

public class ElementChannelsWeights extends IConfigurationElement {
	
	public ElementChannelsWeights() {
		super(IConfigurationElement.CHANNELS_WEIGHTS);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	FastTable<FastTable<Double>> matrixValues = new FastTable<FastTable<Double>>() ;
	FastTable<Double> matrixRow = new FastTable<Double>() ;
	public void addNewRow(){
		if(matrixRow.size()>0){
			matrixValues.add(matrixRow);
			matrixRow = new FastTable<Double>() ; 
		}
	}
	
	public void addValue(Double value){
		matrixRow.add(value);
	}
	
	public void addToRow(int rowNumber , double value) {	
		matrixValues.get(rowNumber).add(value);
	}
	
	public void setIfEmpty(int column, int rowNumber , double value) {
		if(matrixValues.size()<=rowNumber)
			matrixValues.addLast(new FastTable<Double>()) ;
		if(matrixValues.get(rowNumber).size()<=column)
			matrixValues.get(rowNumber).addLast(value);
	}
	
	public Double[] getRow(int rowNumber) {
		return matrixValues.get(rowNumber).toArray(new Double[matrixValues.get(rowNumber).size()]);
	}
	
	public Double[][] getMatrixValues() {
		Double[][] out = new Double[matrixValues.size()][] ;
		for(int i=0;i<out.length;i++){
			FastTable<Double> vals = matrixValues.get(i);
			out[i] = new Double[vals.size()];
			for(int a = 0 ; a < vals.size(); a++)
				out[i][a] = vals.get(a);
		}
		return out ;
	}
	public Point getMatrixDimensions(){
		return new Point(matrixValues.get(0).size(),matrixValues.size()) ; 
	}
	
	protected static final XMLFormat<ElementChannelsWeights> XML = new XMLFormat<ElementChannelsWeights>(ElementChannelsWeights.class) {
        public void write(ElementChannelsWeights g, OutputElement xml) throws XMLStreamException {
        	IConfigurationElement.XML.write(g,xml);
        	for(Double []row : g.getMatrixValues()){
        		xml.getStreamWriter().writeStartElement("row");
        		for(Double elem: row){
        			xml.getStreamWriter().writeStartElement("val");
        			xml.addText(""+elem) ;
        			xml.getStreamWriter().writeEndElement();
        		}
        		xml.getStreamWriter().writeEndElement();
        	}
        }
        public void read(InputElement xml, ElementChannelsWeights g) throws XMLStreamException {
        	IConfigurationElement.XML.read(xml,g);
            XMLStreamReader reader = xml.getStreamReader();
            boolean completed = false ;
            while (reader.getEventType() != XMLStreamConstants.END_DOCUMENT) {
                switch (reader.next()) {
                    case XMLStreamConstants.START_ELEMENT:
                    	if (reader.getLocalName().equals("val")) {
                            g.addValue(Double.parseDouble(reader.getElementText().toString()));
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
	public void setValue(int row, int column, double d) {
		matrixValues.get(row).set(column,d);
	}
}
