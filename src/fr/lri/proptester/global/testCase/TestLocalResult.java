/**
 * Jul 27, 2012 11:58:56 PM
 * @author nhnghia
 */
package fr.lri.proptester.global.testCase;

import java.util.ArrayList;
import java.util.List;


import fr.lri.proptester.global.LocalResult;

public class TestLocalResult {

	public static void main(String[] args) throws Exception {
		List<String> lst = new ArrayList<String>();
		
		//for (int i=0; i<5; i++)
		{
			lst.add("http://www.lri.fr/~nhnghia/prop-tester/msg/localVdict?num=2&name=1&time");
			lst.add("http://www.lri.fr/~nhnghia/prop-tester/msg/localVdict?num=2&name=2&time");
			lst.add("http://www.lri.fr/~nhnghia/prop-tester/msg/localVdict?num=2&name=3&time");
		}
		
		//lst.add("http://www.lri.fr/~nhnghia/prop-tester/msg/localVdictt");
		
		LocalResult lr = new LocalResult(lst);
		String v;
		while (true){
			v = lr.getResult();
			if (v == null)
				break;
			System.out.println("A  " + v);
		}
		
		try{
			//Thread.sleep(2000);
		}catch(Exception ex){
			
		}
	}

}
