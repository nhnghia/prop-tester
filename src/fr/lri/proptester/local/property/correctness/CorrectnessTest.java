/**
 * Jul 28, 2012 11:26:50 AM
 * @author nhnghia
 */
package fr.lri.proptester.local.property.correctness;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import fr.lri.proptester.stga.STGA;
import fr.lri.proptester.local.property.Property;

public class CorrectnessTest {

	/**
	 * Test method for {@link fr.lri.proptester.local.property.correctness.Correctness#isCorrect(fr.lri.proptester.local.property.Property, fr.lri.proptester.stga.STGA, int)}.
	 * @throws Exception 
	 */
	@Test
	public final void testIsCorrect() throws Exception {
		List<Property> lst = Property.getProperties("bin/testCase/ad.prop.xml");
		
		STGA stga = STGA.getSTGA("bin/testCase/accounting.stga.xml");
		Correctness cor = new Correctness();
		assertTrue(cor.isCorrect(lst.get(0), stga, 10, "/Users/nhnghia/soft/z3/bin/z3"));
	}

}
