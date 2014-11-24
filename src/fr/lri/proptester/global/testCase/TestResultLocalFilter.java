/**
 * Jul 27, 2012 9:39:48 PM
 * @author nhnghia
 */
package fr.lri.proptester.global.testCase;


import java.io.IOException;

import org.junit.Test;

import fr.lri.proptester.global.ResultLocalFilter;

public class TestResultLocalFilter {

	@Test
	public final void test() throws IOException, InterruptedException {
		ResultLocalFilter ft = new ResultLocalFilter("http://www.lri.fr/~nhnghia/prop-tester/msg/localVdict?num=3@name=1");
		while (!ft.isEndOfStream()){
			String s = ft.getMessage();
			if (s != null)
				System.out.println("\nA " + s);
			System.out.flush();
		}
	}

}
