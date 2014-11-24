package fr.lri.proptester.local.xqueryOnWindow;

import java.io.StringReader;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DecimalValue;

public class Saxon extends Query {

	XQueryExpression exp;
	Configuration config;
	
	public Saxon(String xquery) throws Exception{
		super(xquery);
		
		config = Configuration.newConfiguration();
		StaticQueryContext sqc = config.newStaticQueryContext();
        sqc.setLanguageVersion(new DecimalValue(3.0));					//XQuery 3.0
        
        exp = sqc.compileQuery(xquery);
	}
	
	/**
	 * Exec Saxon query on xmlDocument
	 */
	public Object query(String xmlDocument) throws XPathException {
		DynamicQueryContext dynamicContext = new DynamicQueryContext(config);
		dynamicContext.setContextItem(config.buildDocument(new StreamSource(new StringReader(xmlDocument))));
		Object result = exp.evaluateSingle(dynamicContext);
		return result;
	}

}
