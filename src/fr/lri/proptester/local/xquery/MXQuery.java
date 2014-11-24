package fr.lri.proptester.local.xquery;

import java.io.PrintStream;
import java.lang.reflect.Field;

import ch.ethz.mxquery.bindings.WindowBuffer;
import ch.ethz.mxquery.bindings.WindowSequenceIterator;
import ch.ethz.mxquery.contextConfig.CompilerOptions;
import ch.ethz.mxquery.contextConfig.Context;
import ch.ethz.mxquery.datamodel.QName;
import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.exceptions.QueryLocation;
import ch.ethz.mxquery.iterators.ForIterator;
import ch.ethz.mxquery.model.CurrentBasedIterator;
import ch.ethz.mxquery.model.XDMIterator;
import ch.ethz.mxquery.query.PreparedStatement;
import ch.ethz.mxquery.query.XQCompiler;
import ch.ethz.mxquery.query.impl.CompilerImpl;
import ch.ethz.mxquery.sms.StoreFactory;
import ch.ethz.mxquery.sms.MMimpl.StreamStoreInput;
import ch.ethz.mxquery.sms.MMimpl.TokenBufferStore;
import ch.ethz.mxquery.sms.interfaces.StreamStore;
import ch.ethz.mxquery.util.StringReader;
import ch.ethz.mxquery.xdmio.XDMInputFactory;
import ch.ethz.mxquery.xdmio.XDMSerializer;
import ch.ethz.mxquery.xdmio.XDMSerializerSettings;
import ch.ethz.mxquery.xdmio.XMLSource;

public class MXQuery extends XQuery{
	Context ctx;
	StreamStoreInput input;
	/**
	 * Execution an XQuery on a stream of XML
	 * which will be fed by putData method
	 * @param query: xquery string
	 * @param output: a stream to print result
	 * @return
	 * @throws MXQueryException 
	 * @throws Exception
	 */
	Output output;
	final PrintStream print;
	final XDMIterator result;
	int msgCount;
	
	public MXQuery(String query, Output output) throws MXQueryException  {
		super(output);
		this.output = output;
		msgCount = 0;
		// Compile and run the query
		ctx = new Context();
		CompilerOptions co = new CompilerOptions();
		co.setXquery11(true);
		co.setContinuousXQ(true);
		co.setXquery30(true);
		
		XQCompiler compiler = new CompilerImpl();
		
		PreparedStatement statement = compiler.compile(ctx, query, co, null, null);
		result = statement.evaluate();

		StreamStore store = ctx.getStores().createStreamStore(
				StoreFactory.SEQ_FIFO, "input-stream");

		XDMIterator wnd = store.getIterator(ctx);
		statement.addExternalResource(new QName("stream"), wnd);

		input = new StreamStoreInput(store);
		
		print = new PrintStream(output);
		// process window results in a separate thread..
		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					XDMSerializerSettings setting = new XDMSerializerSettings();
					setting.setOmitXMLDeclaration(true);
					XDMSerializer ip = new XDMSerializer(setting);
					
					// The serializer can also handle inifinite XDM sequences
					ip.eventsToXML(print, result);
				} catch (Exception e) {
				}
			}
		};
		t.setName("Query");
		t.start();
	}
	
	
	void garb(){
		System.out.println("Garbage ...");

		XDMIterator[] iters = result.getAllSubIters();
		for(XDMIterator iter : iters) {
			if((iter instanceof ForIterator))
			{
				try{
					Field f1 = CurrentBasedIterator.class.getDeclaredField("current");
					f1.setAccessible(true);
					WindowSequenceIterator i = (WindowSequenceIterator)f1.get(iter);
					WindowBuffer b = i.getMat();
					
					Field f2 = TokenBufferStore.class.getDeclaredField("nodeI");
					f2.setAccessible(true);
					int nodeI = (Integer)f2.get(b.getBuffer());
					Field f3 = TokenBufferStore.class.getDeclaredField("nodesDel");
					f3.setAccessible(true);
					int nodesDel = (Integer)f3.get(b.getBuffer());
					
					fr.lri.schora.util.Print.println("\n node to del" +  (nodeI - nodesDel));
					if((nodeI - nodesDel) > 50) {
						fr.lri.schora.util.Print.println("Deleting " + (nodeI - nodesDel) + " items from buffer");
						((TokenBufferStore)b.getBuffer()).deleteItems(nodeI - 50);
					}
				}catch (Exception ex){
					fr.lri.schora.util.Debug.print(ex);
				}
			}
		}
		System.gc();
	}
	
	/**
	 * Put data into stream of query
	 * Put NULL to end stream
	 * @param msg
	 * @throws MXQueryException 
	 */
	public void putData(String msg) throws MXQueryException{

		if (msg == null){
			input.endStream();
			return;
		}

		/*
		int d = msg.indexOf("<message");
		int dd = msg.indexOf("</message>", d);
		if (d == -1 || dd == -1){
			return;
		}
		*/
		
		//msg = msg.substring(d, dd);
		
		XMLSource xmlIt = XDMInputFactory.createXMLInput(ctx,
				new StringReader(msg), true,
				Context.NO_VALIDATION, QueryLocation.OUTSIDE_QUERY_LOC);
		
		XDMIterator curItemIt = xmlIt;
		input.pushIterator(curItemIt);
		
		/*
		if (msgCount++ > 2000){
			msgCount = 0;
			garb();
		}
		*/
	}
}
