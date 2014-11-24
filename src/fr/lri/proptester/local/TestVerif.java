package fr.lri.proptester.local;
import org.junit.Test;

/**
 * Jul 20, 2012 4:38:08 PM
 * @author nhnghia
 */

public class TestVerif {

	/**
	 * Test method for {@link Main#smain(java.lang.String[])}.
	 */
	@Test
	public final void testSmain() {
		String [] arv = {"evaluation/properties_quotation.xml", "http://localhost/~nhnghia/these/HASE2012/quotation?num=2000"};
		Main.main(arv);
	}

}
