/**
 * Jan 10, 2013 11:49:47 AM
 * @author nhnghia
 */
package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.PullSource;
import net.sf.saxon.pull.StaxBridge;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DecimalValue;
import fr.lri.proptester.local.xquery.Output;

public class SaxonPull {

	/**
	 * @param args
	 * @throws XPathException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, XPathException {
		fr.lri.schora.util.Stat.start();
		
		String query =
				" for $m in /log//message return ($m/time > 0)" ;
		
		OutputStream output = new Output("a");
		File input = new File("/Users/nhnghia/these/SIPP/log/adhoc.server.500.xml");
        Configuration config = Configuration.newConfiguration();
        config.setLazyConstructionMode(true);
        Processor proc = new Processor(true);
		proc.setConfigurationProperty(FeatureKeys.XQUERY_VERSION , "3.0");
        config.setProcessor(proc);
        //config.setLineNumbering(true);

        PipelineConfiguration pipe = config.makePipelineConfiguration();
        
        // PullProvider p = o.getParser(input);
        StaxBridge parser = new StaxBridge();
        parser.setInputStream(input.toURI().toString(), new FileInputStream(input));
        parser.setPipelineConfiguration(config.makePipelineConfiguration());
        PullProvider p = parser;
        
        p.setPipelineConfiguration(pipe);
        
        //o.query(p, query, output);
        final StaticQueryContext sqc = config.newStaticQueryContext();
        sqc.setLanguageVersion(new DecimalValue(3.0));
        final XQueryExpression exp = sqc.compileQuery(query);
        final DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
        dynamicContext.setContextItem(config.buildDocument(new PullSource(p)));
        Properties props = new Properties();
        props.setProperty(OutputKeys.INDENT, "yes");
        exp.run(dynamicContext, new StreamResult(output), props);
        
        
        fr.lri.schora.util.Stat.end();
	}

	
}
