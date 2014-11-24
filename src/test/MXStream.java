/**
 * Jun 27, 2012 12:35:09 PM
 * @author nhnghia
 */
package test;

import java.io.PrintStream;

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
import ch.ethz.mxquery.util.StringReader;
import ch.ethz.mxquery.xdmio.XDMInputFactory;
import ch.ethz.mxquery.xdmio.XDMSerializer;
import ch.ethz.mxquery.xdmio.XDMSerializerSettings;
import ch.ethz.mxquery.xdmio.XMLSource;
import fr.lri.proptester.local.xquery.Output;

public class MXStream {
	public static void main(String[] args) throws Exception {
		fr.lri.schora.util.Stat.start();
		
		Context ctx = new Context();
		CompilerOptions co = new CompilerOptions();
		co.setXquery11(true);
		co.setContinuousXQ(true);
		XQCompiler compiler = new CompilerImpl();

		String query = "declare variable $input external; " +
			"for tumbling window $w in $input//message " +
			"start at $s when true() end at $e when ($e - $s) ge 2 " +
			"return <result>{$w}</result>\n";
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
		String msg = "\n<message tstamp=\"%d\">" +
				"\n <statusCode>180</statusCode>" +
				"\n <source>10.42.43.11:5060</source>" +
				"\n <destination>10.42.43.1:5061</destination>" +
				"\n <direction>reception</direction>" +
				"\n <time>1318279004001</time>" +
				"\n <cSeq>" +
				"\n <method>INVITE</method>" +
				"\n <seq>1</seq>" +
				"\n </cSeq>" +
				"\n <callId>524-3450@10.42.43.1</callId>" +
				"\n <via>Via: SIP/2.0/UDP 10.42.43.1:5061;branch=z9hG4bK-3450-524-3" +
				"\n </via>" +
				"\n</message>";
		long i = 0;
		while(i<Integer.parseInt(args[0])) {
			//Thread.sleep(500);
			XMLSource xmlIt = XDMInputFactory.createXMLInput(ctx,
				new StringReader(String.format(msg, i++)), true, Context.NO_VALIDATION,
				QueryLocation.OUTSIDE_QUERY_LOC);
			input.pushIterator(xmlIt);
		}
		
		input.endStream();
		
		fr.lri.schora.util.Stat.end();
	}
}
