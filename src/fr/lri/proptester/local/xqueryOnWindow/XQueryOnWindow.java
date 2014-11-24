package fr.lri.proptester.local.xqueryOnWindow;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;


import fr.lri.proptester.local.property.Property;
import fr.lri.schora.util.Broadcast;

public class XQueryOnWindow {
	int windowSize;
	int noMsgAdded; // number of <message> elements which are added in window
	String window; // window to verify. It's a list of windowSize <message>
	// elements

	// String queryStr; //XQuery string to verify on first element of window
	// String queryStr; //XQuery string to verify on window

	Query query0; // verify if first CandidateEvent is satisfied
	Query query1; // get verdict
	Query query2; // get messageID
	String propName;
	public int nPass, nFail, nIncl;

	OutputStream output;
	Broadcast broadcast;
	/**
	 * @param property
	 *            property to verify
	 * @param output
	 * @throws Exception
	 */
	public XQueryOnWindow(Property p, OutputStream output) throws Exception {
		this.output = output;
		broadcast = null;
		propName = p.name;
		nPass = nFail = nIncl = 0;
		String queryStr = p.toXQueryOnWindow();
		windowSize = p.getLength();
		noMsgAdded = 0;
		window = "";

		query0 = new Saxon(p.getXQueryOfFirstEvent());
		query1 = new Saxon(queryStr);
		query2 = new Saxon("data(message/@tstamp)");
		
		// query = new MXQuery(queryStr);
		// query2 = new
		// MXQuery("for $m in /log//message[1] return data($m/@tstamp)");
	}

	public XQueryOnWindow(Property p, Broadcast b) throws Exception {
		broadcast = b;
		propName = p.name;
		nPass = nFail = 0;
		String queryStr = p.toXQueryOnWindow();
		windowSize = p.getLength();
		noMsgAdded = 0;
		window = "";

		query0 = new Saxon(p.getXQueryOfFirstEvent());
		query1 = new Saxon(queryStr);
		query2 = new Saxon("data(message/@tstamp)");
		
		// query = new MXQuery(queryStr);
		// query2 = new
		// MXQuery("for $m in /log//message[1] return data($m/@tstamp)");
	}
	
	/**
	 * Put data into stream of query Put NULL to end stream
	 * 
	 * @param msg
	 * @throws MXQueryException
	 */
	public void putData(String msg) throws Exception {
		if (msg != null) {
			// add message to the end of window
			window += msg;
			if (noMsgAdded >= windowSize - 1) {
				int d = window.indexOf("</message>");
				String firstMsg = window.substring(0, d + 10);
				
				queryOnWindow(firstMsg, "<log>" + window + "</log>");
				
				if (d >= 0)
					window = window.substring(d  + 10);
			} else {
				noMsgAdded++;
			}
		} else {
			int d = window.indexOf("</message>");
			
			while (d > -1) {
				String firstMsg = window.substring(0, d + 10);
				
				queryOnWindow(firstMsg, "<log>" + window + "</log>");
				
				window = window.substring(d + 10);
				d = window.indexOf("</message>");
			}
		}

	}

	void queryOnWindow(String firstMessage, String xmlDocument) throws Exception {
		//check if first message satisfies first condition of the property
		Object obj = query0.query(firstMessage);
		if (obj == null)
			return;
		if ((Boolean) obj == false)
			return;
		
		// query
		// Limit number of threads
		while (Thread.getAllStackTraces().size() > 120) {
			Thread.sleep(50);
		}
		
		ThreadQuery t = new ThreadQuery(xmlDocument, this);
		t.firstMessage = firstMessage;
		t.setName("Query");
		//t.start();
		t.run();
	}

	synchronized void saveVerdict(Object vdict, String msgId) {
		String str = "";
		//System.out.println(vdict);
		if (vdict != null){
			try{
				int v = Integer.parseInt(vdict.toString());
				if (v==1) {
					str = "pass";
					nPass++;
				} else if (v==2){
					str = "fail";
					nFail++;
				} else if (v==3){
					str = "inconclusive";
					nIncl ++;
				}
			}catch(Exception ex){
				
			}
		}
		if (str == "")
			return;
		str = String.format(
				"\n<message prop=\"%s\" tstamp=\"%d\" msg=\"%s\">%s</message>",
				propName, System.nanoTime(), msgId, str);

		try {
			if (broadcast != null)
				broadcast.broadcast(str);
			if (output != null)
				output.write(str.getBytes());
			else
				System.out.println(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printStatAndClose() {
		try {
			// wait all threads
			// Wait all queries complete
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			for (Thread t : threadSet) {
				if (t.getName().equals("Query") && t.isAlive())
					try {
						t.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}

		
				String str = String.format(
						"\n\n<stat pass=\"%d\" fail=\"%d\" inconclusive=\"%d\"/>", nPass, nFail, nIncl);
				output.write(str.getBytes());
				System.out.println("[" + propName + "] Pass: " + nPass + ", Fail:  " + nFail + ", Inconclusive: " + nIncl);
			//output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ThreadQuery extends Thread {
		String xmlDocument;
		String firstMessage;
		XQueryOnWindow wQuery;

		public ThreadQuery(String xmlDocument, XQueryOnWindow wQuery) {
			this.xmlDocument = xmlDocument;
			this.wQuery = wQuery;
		}

		public void run() {
			try {
				Object vdict = query1.query(xmlDocument);
				String msgId = (String) query2.query(firstMessage);
				wQuery.saveVerdict(vdict, msgId);
			} catch (Exception ex) {
				fr.lri.schora.util.Print.error("Error of query: " + ex.getMessage());
				ex.printStackTrace();
			}
		}
	}
}
