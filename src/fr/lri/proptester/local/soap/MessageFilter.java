/**
 * Jul 20, 2012 4:16:21 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.soap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MessageFilter {
	String msgStr = "";
	BufferedReader xml;
	public MessageFilter(InputStream in){
		xml = new BufferedReader(new InputStreamReader(in));
	}
	
	public String getMessage() throws IOException{
		String str = null;
		
		while (!(msgStr.contains("<message") && msgStr.contains("</message"))){
			str = xml.readLine();
			if (str == null)
				break;
			msgStr += str + "\n";
		}
		if (str == null)
			return null;
		//Since we use readline ==> msgStr may contains more than a message
		int d = msgStr.indexOf("</message");
		if (d == -1)
			return null;
		//Extract un message
		d = msgStr.indexOf(">", d) + 1;
		String msg = msgStr.substring(msgStr.indexOf("<message"), d);
		msgStr = msgStr.substring(d);
		return msg;
	}
}
