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
import fr.lri.proptester.local.xquery.Output;
import fr.lri.proptester.local.xquery.SaxonQuery;
import fr.lri.schora.util.Broadcast;
import fr.lri.schora.util.Debug;

/**
 * Jun 20, 2012 3:35:50 PM
 * @author nhnghia
 */

public class MainSaxon {

	static void help(){
		System.out.println("java -jar local-tester.jar propertyFile inputStreamURLMsg [-b:broadcastPort] [-v]");
		System.out.println("where: ");
		System.out.println("   * propertyFile     : path to property file");
		System.out.println("   * inputStreamURLMsg: url of xml messages");
		System.out.println("   * -b:broadcastPort : port to broadcast verdicts");
		System.out.println("   * -v               : print verbose");
	}
	/**
	 * @param args
	 * Verif PO inputStreamURL outputStreamURL
	 */
	public static void main(String[] args) {
		
		if (Debug.isDebug()){
			args = new String[3];
			args[0] = "/Users/nhnghia/these/code/java/fr.lri.proptester.local/evaluation/sipp/sip1.prop.xml";
			args[1] = "file:///Users/nhnghia/these/SIPP/log/adhoc.500.xml";
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
			List<SaxonQuery> queryLst = new ArrayList<SaxonQuery>(n);
			
			for (int i=0; i<n; i++){
				String str = propLst.get(i).toXQuery();
				
				str = "for $w in //message return $w/time";
				
				if (isVerbose){
					System.out.println("\n====[XQuery " + (i+1) + "]======================================");
					System.out.println(str);
				}
				
				SaxonQuery query;
				
				if (broadcast == null)
					query = new SaxonQuery(str, new Output(propLst.get(i).name));
				else
					query = new SaxonQuery(str, new Output(broadcast, propLst.get(i).name, propLst.get(i).isUsedByGlobalProperty));
				
				 
				queryLst.add(query);
			}
			
			if (isVerbose){
				System.out.println("\n====[VERIFYING ON STREAM LOG .... ]=================");
			}
			
			//Test input stream
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
			
			
			fr.lri.schora.util.Stat.start();
			
			int i=0; int d = 0;
			while (true){
				msg = filter.getMessage();
				
				//if msg == null ==> pass "null" to exits queries
				for (SaxonQuery q : queryLst){
					q.putData(msg);
				}
				
				if (msg == null)
					break;
				i ++;
				if (i==1000){
					//System.gc();
					if (isVerbose)
						System.out.println("  " + ((++d)*1000) +"msg.");
					i=0;
				}
			}
			
			
			//Wait all queries complete
			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
			for (Thread t : threadSet){
				if (t.getName().equals("Query") && t.isAlive())
					t.join();
			}
			
			fr.lri.schora.util.Stat.end();
			
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
