/**
 * Jul 26, 2012 11:25:29 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.testCase;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.lri.proptester.local.property.correctness.Solver;
import fr.lri.schora.expr.Condition;
import fr.lri.schora.expr.util.BoolExpression;

public class TestSolver {

	/**
	 * Test method for {@link fr.lri.proptester.local.property.correctness.Solver#isSatisfy(fr.lri.schora.expr.Condition)}.
	 * @throws Exception 
	 */
	@Test
	public final void testIsSatisfy() throws Exception {
		Solver solver = new Solver("/Users/nhnghia/soft/z3/bin/z3");
		Condition con = BoolExpression.parser("x*y < 1");
		assertTrue(solver.isSatisfy(con));
	}

}
