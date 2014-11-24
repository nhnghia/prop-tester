/**
 * Jun 29, 2012 11:41:28 AM
 * @author nhnghia
 */
package fr.lri.proptester.local.testCase;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.junit.Test;

import fr.lri.proptester.local.property.Property;
import fr.lri.proptester.local.soap.SOAPFilter;
import fr.lri.proptester.local.xquery.MXQuery;
import fr.lri.proptester.local.xquery.Output;


public class TestXQueryOnSOAPStream {

	/**
	 * Test method for {@link fr.lri.proptester.local.xquery.MXQuery#exec(java.io.InputStream, java.lang.String, java.io.PrintStream)}.
	 */
	@Test
	public final void testExec() {
		try{
			URL u = new URL("http://localhost/~nhnghia/these/HASE2012/quotation?num=1000");
			final InputStream stream = u.openStream();
			
			String queryStr = getQuery();
			
			/*
			queryStr = 
					" for $s in $stream//message" +
					" return $s";
			/*
			queryStr = 
					//" for $result in (" +
					" declare namespace m=\"http://www.example.org/stock\";" +
					"\n declare variable $stream external; " +
					"\n for tumbling window $w in $stream//message" +
					"\n 		start $s at $spos when $s/@source eq \"a\" and $s/@destination eq \"b1\" and $s/@direction eq \"SEND\"" +
					"\n			and $s/m:GetStockPrice/m:StockName eq \"IBM\"" +
					"\n		end   $e at $epos when $epos - $spos eq 3" +
					"\n return <result>{$w}</result>" 
					//")" +
					//" return $result/message[2]"
					;
			//*/
			System.out.println(queryStr);
			
			MXQuery query = new MXQuery(queryStr, new Output(""));
			SOAPFilter filter = new SOAPFilter(stream);
			String msg;
			while ( (msg = filter.getMessage()) != null){
				fr.lri.schora.util.Debug.println(msg);
				query.putData(msg);
			}
			
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}
	
	String getQuery() throws Exception{
		List<Property> lst = Property.getProperties("bin/testCase/prop.xml");
		return lst.get(0).toXQuery();
	}
}
