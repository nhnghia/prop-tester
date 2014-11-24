/**
 * Jun 20, 2012 5:58:30 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.testCase;

import java.util.List;

import org.junit.Test;

import fr.lri.proptester.local.property.Property;


public class TestProperty {

	/**
	 * Test method for {@link fr.lri.proptester.local.property.Property#getProperties(java.lang.String)}.
	 */
	@Test
	public final void testGetProperties() throws Exception{
		List<Property> lst = Property.getProperties("evaluation/hase12/client.prop.xml");
		fr.lri.schora.util.Debug.println("LIST OF PROPERTIES");
		
		System.out.print(fr.lri.schora.util.List.toString(lst, "\n"));
		System.out.println();
		System.out.println(lst.get(0).toXQuery());
	}

}
