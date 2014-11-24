/**
 * Jan 7, 2013 3:53:38 PM
 * @author nhnghia
 */
package test;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeConverter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
		
		Date date = new Date();
		
		date.setTime(1318278956466l); //1318278951672);
		System.out.println(dateformat.format(date));
		
		date.setTime(1318278951672l);
		System.out.println(dateformat.format(date));
		
		date.setTime(1318278951673l);
		System.out.println(dateformat.format(date));
	}

}
