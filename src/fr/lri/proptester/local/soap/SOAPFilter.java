/**
 * Jun 20, 2012 11:27:50 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.soap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import fr.lri.schora.util.XML;

/**
 * Filter stream of SOAP message, then create a stream of messages
 */
public class SOAPFilter {
	BufferedReader xml;
	String msgStr;
	String endSoapRegex;
	Pattern partern;
	
	public SOAPFilter(InputStream soapStream) throws Exception{
		xml = new BufferedReader(new InputStreamReader(soapStream));
		msgStr = "";
		endSoapRegex = "(</)(\\w*)(\\s*)(:+)(\\s*)(Envelope)(\\s*)(>)";
		partern = Pattern.compile(endSoapRegex);
	}
	
	public String getMessage() throws SAXException, IOException, ParserConfigurationException{
		if (!xml.ready())
			return null;

		Matcher matcher = null;
		// Read un message from xml
		String str;
		while (true) {
			str = xml.readLine();
			if (str == null) {
				break;
			}
			msgStr += str + "\n";

			matcher = partern.matcher(msgStr);

			if (msgStr.contains("<?xml") && matcher.find()) {
				break;
			}
		}
		// Since we use readline ==> msgStr may contains more than a message
		// Extract a SOAP message
		int d = msgStr.indexOf("<?xml");
		if (d == -1) {
			return null;
		}
		int dd = matcher.end();

		String msg = msgStr.substring(d, dd);
		msgStr = msgStr.substring(dd);

		Element soapDoc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(new ByteArrayInputStream(msg.getBytes()))
				.getDocumentElement();
		Element header = (Element) soapDoc.getElementsByTagName("soap:Header")
				.item(0);
		Element body = (Element) soapDoc.getElementsByTagName("soap:Body")
				.item(0);
		String bodyStr = XML.node2String(body);
		bodyStr = bodyStr.substring(bodyStr.indexOf("<", 2),
				bodyStr.lastIndexOf(">", bodyStr.length() - 3) + 1);

		msg = "";
		if (header == null) {
			msg = "\n<message>\n";
		} else {
			Node ele = header.getElementsByTagName("Info").item(0);
			NamedNodeMap attributes = ele.getAttributes();
			str = "";
			for (int g = 0; g < attributes.getLength(); g++) {
				Attr attr = (Attr) attributes.item(g);
				str += attr.getName() + "=\"" + attr.getValue() + "\" ";
			}

			msg = "\n<message " + str + ">\n";
		}
		msg += bodyStr;
		msg += "\n</message>";
		return msg;
	}

}
