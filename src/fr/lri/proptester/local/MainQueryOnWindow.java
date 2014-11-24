package fr.lri.proptester.local;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.ethz.mxquery.exceptions.MXQueryException;
import fr.lri.proptester.local.property.Property;
import fr.lri.proptester.local.soap.MessageFilter;
import fr.lri.proptester.local.xqueryOnWindow.XQueryOnWindow;
import fr.lri.schora.util.Broadcast;
import fr.lri.schora.util.Debug;

/**
 * Jun 20, 2012 3:35:50 PM
 * @author nhnghia
 */

public class MainQueryOnWindow {

	static void help(){
		System.out.println("java -jar "+fr.lri.schora.util.File.getJarFolder(MainQueryOnWindow.class)+" propertyFile inputStreamURLMsg [-b:broadcastPort] [-v]");
		System.out.println("where: ");
		System.out.println("   * propertyFile     : path to property file");
		System.out.println("   * inputStreamURLMsg: url of xml messages");
		System.out.println("   * -b:broadcastPort : port to broadcast verdicts");
		System.out.println("   * -v               : print verbose");
	}
	/**
	 * Verif PO inputStreamURL outputStreamURL
	 * @param args
	 */
	public static void main(String[] args) {
		//begin statistic
		fr.lri.schora.util.Stat.start();
		
		if (Debug.isDebug())
		{
			args = new String[4];
			args[0] = "/Users/nhnghia/these/workshop/prop-tester/tester/quotation.prop.xml";
			//args[1] = "file:///Users/nhnghia/these/SIPP/log/adhoc.server.1000.xml";
			args[1] = "http://localhost:2021";
			args[2] = "-v"; //"-b"
			args[3] = "-b 3030";
			//args[4] = "-v";
		}
		
		if (args.length < 2 || args.length > 4){
			help();
			return;
		}
		
		//Parser arguments
		String propertyFile = args[0];
		String inputStreamURL = args[1];

		int broadcastPort = 0;
		boolean isVerbose = false;
		
		if (args.length > 2){
			try{
			for (int i=2; i<args.length; i++)
				if (args[i].equals("-v"))
					isVerbose = true;
				else if (args[i].startsWith("-b:"))
					broadcastPort = Integer.parseInt(args[i].substring(3));
			}catch(Exception ex){
				help();
				return;
			}
		}
		
		
		
		try{
			Broadcast broadcast = null;
			if (broadcastPort != 0){
				broadcast = new Broadcast();
				broadcast.setPort(broadcastPort);
			}
			//Get properties
			List<Property> propLst = Property.getProperties(propertyFile);
			
			if(isVerbose){
				System.out.println("====[LIST OF PROPERTIES]============================");
				System.out.print(fr.lri.schora.util.List.toString(propLst, "\n"));
				System.out.println();
			}
			
			int n = propLst.size();
			List<XQueryOnWindow> queryLst = new ArrayList<XQueryOnWindow>(n);
			
			for (int i=0; i<n; i++){
				Property p = propLst.get(i);
				String str = p.toXQueryOnWindow();
				
				if (isVerbose){
					System.out.println("\n====[XQuery " + (i+1) + "]======================================");
					System.out.println(str);
				}
				
				XQueryOnWindow query;
				/*
				str = "declare variable $stream external; " +
						"for tumbling window $w in $stream//message " +
						"start at $s when true() end at $e when ($e - $s) ge 2 " +
						"return <result>{$w}</result>\n";
				*/
				try{
					if (broadcast == null)
						query = new XQueryOnWindow(p, System.out);
					else
						query = new XQueryOnWindow(p, broadcast);
					
					 
					queryLst.add(query);
				}catch (Exception ex){
					fr.lri.schora.util.Print.error(ex.getMessage());
				}
			}
			
			if (queryLst.size() == 0){
				fr.lri.schora.util.Print.error("No xquery is found. We quit now!");
				return;
			}
			
			fr.lri.schora.util.Print.println("Waiting for the input stream at [" + inputStreamURL + "]");
			
			InputStream stream;
			while (true){
				try{
					URL u = new URL(inputStreamURL);
					stream = u.openStream();
					break;
				}catch (IOException ex){
					//tools.Debug.print(ex);
					ex.printStackTrace();
					fr.lri.schora.util.Print.error("Cannot open stream at [" + inputStreamURL + "], I try again in 2 seconds");
					Thread.sleep(2000);
				}
			}
			
			if (isVerbose){
				System.out.println("\n====[VERIFYING ON STREAM LOG .... ]=================");
			}			
			
			MessageFilter filter = new MessageFilter(stream);
			String msg;
			
			
			System.gc();
			
			
			int i=0; int d = 0;
			while (true){
				msg = filter.getMessage();
				if (isVerbose){
					System.out.println(" ==> msg");
					System.out.flush();
				}
				//if msg == null ==> pass "null" to exits queries
				for (XQueryOnWindow q : queryLst){
					q.putData(msg);
				}
				
				if (msg == null)
					break;
				
				i ++;
				if (i==5000){
					System.gc();
					if (isVerbose){
						System.out.println("  " + ((++d)*5000) +"msg.");
						fr.lri.schora.util.Stat.end();
					}
					i=0;
				}
			}
			
			
			//Wait all queries complete
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			for (Thread t : threadSet){
				if (t.getName().equals("Query") && t.isAlive())
					t.join();
			}
			
			//Print statistic
			for (XQueryOnWindow q : queryLst)
				q.printStatAndClose();
			
			try{
				broadcast.close();
			}catch(Exception ex){}
			
		}catch (MXQueryException mx){
			System.out.println("XQUERY ERROR: " + mx.getMessage());
			fr.lri.schora.util.Debug.print(mx);
		}catch (MalformedURLException ex){
			System.out.println("ERROR: URL address is not correct");
		}catch (Exception ex){
			System.out.println("ERROR: " + ex.getMessage());
			fr.lri.schora.util.Debug.print(ex);
		}
		fr.lri.schora.util.Stat.end();
	}
	
}
