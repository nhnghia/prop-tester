package fr.lri.proptester.local.xqueryOnWindow;

import java.io.StringReader;

import ch.ethz.mxquery.contextConfig.CompilerOptions;
import ch.ethz.mxquery.contextConfig.Context;
import ch.ethz.mxquery.datamodel.QName;
import ch.ethz.mxquery.exceptions.MXQueryException;
import ch.ethz.mxquery.exceptions.QueryLocation;
import ch.ethz.mxquery.model.XDMIterator;
import ch.ethz.mxquery.query.PreparedStatement;
import ch.ethz.mxquery.query.XQCompiler;
import ch.ethz.mxquery.query.impl.CompilerImpl;
import ch.ethz.mxquery.xdmio.XDMInputFactory;
import ch.ethz.mxquery.xdmio.XDMSerializer;
import ch.ethz.mxquery.xdmio.XMLSource;

public class MXQuery extends Query {

	PreparedStatement statement;
	XDMIterator result;
	XDMSerializer ip;
	public MXQuery(String queryStr) {
		super(queryStr);
		Context ctx = new Context();
		// Create a compiler options oh
		CompilerOptions co = new CompilerOptions();
		co.setXquery11(true);
		// Enable schema support
		co.setSchemaAwareness(true);
		// create a XQuery compiler
		XQCompiler compiler = new CompilerImpl();
		try {
		    // out of the context and the query "text" create a prepared
		    // statement,
		    // considering the compiler options
		    statement = compiler.compile(ctx, queryStr, co, null, null);
		    // Get an iterator from the prepared statement
		    // Set up dynamic context values, e.g., current time
		    result = statement.evaluate();
		} catch (MXQueryException err) {
		    MXQueryException.printErrorPosition(queryStr, err.getLocation());
		}
		
	    // Create an XDM serializer, can take an XDMSerializerSettings
	    // object if needed
	    ip = new XDMSerializer();
	    // format
	}

	@Override
	public Object query(String xmlDocument) throws Exception {
		// Add the contents of the external variable
	    XMLSource xmlIt = XDMInputFactory.createXMLInput(result
		    .getContext(), new StringReader(xmlDocument), true,
		    Context.NO_VALIDATION, QueryLocation.OUTSIDE_QUERY_LOC);
	    
	    statement.addExternalResource(new QName("log"), xmlIt);
	    
	    // run expression, generate XDM instance and serialize into String
	    String strResult = ip.eventsToXML(result);
	    System.out.println(strResult);

		return strResult;
	}

}
