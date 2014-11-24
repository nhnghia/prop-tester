/**
 * Jan 10, 2013 3:42:21 PM
 * @author nhnghia
 */
package test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.namespace.QName;
import javax.xml.xquery.XQConnection;
import javax.xml.xquery.XQException;
import javax.xml.xquery.XQItem;
import javax.xml.xquery.XQPreparedExpression;
import javax.xml.xquery.XQResultSequence;

import net.sf.saxon.xqj.SaxonXQDataSource;
public class SaxonStream {

	/**
	 * @param args
	 * @throws XQException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, XQException {
		fr.lri.schora.util.Stat.start();
		
		SaxonStream s = new SaxonStream();
		s.getCount("INVITE");
		
		fr.lri.schora.util.Stat.end();
	}

	private XQItem referenceDataItem;
	private XQPreparedExpression xPrepExec;
	private XQConnection conn;

	public SaxonStream() throws FileNotFoundException, XQException{
		String query = 
				" declare variable $refDocument external;" +
				" for $m in $refDocument//message" +
				" return $m";
		//	set connection string and xquery file
		this.conn = new SaxonXQDataSource().getConnection();
	
		//Set the prepared expression 
		InputStream is  = new FileInputStream("/Users/nhnghia/these/SIPP/log/adhoc.64000.xml");
		this.referenceDataItem = conn.createItemFromDocument(is, null, null);
		this.xPrepExec = conn.prepareExpression(query);
		xPrepExec.bindItem(new QName("refDocument"), this.referenceDataItem);   
	}
	
	//the code below is in a seperate method and called multiple times
	public void getCount(String searchVal) throws XQException{

	    XQResultSequence result = xPrepExec.executeQuery();
        while (result.next()) {
            System.out.println("count: " + result.getItemAsString(null));
        }
	}
}
