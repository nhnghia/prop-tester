/**
 * Jul 24, 2012 2:52:55 PM
 * @author nhnghia
 */
package fr.lri.proptester.global.testCase;

import org.junit.Test;

import fr.lri.proptester.global.Main;

public class TestMain {

	/**
	 * Test method for {@link fr.lri.proptester.global.Main#main(java.lang.String[])}.
	 */
	@Test
	public final void testMain() {
		
		String [] args = {"bin/testCase/gprop.xml", 
				"file:///Users/nhnghia/these/workshop/SIPP/testWithBaseX/vdict/vdic.xml",
				//"-b:8080",
				"-v"};
		Main.main(args);
	}

}
