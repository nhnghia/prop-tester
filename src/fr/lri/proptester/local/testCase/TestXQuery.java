/**
 * Jun 22, 2012 1:20:10 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.testCase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.Test;

import fr.lri.proptester.local.xquery.MXQuery;
import fr.lri.proptester.local.xquery.Output;


public class TestXQuery {

	/**
	 * Test method for {@link fr.lri.proptester.local.xquery.MXQuery#exec(java.io.Reader, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public final void testExec()  {
		try{
			String queryStr = 
					" declare variable $stream external; " +
					" for $result in (" +
					" for tumbling window $w in $stream//message" +
					" 		start $s at $spos when fn:true()" +
					"		end   $e at $epos when $epos - $spos eq 2" +
					" return <result>{$w}</result>" +
					")" +
					" return " +
					"	some $e in $result//message satisfies $e/Request/@id = 3 and $e/position() eq 1";
			
			//queryStr = 
			//		" for $s in $stream//message" +
			//		" return $s";
			MXQuery query = new MXQuery(queryStr, new Output(""));

			URL u = new URL("http://localhost/~nhnghia/these/streamXML1.php");
			InputStream stream = u.openStream();
			BufferedReader xml = new BufferedReader(new InputStreamReader(stream));
			// continuously add events...
			String msgStr = "";
			while (true) {
				//Read un message from xml
				String str = null;
				while (!(msgStr.contains("<message") && msgStr.contains("</message"))){
					str = xml.readLine();
					if (str == null)
						break;
					msgStr += str + "\n";
				}
				if (str == null)
					break;
				//Since we use readline ==> msgStr may contains more than a message
				int d = msgStr.indexOf("</message");
				if (d == -1)
					continue;
				//Extract un message
				d = msgStr.indexOf(">", d) + 1;
				String msg = msgStr.substring(msgStr.indexOf("<message"), d);
				msgStr = msgStr.substring(d);
				
				query.putData(msg);
			}
			//query.putData(null);
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

}
