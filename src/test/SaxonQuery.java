/**
 * Jan 8, 2013 3:34:20 PM
 * @author nhnghia
 */
package test;

import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XQueryCompiler;
import net.sf.saxon.s9api.XQueryExecutable;
import fr.lri.proptester.local.xquery.Output;


public class SaxonQuery {
	public static void main(String[] args) throws SaxonApiException{
		
		//verif in line
		String query =
				"for $m in saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.64000.xml')//message)" +
				" where $m/method = 'ACK'" +
				"\n return $m/time";
		//verif until end
		String query2 = " declare namespace u='fr.lri.proptester';" +
				" declare function u:stream() {" +
				" saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.500.xml')//message)" +
				" };" +
				" for $m in u:stream()" +
				"\n return <msg>{$m}</msg>";
		//verif until end
		String query3 = " for sliding window $m in saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.500.xml')//message)" +
				" start $e1 at $spos when ($spos = 2)" +
				" end   $e at $epos when $epos - $spos eq 3" +
				" return <window>{$m}</window>";
		
		//verif in line
		String query4 =
				" for sliding window $w in(" +
				"   for $m in saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.500.xml')//message)" +
				"   return $m)" +
				" start $e1 at $spos when $e1/method != 'ACK'" +
				" end       at $epos when $epos - $spos = 3" +
				" return <win>{$w}</win>";
		
		//error xquery compile
		String query5 =
				" declare namespace u='fr.lri.proptester';" +
				" declare function u:stream() {" +
				"   for $m in saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.500.xml')//message)" +
				"   return $m" +
				" };" +
				" for sliding window $w in" +
				"    u:stream()" +
				" start $e1 at $spos when $e1/method != 'ACK'" +
				" end       at $epos when $epos - $spos = 3" +
				" return <win>{$w}</win>";
		
		
		//verif in line
		String query6 =
				" for $w in(" +
				" for sliding window $win in(" +
				"   for $m in saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.500.xml')//message)" +
				"   return $m" +
				" )" +
				" start $e1 at $spos when $e1/method != 'ACK'" +
				" end       at $epos when $epos - $spos = 3" +
				" return <win>{$win}</win>" +
				" )" +
				" return (" +
				"   for $e in $w/message[1]" +
				"	return (" +
				"		<msg>{$e}</msg>" +
				"	)" +
				")";
		
		//verif until end
		String query7 =
				" for $w in(" +
				" for sliding window $win in(" +
				"   for $m in saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.500.xml')//message)" +
				"   return $m" +
				" )" +
				" start $e1 at $spos when $e1/method != 'ACK'" +
				" end       at $epos when $epos - $spos = 3" +
				" return <win>{$win}</win>" +
				" )" +
				" return (" +
				"   some $e in $w/message[1] satisfies $e/statusCode != 200" +
				")";
				
		//
		String query8 =
				" for $w in(" +
				" for sliding window $win in(" +
				"   for $m in saxon:stream(doc('file:///Users/nhnghia/these/SIPP/log/adhoc.64000.xml')//message)" +
				"   return $m" +
				" )" +
				" start $e1 at $spos when $e1/method != 'ACK'" +
				" end       at $epos when $epos - $spos = 3" +
				" return <win>{$win}</win>" +
				" )" +
				" return (" +
				"   for $m in $w/message[1] return ($m/time > 0)" +
				")";
		
		//query = query8;
		Processor proc = new Processor(true);
		proc.setConfigurationProperty(FeatureKeys.XQUERY_VERSION , "3.0");
        XQueryCompiler comp = proc.newXQueryCompiler();
        XQueryExecutable exp = comp.compile(query);
        
        
        //*
        Serializer out = proc.newSerializer(new Output("p"));
        out.setOutputProperty(Serializer.Property.METHOD, "xml");
        out.setOutputProperty(Serializer.Property.INDENT, "yes");
        out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
        
        fr.lri.schora.util.Stat.start();
        try{
        exp.load().run(out);
        }catch (Exception ex){
        	fr.lri.schora.util.Print.error(ex.getMessage());
        }
        fr.lri.schora.util.Stat.end();
        
        /*/
        XQueryEvaluator qe = exp.load();
        XdmValue result = qe.evaluate();
        for (XdmItem item: result)  {
            System.out.println("-- \n" + item.getStringValue());
        }
        /*/
    }
}
