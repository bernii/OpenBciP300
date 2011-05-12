package configuration;

import javolution.util.FastTable;
import javolution.xml.XMLFormat;
import javolution.xml.XMLFormat.InputElement;
import javolution.xml.stream.XMLStreamException;

public class ElementClassificatorConf extends IConfigurationElement {

	public ElementClassificatorConf() {
		super(IConfigurationElement.CLASSIFICATOR_CONF);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	FastTable<ClassificatorConfElem> classificatorConf = new FastTable<ClassificatorConfElem>() ;

	public void addNewParam(ClassificatorConfElem elem) {
		classificatorConf.add(elem);
	}

	public FastTable<ClassificatorConfElem> getClassificationParams(boolean sorted){
		if(sorted){
			FastTable<ClassificatorConfElem> out = new FastTable<ClassificatorConfElem>();
			out.addAll(classificatorConf);
			out.sort();
			return out ;
		}
		return classificatorConf ;
	}
	public void updateParam(int span, float weight, int newSpan, float newWeight) {
		for(ClassificatorConfElem elem : classificatorConf){
			if(elem.span==span&&elem.weight==weight){
				elem.span = newSpan ; elem.weight = newWeight;
				return ;
			}
		}
	}
	public void removeParam(ClassificatorConfElem elem) {
		for(int i =0 ; i <classificatorConf.size();i++){
			if(elem.span==classificatorConf.get(i).span&&elem.weight==classificatorConf.get(i).weight){
				classificatorConf.remove(i);
				return ;
			}
		}
	}

	protected static final XMLFormat<ElementClassificatorConf> XML = new XMLFormat<ElementClassificatorConf>(ElementClassificatorConf.class) {
		public void write(ElementClassificatorConf g, OutputElement xml) throws XMLStreamException {
			IConfigurationElement.XML.write(g,xml);
			for(ClassificatorConfElem val: g.classificatorConf){
				xml.add(val, "param",ClassificatorConfElem.class);
			}
		}
		public void read(InputElement xml, ElementClassificatorConf g) throws XMLStreamException {
			IConfigurationElement.XML.read(xml,g);
			while(xml.hasNext())
				g.classificatorConf.add((ClassificatorConfElem) xml.get("param",ClassificatorConfElem.class)) ;
		}
	};
}
