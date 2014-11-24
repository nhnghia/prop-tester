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
import fr.lri.proptester.local.xquery.MXQuery;
import fr.lri.proptester.local.xquery.Output;
import fr.lri.proptester.local.xquery.XQuery;
import fr.lri.schora.util.Broadcast;
import fr.lri.schora.util.Debug;

/**
 * Jun 20, 2012 3:35:50 PM
 * @author nhnghia
 */

public class Main {

	static void help(){
		System.out.println("java -jar "+fr.lri.schora.util.File.getJarFolder(Main.class)+" propertyFile inputStreamURLMsg [-b:broadcastPort] [-v]");
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
		
		if (Debug.isDebug())
		{
			args = new String[3];
			args[0] = "/Users/nhnghia/these/code/java/fr.lri.proptester.local/evaluation/sipp/sip1.prop.xml";
			args[1] = "file:///Users/nhnghia/these/SIPP/log/adhoc.server.4000.xml";
			args[2] = "-v"; //"-b"
			//args[3] = "8082";
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
			List<XQuery> queryLst = new ArrayList<XQuery>(n);
			
			for (int i=0; i<n; i++){
				Property p = propLst.get(i);
				String str = p.toXQuery();
				
				if (isVerbose){
					System.out.println("\n====[XQuery " + (i+1) + "]======================================");
					System.out.println(str);
				}
				
				XQuery query;
				/*
				str = "declare variable $stream external; " +
						"for tumbling window $w in $stream//message " +
						"start at $s when true() end at $e when ($e - $s) ge 2 " +
						"return <result>{$w}</result>\n";
				*/
				try{
					if (broadcast == null)
						query = new MXQuery(str, new Output(propLst.get(i).name));
					else
						query = new MXQuery(str, new Output(broadcast, propLst.get(i).name, propLst.get(i).isUsedByGlobalProperty));
					
					 
					queryLst.add(query);
				}catch (Exception ex){
					fr.lri.schora.util.Print.error(ex.getMessage());
				}
			}
			
			if (queryLst.size() == 0){
				fr.lri.schora.util.Print.error("No xquery is found. We quit now!");
				return;
			}
			
			if (isVerbose){
				System.out.println("\n====[VERIFYING ON STREAM LOG .... ]=================");
			}
			
			InputStream stream;
			while (true){
				try{
					URL u = new URL(inputStreamURL);
					stream = u.openStream();
					break;
				}catch (IOException ex){
					fr.lri.schora.util.Debug.print(ex);
					fr.lri.schora.util.Print.error("Cannot open stream at [" + inputStreamURL + "],we try again in 2 seconds");
					Thread.sleep(2000);
				}
			}
			
			MessageFilter filter = new MessageFilter(stream);
			String msg;
			
			
			System.gc();
			
			
			long t1 = System.currentTimeMillis();
			int i=0; int d = 0;
			while (true){
				msg = filter.getMessage();
				
				//if msg == null ==> pass "null" to exits queries
				for (XQuery q : queryLst){
					q.putData(msg);
				}
				
				if (msg == null)
					break;
				/*
				i ++;
				if (i==5000){
					//System.gc();
					if (isVerbose){
						System.out.println("  " + ((++d)*5000) +"msg.");
						tools.Stat.end();
					}
					i=0;
				}
				*/
				
			}
			
			
			//Wait all queries complete
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			for (Thread t : threadSet){
				if (t.getName().equals("Query") && t.isAlive())
					t.join();
			}
			
			//Print statistic
			for (XQuery q : queryLst)
				q.printStat();
			
			long t2 = System.currentTimeMillis();
			long m = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			float mm = (float) (m / (1024.0 * 1024));
			
			System.out.println(String.format("\n\nTime usage: %.3f (seconds), Memory usage: %.2f (Mega Bytes)", (t2-t1)/1000.0, mm));
			//System.out.println(String.format("%d, %.2f", (t2-t1), mm));
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
	}
	
}
