/**
 * Jul 28, 2012 8:52:45 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.property.correctness;

import org.junit.Test;

public class PropCheckerTest {

	/**
	 * Test method for {@link fr.lri.proptester.local.property.correctness.PropChecker#main(java.lang.String[])}.
	 */
	@Test
	public final void testMain() {
		String [] arv = {"bin/testCase/ad.prop.xml", "bin/testCase/accounting.stga.xml", "10", "/Users/nhnghia/soft/z3/bin/z3"};
		PropChecker.main(arv);
	}

}
