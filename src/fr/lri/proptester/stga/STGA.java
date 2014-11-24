/**
 * Jul 25, 2012 1:20:44 PM
 * @author nhnghia
 */
package fr.lri.proptester.stga;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import fr.lri.schora.expr.ExprFactory;
import fr.lri.schora.expr.Variable;
import fr.lri.schora.util.Debug;
import fr.lri.schora.util.XML;


public class STGA {
	public List<State> states;
	public List<Transition> transitions;
	public State initState;
	public String name;
	
	
	/**
	 * Get an STGA from an XML file given by url
	 * @param url
	 * @return
	 * @throws Exception 
	 */
	public static STGA getSTGA(String url) throws Exception{
		InputStream in = new FileInputStream(new File(url));
		
		Element document =  DocumentBuilderFactory
			    .newInstance()
			    .newDocumentBuilder()
			    .parse(in)
			    .getDocumentElement();
		
		STGA stga = new STGA();
		
		stga.name = document.getAttribute("name");
		
		Element tmpNode = (Element) document.getElementsByTagName("states").item(0);
		NodeList nodes = tmpNode.getElementsByTagName("state");
		stga.states = new ArrayList<State>();
		int n = nodes.getLength();
		for (int i=0; i<n; i++){
			Element e = (Element) nodes.item(i);
			State s = new State();
			s.name = e.getAttribute("name");
			
			s.freeVariables = new ArrayList<Variable>();
			NodeList nl = e.getElementsByTagName("freeVariables");
			if (nl != null && nl.getLength() != 0){
				nl = ((Element) nl.item(0)).getElementsByTagName("variable");
				int d = nl.getLength();
				for (int j=0; j<d; j++)
					if (nl.item(j) instanceof Element){
						Variable v = ExprFactory.eINSTANCE.createVariable();
						String str = nl.item(j).getTextContent();
						v.setName(str);
						s.freeVariables.add(v);
					}
			}
			stga.states.add(s);
		}
		
		stga.initState = stga.getState(document.getAttribute("initstate"));
		
		
		tmpNode = (Element) document.getElementsByTagName("transitions").item(0);
		nodes = tmpNode.getElementsByTagName("transition");
		n = nodes.getLength();
		stga.transitions = new ArrayList<Transition>(n);
		
		for (int i=0; i<n; i++){
			Element e = (Element) nodes.item(i);
			Transition t = new Transition();
			
			String s = "";
			s=e.getElementsByTagName("source").item(0).getTextContent();
			t.sourceState = stga.getState(s);
			
			s=e.getElementsByTagName("destination").item(0).getTextContent();
			t.destinationState = stga.getState(s);
			
			s= getContentOfElement(e, "guard");
			t.guard = new Guard(s);
			
			Element e1 = (Element) e.getElementsByTagName("event").item(0);
			if (e1.getAttribute("type").equalsIgnoreCase("internal"))
				t.event = new Tau();
			else{
				Communication com = new Communication();
				com.sender = e1.getElementsByTagName("sender").item(0).getTextContent();
				com.receiver = e1.getElementsByTagName("receiver").item(0).getTextContent();
				com.operationName = e1.getElementsByTagName("name").item(0).getTextContent();
				String str = e1.getElementsByTagName("direction").item(0).getTextContent();
				if (str.equalsIgnoreCase("sending"))
					com.direction = CommunicationType.SENDING;
				else if(str.equalsIgnoreCase("reception"))
					com.direction = CommunicationType.RECEPTION;
				else
					com.direction = CommunicationType.INTERACTION;
				
				com.data = new Hashtable<String, String>();
				Element e2 = (Element) e1.getElementsByTagName("data").item(0);
				NodeList nLst = e2.getChildNodes();
				if (nLst != null){
					int d = nLst.getLength();
					for (int j=0; j<d; j++){
						if (!(nLst.item(j) instanceof Element))
							continue;
						Element e3 = (Element) nLst.item(j);
						com.data.put(e3.getTagName(), e3.getTextContent());
					}
				}
				t.event = com;
			}
			
			t.actions = new ArrayList<Action>();
			Element e2 = (Element) e.getElementsByTagName("actions").item(0);
			if (e2 != null){
				int d = e2.getChildNodes().getLength();
				
				for (int j=0; j<d; j++){
					String str = "action_" + (j+1);
					
					Node e3 = e2.getElementsByTagName(str).item(0);
					if (e3  instanceof Element)
						t.actions.add(new Action(e3.getTextContent()));
				}
			}
			
			stga.transitions.add(t);
		}
		
		
		return stga;
	}
	
	static String getContentOfElement(Element e, String tag){
		try{
			return e.getElementsByTagName(tag).item(0).getTextContent();
		}catch (Exception ex){
			return null;
		}
	}
	
	public State getState(String name) {
		for (State s : states)
			if (s.name.equalsIgnoreCase(name))
				return s;
		return null;
	}
	
	public void saveToFile(String url) throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter(url));
		String str = "<STGA name=\"" + name + "\" initstate=\"" + initState + "\">";
		str += "<states>";
		for (State s : states){
			str += String.format("<state name=\"%s\">", s.name);
			
			if (s.freeVariables != null && s.freeVariables.size() > 0){
				str += "<freeVariables>";
				for (Variable v : s.freeVariables)
					str += "<variable>" + v + "</variable>";
				str += "</freeVariables>";
			}
			
			str += "</state>";
		}
		str += "</states>";
		str += "<transitions>";
		for (Transition t : transitions){
			str += "<transition>";
			str += "<source>" + t.sourceState.name + "</source>";
			str += "<destination>" + t.destinationState.name + "</destination>";
			str += "<guard><![CDATA[" + t.guard + "]]></guard>";
			if (t.event instanceof Tau){
				str += "<event type = \"internal\">";
				str += "<name>tau</name>";
			}else{
				str += "<event type = \"external\">";
				Communication com = (Communication) t.event;
				str += String.format("<name>%s</name>", com.operationName);
				str += String.format("<sender>%s</sender>", com.sender);
				str += String.format("<receiver>%s</receiver>", com.receiver);
				str += String.format("<direction>%s</direction>", 
						((com.direction==CommunicationType.INTERACTION)?"interaction" : (
								(com.direction==CommunicationType.RECEPTION)?"reception":"sending") ));
				str += "<data>";
				Set<String> keys = com.data.keySet();
				for (String k : keys){
					
					str += String.format("<%s><![CDATA[%s]]></%s>", k, com.data.get(k), k);
				}
				str += "</data>";
			}
			str += "</event>";
			str += "<actions>";
			int i=0;
			for (Action a : t.actions){
				i ++;
				str += String.format("<action_%d><![CDATA[%s]]></action_%d>", i, a, i);
			}
			str += "</actions>";		
			str += "</transition>";
		}
		str += "</transitions>";
		str += "</STGA>";
		
		Debug.println(str);
		try {
			Element node =  DocumentBuilderFactory
				    .newInstance()
				    .newDocumentBuilder()
				    .parse(new ByteArrayInputStream(str.getBytes()))
				    .getDocumentElement();
			
			out.write(XML.node2String(node));
			out.flush();
		} catch (Exception e) {
			Debug.print(e);
		}
	}
	
	/**
	 * get all outgoing transitions form state s
	 * @param s
	 * @return
	 */
	public List<Transition> getOutgoingTransitions(State s){
		List<Transition> lst = new ArrayList<Transition>();
		for (Transition t : transitions)
			if (t.sourceState.equals(s))
				lst.add(t);
		
		return lst;
	}
}
