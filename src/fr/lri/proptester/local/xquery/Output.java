package fr.lri.proptester.local.xquery;
import java.io.IOException;
import java.io.OutputStream;

import fr.lri.schora.util.Broadcast;


/**
 * Jun 20, 2012 3:51:40 PM
 * @author nhnghia
 */

public class Output extends OutputStream {
	Broadcast broadcast;
	boolean isOut2Console;
	String name;
	boolean isConvert;
	
	boolean hasBeenPrinted;
	
	long nPass, nFail;
	OutputStream output;
	
	public Output(String name){
		output = null;
		isOut2Console = true;
		this.name = name;
		isConvert = true;
		hasBeenPrinted = false;
		
		nPass = nFail = 0;
	}
	
	public Output(String name, OutputStream out){
		isOut2Console = true;
		output = out;
		this.name = name;
		isConvert = true;
		hasBeenPrinted = false;
		
		nPass = nFail = 0;
	}
	
	public Output(Broadcast bc, String name, boolean isConvert) throws IOException{
		broadcast = bc;
		isOut2Console = false;
		this.name = name;
		this.isConvert = isConvert;
		hasBeenPrinted = false;
		
		nPass = nFail = 0;
	}
	
	@Override
	public void write(int b) throws IOException {
		String str = String.format("%c", b);
		if (isOut2Console)
			System.out.print(str);
		else
			broadcast.broadcast(str);
		hasBeenPrinted = true;
	}
	
	@Override
	public void write(byte[] b) throws IOException{
		write(b, 0, b.length);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException{
		String str = new String(b);
		str = str.substring(off, off + len);

		//tools.Debug.println(str);
		hasBeenPrinted = true;
		///*
		if (len > 3){
			if (str.indexOf("true") > -1){
				str = str.replace("true", "pass");
				nPass ++;
			}
			else if (str.indexOf("false") > -1){
				str = str.replace("false", "fail");
				nFail ++;
			}
			//str = str.replace("message>", "message>\n");
		}
		//*/
		
		if (isOut2Console){
			if (output == null){
				System.out.print(str);
				if (len > 5)
					System.out.flush();
			}
			else{
				output.write(str.getBytes());
				output.flush();
			}
		}else
			broadcast.broadcast(str);
		
	}
	
	public void printStat(){
		String str = "";
		if (!hasBeenPrinted){
			String time = String.format("%d", System.currentTimeMillis());
			str = String.format("\n<message prop=\"%s\" tstamp=\"%s\">inconclusive</message>", name, time);
		}
		
		str += String.format("\n\n<stat pass=\"%d\" fail=\"%d\"/>", nPass, nFail);
		
		if (isOut2Console){
			System.out.print(str);
			System.out.flush();
		}else{
			broadcast.broadcast(str);
		}
	}
}
