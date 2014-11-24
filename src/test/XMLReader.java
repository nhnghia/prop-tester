/**
 * Jun 25, 2012 4:22:54 PM
 * @author nhnghia
 */
package test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XMLReader {
	public static void main (String[] args) throws Exception{
		XMLInputFactory factory = XMLInputFactory.newInstance();

		try {
			//get Reader connected to XML input from somewhere..
			URL u = new URL("http://localhost/~nhnghia/these/streamXML1.php");
			InputStream stream = u.openStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		    XMLEventReader eventReader =
		        factory.createXMLEventReader(reader);
		    
		    while(eventReader.hasNext()){

		        XMLEvent event = eventReader.nextEvent();

		        if(event.getEventType() == XMLStreamConstants.START_ELEMENT){
		            StartElement startElement = event.asStartElement();
		            System.out.println(startElement.getName().getLocalPart());
		            System.out.flush();
		        }
		        //handle more event types here...
		    }
		    
		} catch (XMLStreamException e) {
		    e.printStackTrace();
		}

	}
}
