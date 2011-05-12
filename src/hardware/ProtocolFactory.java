package hardware;

import javolution.util.FastList;

public class ProtocolFactory {

	private static IProtocol protocol ;
	private static IProtocol[] protocols;
	
	public static IProtocol get(String protocolType) {
		if(protocol==null)
			protocol = create(protocolType);
		else{
			if(protocol.getType().equalsIgnoreCase(protocolType))
				return protocol;
			else
				protocol = create(protocolType);
		}
		return protocol;
	}
	
	private static IProtocol create(String protocolType){
		for(IProtocol proto: getProtocols()){
			if(proto.getType()!=null&&proto.getType().equalsIgnoreCase(protocolType))
				try {
					return proto.getClass().newInstance() ;
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
	
	public static IProtocol[] getProtocols(){
		if(protocols==null){
			Class[] classes = null;
			
			try {
				classes = PackageUtility.getClasses("hardware");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}
			
			FastList<IProtocol> protoArr = new FastList<IProtocol>();
			for (Class<IProtocol> cl : classes) {
				Class<?>[] interf = cl.getInterfaces();
				if(interf.length==0)
					continue;
				else if(IProtocol.class.toString().contains(interf[0].getName())){
					IProtocol proto = null;
					try {
						proto = cl.newInstance();
					} catch (InstantiationException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
					protoArr.add(proto);
				}
			}
			protocols = protoArr.toArray(new IProtocol[protoArr.size()]);
		}
		return protocols ;
	}
}
