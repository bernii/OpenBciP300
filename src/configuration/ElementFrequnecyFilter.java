package configuration;

import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

public class ElementFrequnecyFilter extends IConfigurationElement {

	public static final int COEFF_A = 0 ;
	public static final int COEFF_B = 1 ;
	
	public ElementFrequnecyFilter() {
		super(IConfigurationElement.FREQFILTER);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public FastTable<Double> valuesA = new FastTable<Double>() ;
	public FastTable<Double> valuesB = new FastTable<Double>() ;
	
	public void addValue(int coeffType, double value){
		switch(coeffType){
		case COEFF_A:
			valuesA.add(value);
			break ;
		case COEFF_B:
			valuesB.add(value);
			break ;	
		}
		
	}
	public double[] getValues(int coeffType) {
		FastTable<Double> values = null;
		switch(coeffType){
		case COEFF_A:
			values = valuesA ;
			break ;
		case COEFF_B:
			values = valuesB ;
			break ;	
		}
		double[] out = new double[values.size()];
		int i = 0 ;
		for(Double val :values)
			out[i++] = val ;
		return out;
	}
	
	protected static final XMLFormat<ElementFrequnecyFilter> XML = new XMLFormat<ElementFrequnecyFilter>(ElementFrequnecyFilter.class) {
        public void write(ElementFrequnecyFilter g, OutputElement xml) throws XMLStreamException {
        	IConfigurationElement.XML.write(g,xml);
        	for(double val: g.valuesA)
            	xml.add(val, "coeffA",Double.class);
        	for(double val: g.valuesB)
            	xml.add(val, "coeffB",Double.class);
        }
        public void read(InputElement xml, ElementFrequnecyFilter g) throws XMLStreamException {
        	IConfigurationElement.XML.read(xml,g);
            while(xml.hasNext()){
            	if(xml.getStreamReader().getLocalName().equals("coeffA"))
            		g.valuesA.add(xml.get("coeffA", Double.class));
            	else
            		g.valuesB.add(xml.get("coeffB", Double.class));
            }
       }
   };

	public String getValuesAsString(int coeffType) {
		FastTable<Double> values = null;
		switch(coeffType){
		case COEFF_A:
			values = valuesA ;
			break ;
		case COEFF_B:
			values = valuesB ;
			break ;	
		}
		TextBuilder txtb = new TextBuilder("[");
		for(double val: values){
			txtb.append(""+val+";");
        }
		txtb.setCharAt(txtb.length()-1, ']');
		return txtb.toString() ;
	}
	
	public void setValue(int coeffType, String valuesAsVector) {
		switch(coeffType){
		case COEFF_A:
			valuesA.clear() ;
			valuesAsVector = valuesAsVector.replace("[","").replace("]","");
			StringTokenizer tok = new StringTokenizer(valuesAsVector,";");
			while(tok.hasMoreElements()){
				valuesA.add(Double.parseDouble(tok.nextToken()));
			}
			break ;
		case COEFF_B:
			valuesB.clear() ;
			valuesAsVector = valuesAsVector.replace("[","").replace("]","");
			StringTokenizer tok1 = new StringTokenizer(valuesAsVector,";");
			while(tok1.hasMoreElements()){
				valuesB.add(Double.parseDouble(tok1.nextToken()));
			}
			break ;	
		}
		
	}
}
