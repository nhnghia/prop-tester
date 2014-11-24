/**
 * Jan 8, 2013 2:07:15 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.xquery;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.PullSource;
import net.sf.saxon.pull.StaxBridge;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DecimalValue;
import ch.ethz.mxquery.exceptions.MXQueryException;


public class SaxonQuery extends XQuery{

	final DynamicQueryContext dynamicContext;
	Configuration config;
	final StreamResult result;
	OutputStreamWriter writer;
	
	StaxBridge parser;
	final XQueryExpression exp;
	
	public SaxonQuery(String xquery, Output output) throws Exception{
		super(output);
		
		xquery = " declare variable $stream external;  for $m in $stream//message return $m";
		
		result = new StreamResult(output);
		config = Configuration.newConfiguration();
		final StaticQueryContext sqc = config.newStaticQueryContext();
        sqc.setLanguageVersion(new DecimalValue(3.0));					//XQuery 3.0
        exp = sqc.compileQuery(xquery);
        
        PipedOutputStream pipeOutput = new PipedOutputStream();
        PipedInputStream pipeInput = new PipedInputStream();
        pipeOutput.connect(pipeInput);
        writer = new OutputStreamWriter(pipeOutput);
        
        PipelineConfiguration pipeConfig = config.makePipelineConfiguration();
        
        parser = new StaxBridge();
        parser.setPipelineConfiguration(pipeConfig);
        //parser.setInputStream("input-stream", pipeInput);
        parser.setInputStream("input-stream", new ByteArrayInputStream("<log><message name='a'/> <message name='b'/></log>".getBytes()));
        PullProvider pullProvider = parser;
        
        dynamicContext = new DynamicQueryContext(config);
        dynamicContext.setParameter("stream", config.buildDocument(new PullSource(pullProvider)));
        
        Thread queryThread = new Thread(){
        	@Override
			public void run(){
        		Properties props = new Properties();
        		props.setProperty(OutputKeys.METHOD, "xml");
        		props.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                props.setProperty(OutputKeys.INDENT, "yes");
        		try {
					//exp.pull(dynamicContext, result, props);
					exp.run(dynamicContext, result, props);
					fr.lri.schora.util.Print.println("END OF QUERY");
					
					System.exit(MAX_PRIORITY);
				} catch (XPathException e) {
					e.printStackTrace();
				}
        	}
        };
        queryThread.setName("Query");
        queryThread.start();
	}
	
	/**
	 * Put data into stream of query
	 * Put NULL to end stream
	 * @param msg
	 * @throws XPathException 
	 * @throws IOException 
	 * @throws MXQueryException 
	 */
	public void putData(String msg) throws XPathException, IOException{
		
		if (msg == null){
			return;
		}

		/*
		int d = msg.indexOf("<message");
		int dd = msg.indexOf("</message>", d);
		if (d == -1 || dd == -1){
			return;
		}
		*/
		fr.lri.schora.util.Print.error("--");
		//writer.write(msg);
		//parser.setInputStream("input-stream", new ByteArrayInputStream(msg.getBytes()));
		//PullProvider pullProvider = parser;
		//dynamicContext.setParameter("stream", config.buildDocument(new PullSource(pullProvider)));
		//exp.pull(dynamicContext, result, new Properties());
		
		//msg = msg.substring(d, dd);
		//dynamicContext.setContextItem(config.buildDocument(new StreamSource(new StringReader(msg))));
		dynamicContext.setParameter("stream", config.buildDocument(new StreamSource(new StringReader(msg))));
	}
}