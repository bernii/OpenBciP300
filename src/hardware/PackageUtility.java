package hardware;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * Dynamically load data drivers from jar (they have to implement IProtocol)
 * @author berni
 *
 */
public class PackageUtility {
	static String jarName = "jEEG.jar" ;
	public static Class[] getClasses(String packageName) throws ClassNotFoundException{
  ArrayList<Class> classes = new ArrayList<Class> ();

  packageName = packageName.replaceAll("\\." , "/");
  File f = new File(jarName);
  if(f.exists()){
	  try{
	    JarInputStream jarFile = new JarInputStream(new FileInputStream (jarName));
	    JarEntry jarEntry;
	
	    while(true) {
	      jarEntry=jarFile.getNextJarEntry ();
	      if(jarEntry == null){
	        break;
	      }
	      if((jarEntry.getName ().startsWith (packageName)) &&
	           (jarEntry.getName ().endsWith (".class")) ) {
	    	  classes.add(Class.forName(jarEntry.getName().replaceAll("/", "\\.").substring(0, jarEntry.getName().length() - 6)));
	      }
	    }
	  }
	  catch( Exception e){
	    e.printStackTrace ();
	  }
	  	Class[] classesA = new Class[classes.size()];
		classes.toArray(classesA);
		return classesA;
  }else
	  return getClassesDir(packageName);
}
	
	public static Class[] getClassesDir(String pckgname)
			throws ClassNotFoundException {
		ArrayList<Class> classes = new ArrayList<Class>();
		// Get a File object for the package
		File directory = null;
		try {
			ClassLoader cld = Thread.currentThread().getContextClassLoader();
			if (cld == null) {
				throw new ClassNotFoundException("Can't get class loader.");
			}
			String path =  pckgname.replace('.', '/');
			
			URL resource = cld.getResource(path);
			if (resource == null) {
				throw new ClassNotFoundException("No resource for " + path);
			}
			directory = new File(resource.getFile());
		} catch (NullPointerException x) {
			throw new ClassNotFoundException(pckgname + " (" + directory
					+ ") does not appear to be a valid package");
		}
		if (directory.exists()) {
			// Get the list of the files contained in the package
			String[] files = directory.list();
			for (int i = 0; i < files.length; i++) {
				// we are only interested in .class files
				if (files[i].endsWith(".class")) {
					// removes the .class extension
					classes.add(Class.forName(pckgname + '.'
							+ files[i].substring(0, files[i].length() - 6)));
				}
			}
		} else {
			throw new ClassNotFoundException(pckgname
					+ " does not appear to be a valid package");
		}
		Class[] classesA = new Class[classes.size()];
		classes.toArray(classesA);
		return classesA;
		}

	
}

