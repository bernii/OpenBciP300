package result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;

/**
 * Write results to hard drive log file
 * @author berni
 *
 */
public class HardDriveResult implements IResultListener {
	private File file;
	private FileOutputStream out;
	private OutputStreamWriter print;
	
	public HardDriveResult(String subjectName){
		Calendar cal = Calendar.getInstance();
		file = new File(System.getProperty("user.dir")+"\\"+
				cal.get(Calendar.YEAR)+"_"+cal.get(Calendar.MONTH)+"_"+
				cal.get(Calendar.DAY_OF_MONTH)+"_"+cal.get(Calendar.HOUR_OF_DAY)+"h"+cal.get(Calendar.MINUTE)+" "+subjectName+"_result.jp300");
		writeStart() ;
	}
	
	private void writeStart(){
		try {
			out = new FileOutputStream(file) ;
			print = new OutputStreamWriter(out);
		} catch(FileNotFoundException e) {
			System.err.println("Could not create file: " + file.getName());
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	private void write(String s){
		try {
			print.write(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void writeEnd(){
		try {
			print.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void resultArrived(String txt) {
		write(txt);
	}
	public void stopRecording(){
		writeEnd();
	}
}
