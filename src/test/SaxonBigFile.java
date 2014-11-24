/**
 * Jan 10, 2013 4:14:09 PM
 * @author nhnghia
 */
package test;

	 
	import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;

import org.xml.sax.InputSource;
	 
	/**
	 * X Query Runner provides an API to compile and run x-queries against an
	 * XML document.
	 *
	 * @author Ants
	 */
	public class SaxonBigFile {
	 
	 
	  /**
	   * Run a query against the given xml document.
	   * @param namespaces defines the namespaces that are expected in the document
	   * that is being queried
	   * @param xmlDocument the document to query
	   * @param queryString the x-query
	   * @return the result of the x-query
	   * @throws net.sf.saxon.trans.XPathException on any XPath errors in the query
	   * compilation or execution
	   * @throws java.io.IOException on any errors processing the input document
	   */
	  public String queryForXmlDocument(
	      InputSource xmlDocument, String queryString)
	      throws XPathException, IOException {
		  fr.lri.schora.util.Debug.println( "entering XQueryRunner.queryForXmlDocument() query = [" +
	        queryString + "]");
	    StringWriter output = new StringWriter();
	 
	    XQueryExpression expression = null;
	    Configuration configuration = new Configuration();
	    StaticQueryContext SQC = new StaticQueryContext( configuration);
	    DynamicQueryContext DQC = new DynamicQueryContext( configuration);
	 
	    try {
	      try {
	        // Compile the query.
	        expression = SQC.compileQuery( queryString);
	        SQC = expression.getStaticContext().getUserQueryContext();
	 
	        SAXSource documentSource = new SAXSource( xmlDocument);
	        DocumentInfo docInfo = SQC.buildDocument( documentSource);
	        DQC.setContextItem( docInfo);
	        // Choose to output XML from the XQuery results (instead of plain text).
	        Properties props = new Properties();
	        props.setProperty( OutputKeys.METHOD, "xml");
	        props.setProperty( OutputKeys.INDENT, "yes");
	        // Run the query.
	        expression.run( DQC, new StreamResult( output), props);
	        output.flush();
	        String outputString = output.getBuffer().toString();
	        //outputString = outputString.substring( outputString.indexOf( ">") + 1);
	        fr.lri.schora.util.Debug.println( "XQueryRunner.queryForXmlDocument() result = " +
	            outputString);
	        return outputString;
	      }
	      finally {
	        output.close();
	      }
	    }
	    catch( XPathException e) {
	    	fr.lri.schora.util.Debug.println( "XPath error in query: " + queryString + " - " +
	          e.getMessage());
	      throw e;
	    }
	    catch ( IOException e) {
	    	fr.lri.schora.util.Debug.println( "Error reading XML document from input source " +
	          e.getMessage());
	      throw e;
	    }
	  }
	  
	  
	  public void use() throws XPathException, IOException{
		  InputSource inputSource = new InputSource( new FileReader("/Users/nhnghia/these/SIPP/log/adhoc.64000.xml"));
		  String query = "for $e in //message return $e/time";
		  // Define the namespaces.
		  String result = new SaxonBigFile().queryForXmlDocument(inputSource,
		      query);
	  }
	  
	  public static void main(String[] args) throws XPathException, IOException{
		  SaxonBigFile s = new SaxonBigFile();
		  s.use();
	  }
	}