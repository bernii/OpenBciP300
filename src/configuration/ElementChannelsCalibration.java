package configuration;

import java.io.Serializable;

import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamConstants;
import javolution.xml.stream.XMLStreamException;
import javolution.xml.stream.XMLStreamReader;

import org.eclipse.swt.graphics.Point;

import sun.awt.SunHints.Value;

public class ElementChannelsCalibration extends IConfigurationElement {

	private static final long serialVersionUID = 3282379265379199009L;

	public ElementChannelsCalibration() {
		super(IConfigurationElement.CHANNELS_CALIBRATION);
	}
	
	private FastTable<ValuePair> calData = new FastTable<ValuePair>();
	
	private class ValuePair implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3116047999848312308L;
		private float gain ;
		private float offset ;
		
		public ValuePair(float _gain, float _offset){
			gain = _gain ;
			offset = _offset ;
		}
		
		public float getGain(){
			return gain ;
		}
		
		public float getOffset(){
			return offset ;
		}

		public void setGain(float gain) {
			this.gain = gain;
		}

		public void setOffset(float offset) {
			this.offset = offset;
		}
	}
	
	public FastTable<ValuePair> getValues(){
		return calData ;
	}
	
	protected void addNewChan() {
		calData.add(new ValuePair(0,0));
	}
	
	protected void addGain(double gain) {
		addNewChan();
		calData.getLast().setGain((float) gain)	;	
	}
	
	protected void addOffset(double off) {
		calData.getLast().setOffset((float) off)	;	
	}
	
	
	protected static final XMLFormat<ElementChannelsCalibration> XML = new XMLFormat<ElementChannelsCalibration>(ElementChannelsCalibration.class) {
        public void write(ElementChannelsCalibration g, OutputElement xml) throws XMLStreamException {
        	IConfigurationElement.XML.write(g,xml);
        	for(ValuePair pair : g.getValues()){
        		xml.getStreamWriter().writeStartElement("chan");
        		xml.getStreamWriter().writeStartElement("gain");
        		xml.addText(""+pair.getGain()) ;
        		xml.getStreamWriter().writeEndElement();
        		xml.getStreamWriter().writeStartElement("offset");
        		xml.addText(""+pair.getOffset()) ;
        		xml.getStreamWriter().writeEndElement();
        		xml.getStreamWriter().writeEndElement();
        	}
        }
        public void read(InputElement xml, ElementChannelsCalibration g) throws XMLStreamException {
        	IConfigurationElement.XML.read(xml,g);
            XMLStreamReader reader = xml.getStreamReader();
            boolean completed = false ;
            while (reader.getEventType() != XMLStreamConstants.END_DOCUMENT) {           	
                switch (reader.next()) {              
                    case XMLStreamConstants.START_ELEMENT:
                    	if (reader.getLocalName().equals("gain")) {
                            g.addGain(Double.parseDouble(reader.getElementText().toString()));
                       }
                    	if (reader.getLocalName().equals("offset")) {
                            g.addOffset(Double.parseDouble(reader.getElementText().toString()));
                       }
                    	break;
                    case XMLStreamConstants.END_ELEMENT:
                    	
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

	public void setIfEmpty(int chanNum, float gain, float offset) {
		if(chanNum>calData.size())
			calData.add(new ValuePair(gain,offset));
	}

	public double[] getChan(int i) {
		return new double[]{calData.get(i).gain,calData.get(i).offset};
	}

	public void setValue(int i, int j, double parseDouble) {
		if(j==0)
			calData.get(i).setGain((float) parseDouble);
		else{
			System.out.println("[ElementChanCalibration] Setting offset on chan "+i+" to "+parseDouble);
			calData.get(i).setOffset((float) parseDouble);
		}
	}

	public double[] getGains() {
		double[] out = new double[calData.size()];
		for(int i=0;i<calData.size();i++)
			out[i] = calData.get(i).getGain() ;
		return out;
	}
	
	public double[] getOffsets() {
		double[] out = new double[calData.size()];
		for(int i=0;i<calData.size();i++)
			out[i] = calData.get(i).getOffset() ;
		return out;
	}
	
}
