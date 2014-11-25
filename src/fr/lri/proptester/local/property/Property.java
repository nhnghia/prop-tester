/**
 * Jun 20, 2012 4:02:25 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.property;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fr.lri.proptester.stga.Communication;
import fr.lri.proptester.stga.CommunicationType;
import fr.lri.schora.expr.Condition;
import fr.lri.schora.expr.ExprFactory;
import fr.lri.schora.expr.Variable;
import fr.lri.schora.expr.util.BoolExpression;


public class Property {

	public List<CandidateEvent> context;
	public List<CandidateEvent> consequence;
	public String name;
	public boolean isPositive, isUsedByGlobalProperty;
	public int numberOfMessage;
	public Hashtable<String, String> namespaces;
	
	/**
	 * name ::= preamble --4--> consequent
	 */
	public Property(){
		context = new ArrayList<CandidateEvent>();
		consequence = new ArrayList<CandidateEvent>();
		namespaces = new Hashtable<String, String>();
		
		name = "";
	}
	
	
	/**
	 * get a list of properties from an XML file <code>path</code><br>
	 * @param path
	 * @return
	 * @throws Exception 
	 */
	public static List<Property> getProperties(String path) throws Exception{
	    File reader =  new File(path);
	    Element propDoc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.parse(reader)
				.getDocumentElement();
	    
	    NodeList names = propDoc.getElementsByTagName("namespace");
	    int n = names.getLength();
	    Hashtable<String, String> ns = new Hashtable<String, String>(n);
	    for (int i=0; i<n; i++){
	    	Element e = (Element) names.item(i);
	    	ns.put(e.getAttribute("prefix"), e.getAttribute("uri"));
	    }
	    
	    //Add property
	    NodeList props = propDoc.getElementsByTagName("property");
	    n = props.getLength();
	    List<Property> lst = new ArrayList<Property>();
	    for (int i=0; i<n; i++){
	    	Element el = (Element) props.item(i);
	    	Property p = new Property();
	    	p.name = el.getAttribute("name");
	    	p.isPositive = el.getAttribute("type").equals("positive");
	    	p.isUsedByGlobalProperty = el.getAttribute("usedInGlobalProperty").equals("true");
	    	
	    	p.numberOfMessage = Integer.parseInt(el.getAttribute("distance"));
	    	
	    	Element preambleEl = (Element) el.getElementsByTagName("context").item(0);
	    	//p.preambleServiceName = preambleEl.getAttribute("service");
	    	try{
	    		p.context = getCandidateEvents(preambleEl);
	    	}catch(Exception ex){
	    		throw new Exception("Property [" + p.name + "]: " + ex.getMessage());
	    	}
	    	Element consequentEl = (Element) el.getElementsByTagName("consequence").item(0);
	    	//p.consequentServiceName = consequentEl.getAttribute("service");
	    	try{
	    		p.consequence = getCandidateEvents(consequentEl);
	    	}catch(Exception ex){
	    		throw new Exception("Property [" + p.name + "]: " + ex.getMessage());
	    	}
	    	p.namespaces = ns;
	    	
    		p.verifyFormatOfProperty();
    		lst.add(p);
	    	
	    }
	    
		return lst;
	}
	
	Hashtable<String, String> variableLables;
	
	public void verifyFormatOfProperty() throws Exception{
		int n = context.size();
		for (int i=0; i<n; i++)
			context.get(i).name = "$e" + (i+1);
		n = consequence.size();
		for (int i=0; i<n; i++)
			consequence.get(i).name = "$q" + (i+1);
		
			variableLables = new Hashtable<String, String>();
			
			for (CandidateEvent ce : context){
				 Set<String> keys = ce.event.data.keySet();
				 for (String k : keys){
					 if (variableLables.containsKey(k)){
						 throw new Exception(String.format("Duplicate variable [%s] was used to point to label [%s]", k, variableLables.get(k)));
					 }{
						 variableLables.put(k, String.format("%s/%s", ce.name, ce.event.data.get(k)));
					 }
				 }
				 
			}
			for (CandidateEvent ce : consequence){
				 Set<String> keys = ce.event.data.keySet();
				 for (String k : keys){
					 if (variableLables.containsKey(k)){
						 throw new Exception(String.format("Duplicate variable [%s] was used to point to label [%s]", k, variableLables.get(k)));
					 }{
						 String str = String.format("%s/%s", ce.name, ce.event.data.get(k));
						 //tools.Debug.println(str);
						 variableLables.put(k, str);
					 }
				 }
			}
	}
	
	
	static List<CandidateEvent> getCandidateEvents(Element el) throws Exception{
		List<CandidateEvent> lst = new ArrayList<CandidateEvent>();
		NodeList nodeLst = el.getChildNodes();
		int n = nodeLst.getLength();
		for (int i=0; i<n; i++){
			Node nl = el.getElementsByTagName("candidateEvent").item(i);
			if (!(nl instanceof Element))
				continue;
			Element e =  (Element) nl;
			
			String predicate = "true";
			Element ee = (Element) e.getElementsByTagName("predicate").item(0);
			predicate  = ee.getTextContent();
			//Debug.print(predicate);
			Condition con = BoolExpression.parser(predicate);
			
			
			Element e1 = (Element) e.getElementsByTagName("event").item(0);
			Communication com = new Communication();
			com.sender = e1.getAttribute("source");
			com.receiver = e1.getAttribute("destination");
			com.operationName = e1.getAttribute("name");
			String str = e1.getAttribute("direction");
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
					String v = e3.getTagName();
					if (com.data.containsKey(v)){
						throw new Exception("Variable [" + v + "] has been already defined");
					}
					else
						com.data.put(v, e3.getTextContent());
				}
			}
			
			CandidateEvent ce = new CandidateEvent(com, con);

			lst.add(ce);
		}
		return lst;
	}
	
	
	@Override
	public String toString(){
		String s = "";
		if (isPositive)
			s= "";
		else
			s = "not";
		String str = String.format("%s ::= <%s> ==%d==> %s {%s}", name, 
				fr.lri.schora.util.List.toString(context, ","),
				numberOfMessage,
				s,
				fr.lri.schora.util.List.toString(consequence, ","));
		return str;
	}
	
	/**
	 * get a complete XQuery with sliding on window
	 * @return
	 * @throws Exception
	 */
	public String toXQuery()  throws Exception{
		//hashTable = getVariableLable();
		
		if (context == null || consequence == null || context.size() == 0 || consequence.size() == 0)
			return "";
		
		if (numberOfMessage < 0)
			return toXQueryNeg();
		
		String ns = "";
		Set<String> keys = namespaces.keySet();
		for (String k : keys)
			ns = String.format("%s declare namespace %s=\"%s\";\n", ns, k, namespaces.get(k));
		
		String preambleStr = "";
		int n = context.size();
		String spc = "";
		for (int i=0; i<n; i++){
			CandidateEvent ce = context.get(i);
			
			preambleStr = String.format(
					"\n %s" +
					"\n %s for %s in $w[%d] " +
					"\n %s where %s " +
					"\n %s return ",
					preambleStr,
					spc, ce.name, (i+1),
					spc, event2XQuery(ce),
					spc
					);
			spc += "   ";
		}
		
		String qStr = ns +  
				"\n declare variable $stream external; " +
				//"\n for $w in (" +
				"\n for sliding window $w in $stream//message" +
				getFirstCondition() + 
				"\n end   $e  at $epos when $epos - $spos eq " + (getLength()-1) +
				//"\n 	return <window>{$win}</window>" +
				//"\n )" +
				" return " +
				preambleStr +
				"\n <message" +
						  " prop=\""+ this.name +"\" " +
						  " tstamp=\"{current-dateTime()}\" " +
						  " msg=\"{" + context.get(0).name + "/@tstamp}\">\n" +
						  "{" +
				(isPositive? "" : "\n not (") +
				 getXConsequent(spc) 
				+
				(isPositive? "" : ")")  +
				"\n}</message>"
				;
		return qStr;
	}
	
	/**
	 * translate to xquery when numberOfMessage < 0
	 * @return
	 * @throws Exception
	 */
	private String toXQueryNeg()  throws Exception{
		//hashTable = getVariableLable();
		
		if (context == null || consequence == null || context.size() == 0 || consequence.size() == 0)
			return "";
		
		String ns = "";
		Set<String> keys = namespaces.keySet();
		for (String k : keys)
			ns = String.format("%s declare namespace %s=\"%s\";\n", ns, k, namespaces.get(k));
		
		String preambleStr = "";
		int n = context.size();
		String spc = "";
		for (int i=0; i<n; i++){
			CandidateEvent ce = context.get(i);
			
			preambleStr = String.format(
					"\n %s" +
					"\n %s for %s in $w[%d] " +
					"\n %s where %s " +
					"\n %s return ",
					preambleStr,
					spc, ce.name, (i+1),
					spc, event2XQuery(ce),
					spc
					);
			spc += "   ";
		}
		
		String qStr = ns +  
				"\n declare variable $stream external; " +
				"\n let $win  := (for sliding window $win in $stream//message" +
				getFirstCondition() + 
				"\n         end   $e at $epos when $epos - $spos eq " + (getLength()-1) +
				"\n         return <window>{$win}</window>)" +
				"\n for sliding window $w in $win " +
				"\n   start at $s when $s > 1" +
				"\n   end   at $e when $e - $s eq " + (context.size() - 1) + 
				"\n return " +
				preambleStr +
				"\n <message" +
				" prop=\""+ this.name +"\" " +
				" tstamp=\"{current-dateTime()}\" " +
				" msg=\"{" + context.get(0).name + "/@tstamp}\">" +
				"\n{" +
				(isPositive? "" : "\n not (") +
				getXConsequentNeg(spc)
				+
				(isPositive? "" : ")")  +
				"\n }</message>"
				;
		return qStr;
	}
	
	protected String getXConsequentNeg(String space)  throws Exception{
		List<String> lst = new ArrayList<String>();
		for (CandidateEvent ce : context){
			String str = String.format(
					"\n%s (some %s in $win[0] satisfies (%s))",
					space,
					ce.name, 
					event2XQuery(ce),
					ce.name);
			lst.add(str);
		}
		return fr.lri.schora.util.List.toString(lst, " or ");
	}
	
	protected String getXConsequent(String space)  throws Exception{
		int psize = context.size();
		List<String> lst = new ArrayList<String>();
		for (CandidateEvent ce : consequence){
			String str = String.format(
					"\n%s (some %s in $w[position() > %d] satisfies (%s))",
					space,
					ce.name, 
					psize,
					event2XQuery(ce),
					ce.name);
			lst.add(str);
		}
		return fr.lri.schora.util.List.toString(lst, " or ");
	}

	protected String getFirstCondition()  throws Exception{
		if (numberOfMessage >= 0){
			CandidateEvent ce = context.get(0);
			return String.format("\n start %s at $spos when (%s)", 
				ce.name, event2XQuery(ce));
		}else{
			String cond = null;
			for (CandidateEvent ce : consequence){
				String tmp = ce.name;
				ce.name = "$s";
				
				Condition con = ce.predicate;
				ce.predicate = ExprFactory.eINSTANCE.createBTrue();
				
				if (cond == null)
					cond = event2XQuery(ce);
				else
					cond = cond + " or "+ event2XQuery(ce);
				
				ce.predicate = con;
				ce.name = tmp;
			}
			return String.format("\n 		start $s at $spos when (%s)", 
					cond);
		}
	}
	
	/**
	 * 
	 * @return window size = size(context) + distance
	 */
	public int getLength(){
		return context.size() + Math.abs(numberOfMessage);
	}
	
	public String event2XQuery(CandidateEvent ce)  throws Exception{
		String var = ce.name;
		
		List<String> s = new ArrayList<String>();
		s.add(format("%s/@name eq \"%s\"", var, ce.event.operationName));
		s.add(format("%s/@source eq \"%s\"", var, ce.event.sender));
		s.add(format("%s/@destination eq \"%s\"", var, ce.event.receiver));
		s.add(format("%s/@direction eq \"%s\"", var, ((ce.event.direction == CommunicationType.RECEPTION)? "reception" : 
			(ce.event.direction == CommunicationType.SENDING)?"sending":"")));
		s.add(predicate2XQuery(ce.predicate));
		
		String str = fr.lri.schora.util.List.toString(s, " and ");
		
		return str;
	}
	
	String format(String pattern, String val0, String val){
		if (val == null || val == "" || val.length() == 0)
			return "";
		return String.format(pattern, val0, val);
	}
	
	String predicate2XQuery(Condition predicate){
		List<Variable> vars = predicate.variables();
		
		for (Variable v : vars){
			if (variableLables.containsKey(v.getName())){
				v.setName(variableLables.get(v.getName()));
			}else{
				//throw new Exception("I don't not know variable " + v.getName());
			}
		}
		
		String str = predicate.toString().replaceAll("==", "=");
		str = str.replaceAll(" AND ", " and ");
		str = str.replaceAll(" OR ", " or ");
		str = str.replaceAll("true", "true()");
		str = str.replaceAll("false", "false()");
		return str;
	}
	
	
	public String getXQueryOfFirstEvent()  throws Exception{
		CandidateEvent ce = context.get(0);
		String str = String.format("for %s in /message \n return (%s)",
				ce.name, event2XQuery(ce));
		return str;
	}
	
	
	

	/**
	 * Create an xquery string which will be verified on a window<br>
	 * Not need window statement <br>
	 * Root element : <code>&lt;win&gt;</code>
	 * @return
	 */
	
	/*
 for $e1 in /log/message[1] 
 where $e1/method != "ACK" 
 return 
    (some $q1 in /log//message[position() > 1] satisfies ((((((number($q1/statusCode) >= 200) and ($e1/from = $q1/from)) and ($e1/callId = $q1/callId)) and ($e1/cSeq/method = $q1/cSeq/method)) and ($e1/vias = $q1/vias)) and ((($e1/to/tag != "null") and ($e1/to = $q1/to)) or (($e1/to/tag = "null") and ($e1/to/address/URI = $q1/to/address/URI)))))
	 */
	public String toXQueryOnWindow()  throws Exception{
		if (context == null || consequence == null || context.size() == 0 || consequence.size() == 0)
			return "";
		
		String ns = "";
		Set<String> keys = namespaces.keySet();
		for (String k : keys)
			ns = String.format("%s declare namespace %s=\"%s\";\n", ns, k, namespaces.get(k));
		
		String preambleStr = "";
		int n = context.size();
		String spc = "";
		for (int i=0; i<n; i++){
			CandidateEvent ce = context.get(i);
			
			preambleStr = String.format(
					"%s" +
					"\n%s for %s in /log/message[%d] " +
					"\n%s where %s " +
					"\n%s return ",
					preambleStr,
					spc, ce.name, (i+1),
					spc, event2XQuery(ce),
					spc
					);
			spc += "   ";
		}
		
		
		int psize = context.size();
		List<String> lst = new ArrayList<String>();
		for (CandidateEvent ce : consequence){
			String var = ce.name;
			
			List<String> s = new ArrayList<String>();
			s.add(format("%s/@name eq \"%s\"", var, ce.event.operationName));
			s.add(format("%s/@source eq \"%s\"", var, ce.event.sender));
			s.add(format("%s/@destination eq \"%s\"", var, ce.event.receiver));
			s.add(format("%s/@direction eq \"%s\"", var, ((ce.event.direction == CommunicationType.RECEPTION)? "reception" : 
				(ce.event.direction == CommunicationType.SENDING)?"sending":"")));
			String header = fr.lri.schora.util.List.toString(s, " and ");
			
			//FIXME: I do a trick hear for only SIPP paper
			if (header.trim().length() == 0){
				s.clear();
				String ss[] = event2XQuery(ce).split("and");
				
				for (String str : ss)
					if (str.contains("statusCode") || str.contains("method")){
						str = str.trim();
						while (str.startsWith("("))
							str = str.substring(1);
						while (str.endsWith(")"))
							str = str.substring(0, str.length() - 1);
						s.add(str);
					}
				header = fr.lri.schora.util.List.toString(s, " and ");
			}
			String str = String.format(
					"\n%s if (some %s in /log//message[position() > %d] satisfies %s) then 1" +
					"\n%s else if (some %s in /log//message[position() > %d] satisfies %s) then 2" +
					"\n%s else 3",
					spc,
					ce.name, 
					psize,
					event2XQuery(ce),
					spc,
					ce.name,
					psize,
					header,
					spc);
			lst.add(str);
		}
		
		String consequenceStr = fr.lri.schora.util.List.toString(lst, " or ");
		
		String qStr = ns +  
				preambleStr +
				(isPositive? "" : "\n not (") +
				consequenceStr +
				(isPositive? "" : ")")  //+
				;
		return qStr;
	}
}
