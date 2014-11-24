/**
 * Jul 24, 2012 1:10:25 AM
 * @author nhnghia
 */
package fr.lri.proptester.global.testCase;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import fr.lri.proptester.global.property.Property;

public class TestProperty {

	/**
	 * Test method for {@link fr.lri.proptester.global.property.Property#getProperties(java.lang.String)}.
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	@Test
	public final void testGetProperties() throws SAXException, IOException, ParserConfigurationException {
		List<Property> lst = Property.getProperties("bin/testCase/gprop.xml");
		for (Property p : lst){
			System.out.println(p.toString());
			System.out.println(p.toXQuery());
		}
	}

}
