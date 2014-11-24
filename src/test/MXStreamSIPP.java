/**
 * Jun 27, 2012 12:35:09 PM
 * @author nhnghia
 */
package test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;

import ch.ethz.mxquery.contextConfig.CompilerOptions;
import ch.ethz.mxquery.contextConfig.Context;
import ch.ethz.mxquery.datamodel.QName;
import ch.ethz.mxquery.datamodel.xdm.Token;
import ch.ethz.mxquery.datamodel.xdm.TokenInterface;
import ch.ethz.mxquery.exceptions.QueryLocation;
import ch.ethz.mxquery.model.XDMIterator;
import ch.ethz.mxquery.query.PreparedStatement;
import ch.ethz.mxquery.query.XQCompiler;
import ch.ethz.mxquery.query.impl.CompilerImpl;
import ch.ethz.mxquery.sms.StoreFactory;
import ch.ethz.mxquery.sms.MMimpl.StreamStoreInput;
import ch.ethz.mxquery.sms.interfaces.StreamStore;
import ch.ethz.mxquery.xdmio.XDMInputFactory;
import ch.ethz.mxquery.xdmio.XDMSerializer;
import ch.ethz.mxquery.xdmio.XDMSerializerSettings;
import ch.ethz.mxquery.xdmio.XMLSource;
import fr.lri.proptester.local.xquery.Output;

public class MXStreamSIPP {
	public static void main(String[] args) throws Exception {
		fr.lri.schora.util.Stat.start();
		
		Context ctx = new Context();
		CompilerOptions co = new CompilerOptions();
		co.setXquery11(true);
		co.setContinuousXQ(true);
		XQCompiler compiler = new CompilerImpl();

		String query = 
				" declare variable $stream external;" +
				" for $w in (" +
				"    for sliding window $win in $stream//message" +
				"      start $e1 at $spos when (($e1/method != \"ACK\") and ($e1/direction = \"sending\"))" +
				"      end   $e at $epos when $epos - $spos eq 10" +
				"    return <window>{$win}</window>" +
				" )" +
				" return for $e1 in $w/message[1] return" +
				" <vdict msg=\"{$e1/@tstamp}\"> {" +
				" (some $ee1 in $w//message[position() > 1] satisfies ((((((($ee1/direction = \"reception\") and ($e1/source = $ee1/destination)) and ($e1/destination = $ee1/source)) and ($e1/via = $ee1/via)) and ($e1/callId = $ee1/callId)) and ($e1/cSeq/seq = $ee1/cSeq/seq)) and ($e1/cSeq/method = $ee1/cSeq/method))) }</vdict>"
				;
		
		PreparedStatement statement = compiler.compile(ctx, query, co, null, null);
		final XDMIterator result = statement.evaluate();
		
		StreamStore store = ctx.getStores().createStreamStore(
				StoreFactory.SEQ_FIFO, "input-stream");
		XDMIterator wnd = store.getIterator(ctx);
		statement.addExternalResource(new QName("input"), wnd);
		StreamStoreInput input = new StreamStoreInput(store);
		
		// process window results in a separate thread..
		new Thread() {
			@Override
			public void run() {
				try {
					/*Token token = null;
					while((token = result.next()) != null) {
						// process token..
						System.out.println(token.getValueAsString());
					}
					*/
					XDMSerializerSettings setting = new XDMSerializerSettings();
					setting.setOmitXMLDeclaration(true);
					
					XDMSerializer ip = new XDMSerializer(setting);
					
					PrintStream pr = new PrintStream(new Output("abc"));
					// The serializer can also handle inifinite XDM sequences
					ip.eventsToXML(pr, result);
				} catch (Exception e) {}
			}
		}.start();
		
		// continuously add events...
		
		InputStream stream;
		URL u = new URL("file:///Users/nhnghia/these/SIPP/log/adhoc.server.64000.xml");
		stream = u.openStream();
		
		long i = 0;
		//Thread.sleep(500);
			XMLSource xmlIt = XDMInputFactory.createXMLInput(ctx,
					new InputStreamReader(stream), true, Context.NO_VALIDATION,
				QueryLocation.OUTSIDE_QUERY_LOC);
			TokenInterface tok;
			while ((tok = xmlIt.next()) != Token.END_SEQUENCE_TOKEN) {
				input.bufferNext(tok);
			}
		
		input.endStream();
		
		fr.lri.schora.util.Stat.end();
	}
}
