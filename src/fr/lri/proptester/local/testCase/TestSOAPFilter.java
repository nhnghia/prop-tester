/**
 * Jun 28, 2012 4:56:09 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.testCase;

import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

import fr.lri.proptester.local.soap.SOAPFilter;
import fr.lri.schora.util.Debug;


public class TestSOAPFilter {

	/**
	 * Test method for {@link fr.lri.proptester.local.soap.SOAPFilter#exec(java.io.InputStream, java.io.OutputStream)}.
	 */
	@Test
	public final void testExec() {
		try{
			URL u = new URL("http://localhost/~nhnghia/these/soap.php");
			InputStream stream = u.openStream();
			
			SOAPFilter filter = new SOAPFilter(stream);
			String msg;
			while ( (msg = filter.getMessage()) != null)
				Debug.println(msg);
		}catch (Exception ex){
			ex.printStackTrace();
		}
	}

}
