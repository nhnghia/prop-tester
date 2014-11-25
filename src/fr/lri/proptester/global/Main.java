/**
 * Jul 24, 2012 1:45:59 PM
 * @author nhnghia
 */
package fr.lri.proptester.global;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.ethz.mxquery.exceptions.MXQueryException;
import fr.lri.proptester.local.xquery.MXQuery;
import fr.lri.proptester.local.xquery.Output;
import fr.lri.proptester.local.xquery.XQuery;
import fr.lri.proptester.global.property.Property;
import fr.lri.schora.util.Broadcast;
public class Main {

	static void help(){
		System.out.println("java -jar " + fr.lri.schora.util.File.getJarFolder(Main.class) + " propertyFile inputStreamURL_1 ... inputStreamURL_n [-b:broadcastPort]");
		System.out.println("where: ");
		System.out.println("   * propertyFile     : path to property file");
		System.out.println("   * inputStreamURL_i : url of local verdict stream");
		System.out.println("   * -b:broadcastPort : port to broadcast verdicts");
		System.out.println("   * -v               : print verbose");
	}
	/**
	 * @param args
	 * Verif PO inputStreamURL outputStreamURL
	 */
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2){
			help();
			return;
		}
		
		//Parser arguments
		int broadcastPort = 0;
		boolean isVerbose = false;
		List<String> inputURL = new ArrayList<String>();
		
		if (args.length > 2){
			try{
			for (int i=1; i<args.length; i++)
				if (args[i].equals("-v"))
					isVerbose = true;
				else if (args[i].startsWith("-b:"))
					broadcastPort = Integer.parseInt(args[i].substring(3));
				else
					inputURL.add(args[i]);
			
			}catch(Exception ex){
				help();
				return;
			}
		}
		
		try{
			Broadcast broadcast = null;
			if (broadcastPort != 0){
				try{
					broadcast = new Broadcast();
					broadcast.setPort(broadcastPort);
				}catch (Exception ex){
					fr.lri.schora.util.Print.error("Cannot open broadcast at port[" + broadcastPort + "]: " + ex.getMessage());
				}
			}
			
			//Get Properties
			List<Property> propLst = Property.getProperties(args[0]);
			
			if (isVerbose){
				System.out.println("====[LIST OF PROPERTIES]============================");
				System.out.print(fr.lri.schora.util.List.toString(propLst, "\n"));
				System.out.println();
			}
			
			//Translate to XQuery
			int n = propLst.size();
			final List<XQuery> queryLst = new ArrayList<XQuery>(n);
			
			for (int i=0; i<n; i++){
				String str = propLst.get(i).toXQuery();
				
				if (isVerbose){
					System.out.println("\n====[XQuery " + (i+1) + "]======================================");
					System.out.println(str);
				}
				
				XQuery query;
				
				if (broadcast == null)
					query = new MXQuery(str, new Output(propLst.get(i).name));
				else
					query = new MXQuery(str, new Output(broadcast, propLst.get(i).name, false));
				
				 
				queryLst.add(query);
			}
			
			if (isVerbose){
				System.out.println("\n====[VERIFYING ON STREAM OF LOCAL VERDICTS .... ]===");
			}
			
			
			LocalResult lc = new LocalResult(inputURL);
			String msg;
			
			
			System.gc();
			
			fr.lri.schora.util.Stat.start();
			
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					// Print statistic
					for (XQuery q : queryLst)
						q.printStat();

					fr.lri.schora.util.Stat.end();
				}
			});
			
			
			int i=0; int d = 0;
			while (true){
				msg = lc.getResult();
				
				//if msg == null ==> pass "null" to exits queries
				for (XQuery q : queryLst){
					q.putData(msg);
				}
				
				fr.lri.schora.util.Print.println("-- " + msg);
				
				if (msg == null)
					break;
				i ++;
				if (i==1000){
					//System.gc();
					if (isVerbose)
						System.out.println("  " + ((++d)*1000) +"verdict.");
					i=0;
				}
			}
			
			
			//Wait all queries complete
//			Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//			for (Thread t : threadSet){
//				if (t.getName().equals("Query") && t.isAlive())
//					t.join();
//			}
//			
			
			
			//System.out.println(String.format("%d, %.2f", (t2-t1), mm));
//			try{
//				broadcast.close();
//			}catch(Exception ex){}
			
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
