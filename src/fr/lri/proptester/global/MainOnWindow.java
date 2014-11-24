/**
 * Jul 24, 2012 1:45:59 PM
 * @author nhnghia
 */
package fr.lri.proptester.global;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import fr.lri.proptester.local.xqueryOnWindow.*;
import fr.lri.proptester.global.property.Property;
import fr.lri.schora.util.Broadcast;

/**
 * Using {@link } rather than {@link fr.lri.proptester.local.xquery.MXQuery}
 * @author nhnghia
 *
 */
public class MainOnWindow {

	static void help(){
		System.out.println("java -jar " + fr.lri.schora.util.File.getJarFolder(MainOnWindow.class) + " propertyFile inputStreamURL_1 ... inputStreamURL_n [-b:broadcastPort] [-v]");
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
	public static void main(String[] args) {
		fr.lri.schora.util.Stat.start();
		
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
			List<VerifyProperty> queryLst = new ArrayList<VerifyProperty>(n);
			
			for (int i=0; i<n; i++){
				VerifyProperty query = new MainOnWindow().new VerifyProperty(propLst.get(i));
				queryLst.add(query);
				
				System.out.println("====[XQUERY OF PROPERTY " + i + "]============================");
				System.out.println(propLst.get(i).toXQueryOnWindow());
			}
			
			if (isVerbose){
				System.out.println("\n====[VERIFYING ON STREAM OF LOCAL VERDICTS .... ]===");
			}
			
			
			LocalResult lc = new LocalResult(inputURL);
			String msg;
			
			
			int d=0;
			boolean []passvdict = new boolean[n];
			boolean []failvdict = new boolean[n];
			String []tstamp = new String[n];
			
			for (int i=0; i<n; i++){
				passvdict[i] = false;
				failvdict[i] = false;
			}
			
			int nvdict = 0;
			while_label: while (true){
				
				msg = lc.getResult();
				//System.out.println(msg);
				//*
				for (int i=0; i<n; i++){
					if (passvdict[i] || failvdict[i])
						continue;
					//verify property i
					Object obj = queryLst.get(i).pushMessage(msg);
					
					//OK, I know property i : pass or fail
					if (obj != null){
						if ((Boolean) obj == true){
							passvdict[i] = true;
						}
						else{
							failvdict[i] = true;
						}
						
						tstamp[i] = "" + System.currentTimeMillis();
						nvdict ++;
						//OK, I got "n" verdicts, 
						//so I can stop here (not need waiting end of log)
						if (nvdict == n)
							break while_label;
						//System.out.println("ok");
					}
				}
				//*/
				if (msg == null)
					break;
				
				d ++;
			}
			
			System.out.println("Total: " + d + " messages");
			
			//update vdict
			String[] vdict = new String[n];
			for (int i=0; i<n; i++){
				if (passvdict[i]){
					vdict[i] = "pass";
				}
				else if (failvdict[i])
					vdict[i] = "fail";
				else{
					vdict[i] = "inconclusive";
					tstamp[i] = "" + System.currentTimeMillis();
				}
			}
			
			//print vdict
			for (int i=0; i<n; i++){
				String str = String.format("<message prop=\"%s\" tstamp=\"%s\">%s</message>",
						propLst.get(i).name,
						tstamp[i],
						vdict[i]
						);
				
				if (broadcast != null)
					broadcast.broadcast(str);
				else
					System.out.println(str);
			}
			
			
			fr.lri.schora.util.Stat.end();
			
		}catch (MalformedURLException ex){
			System.out.println("ERROR: URL address is not correct");
		}catch (Exception ex){
			System.out.println("ERROR: " + ex.getMessage());
			fr.lri.schora.util.Debug.print(ex);
		}
	}
	
	
	class VerifyProperty{
		boolean[] bcontext;
		int bcontextSize;
		int bconsequenceSize;
		
		int contextSize;
		Query[] contextQueries;
		
		int consequenceSize;
		Query[] consequenceQueries;
		
		boolean isPositive;
		
		public VerifyProperty(Property property) throws Exception{
			isPositive = property.isPositive;
			
			contextSize = property.context.size();
			bcontext = new boolean[contextSize];
			for (int i=0; i<contextSize; i++)
				bcontext[i] = false;
			
			bcontextSize = 0;
			bconsequenceSize = 0;
			
			contextQueries = new Query[contextSize];
			for (int i=0; i<contextSize; i++){
				String sql = String.format("(some $e in /message satisfies ($e/@prop=\"%s\" and data($e) = \"pass\") )", 
						property.context.get(i));
				contextQueries[i] = new Saxon(sql);
			}
			
			consequenceSize = property.consequence.size();
			consequenceQueries = new Query[consequenceSize];
			
			for (int i=0; i<consequenceSize; i++){
				String sql = String.format("(some $e in /message satisfies ($e/@prop=\"%s\" and data($e) = \"pass\") )", 
						property.consequence.get(i));
				consequenceQueries[i] = new Saxon(sql);
			}
		}
		
		
		public Boolean pushMessage(String msg) throws Exception{
			if (bcontextSize == contextSize){
				if (bconsequenceSize > 0)
					return isPositive;
				//end of log, but does not exist any "pass" in the consequence
				if (msg == null){
					return !isPositive;
				}
			}
			
			if (msg == null)
				return null;
			
			if (bcontextSize < contextSize)
				for (int i=0; i<contextSize; i++){
					if (bcontext[i])
						continue;
					Object obj = contextQueries[i].query(msg);
					if (obj != null)
						if ((Boolean)obj == true){
							bcontext[i] = true;
							bcontextSize ++;
							//System.out.println("OK" + i);
						}
				}
			
			if (bconsequenceSize == 0){
				for (int i=0; i<consequenceSize; i++){
					Object obj = consequenceQueries[i].query(msg);
					if (obj != null)
						if ((Boolean)obj == true){
							bconsequenceSize = 1;
							break;
						}
				}
			}
			
			return null;
		}
	}
}
