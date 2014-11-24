/**
 * Jul 24, 2012 12:58:20 AM
 * @author nhnghia
 */
package fr.lri.proptester.global.property;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class Property {
	public List<String> context;
	public List<String> consequence;
	public String name;
	public boolean isPositive;
	
	
	/**
	 * name ::= preamble ----> consequent
	 */
	public Property(){
		context = new ArrayList<String>();
		consequence = new ArrayList<String>();
		name = "";
	}
	
	
	/**
	 * get a list of properties from an XML file <code>path</code><br>
	 * @param path
	 * @return
	 * @throws ParseException
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static List<Property> getProperties(String path) throws SAXException, IOException, ParserConfigurationException{
	    File reader =  new File(path);
	    Element propDoc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(reader)
				.getDocumentElement();
	    
	    //Add property
	    NodeList props = propDoc.getElementsByTagName("property");
	    int n = props.getLength();
	    List<Property> lst = new ArrayList<Property>();
	    for (int i=0; i<n; i++){
	    	Element el = (Element) props.item(i);
	    	Property p = new Property();
	    	p.name = el.getAttribute("name");
	    	p.isPositive = el.getAttribute("type").equals("positive");
	    	
	    	Element preambleEl = (Element) el.getElementsByTagName("context").item(0);
	    	NodeList nodeLst = preambleEl.getElementsByTagName("name");
	    	for (int j=0; j<nodeLst.getLength(); j++){
	    		String str = nodeLst.item(j).getTextContent();
	    		if (p.context.contains(str)){
	    			System.out.println(String.format("[%s] exists in the context. I pass.", str));
	    		}
	    		else
	    			p.context.add(str);
	    	}
	    	
	    	Element consequentEl = (Element) el.getElementsByTagName("consequence").item(0);
	    	nodeLst = consequentEl.getElementsByTagName("name");
	    	for (int j=0; j<nodeLst.getLength(); j++){
	    		String str = nodeLst.item(j).getTextContent();
	    		if (p.consequence.contains(str)){
	    			System.out.println(String.format("[%s] exists in the consequence. I pass.", str));
	    		}
	    		/*else if (p.context.contains(str) && p.isPositive == false){
	    			tools.Print.error(String.format("[%s] exists in the context but it must not appear in the consequence. I cannot verify this property.", str));
	    		}*/
	    		else
	    			p.consequence.add(str);
	    	}
	    	
	    	lst.add(p);
	    }
		return lst;
	}
	
	/**
	 * create an xquery on stream mode: "declare variable $stream external;"
	 * @return
	 */
	public String toXQuery(){
		String str = toXQueryOnWindow();
		if (str.length() == 0)
			return "";
		str = str.replace("/log", "$stream");
		
		str = "declare variable $stream external;\n" + str; 
 		return str;
	}
	
	/**
	 * create an XQuery which will be applied on a fix xml document <br/>
	 * Use {@link #toXQuery()} to get xquery which can be use in stream mode
	 * @return
	 */
	public String toXQueryOnWindow(){
		if (context == null || consequence == null || context.size() == 0 || consequence.size() == 0)
			return "";
		
		List<String> contextSql = new ArrayList<String>(context.size());
		for (String p : context){
			String sql = String.format("(some $e in /log/message satisfies ($e/@msg=\"%s\" and data($e) = \"pass\") )", p);
			contextSql.add(sql);
		}
		
		List<String> consequenceSql = new ArrayList<String>(consequence.size());
		for (String p : consequence){
			String sql = String.format("(some $q in /log/message satisfies ($q/@msg=\"%s\" and data($q) = \"pass\") )", p);
			consequenceSql.add(sql);
		}

		String str = String.format("if (\n   %s\n) then (\n   %s \n) else ()" , 
				fr.lri.schora.util.List.toString(contextSql, " and \n   "),
				fr.lri.schora.util.List.toString(consequenceSql, " or \n   "));
		return str;
	}
	
	public String toString(){
		String s = "";
		if (isPositive)
			s= "";
		else
			s = "not";
		String str = String.format("%s ::= {%s} |====> %s {%s}", name, 
				fr.lri.schora.util.List.toString(context, ","),
				s,
				fr.lri.schora.util.List.toString(consequence, ","));
		return str;
	}
}
