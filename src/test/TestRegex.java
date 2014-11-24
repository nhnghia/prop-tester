/**
 * Jun 28, 2012 4:20:42 PM
 * @author nhnghia
 */
package test;

public class TestRegex {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String endSoapRegex = ".*(</)(\\w*)(:+)(\\s*)(Envelope)(\\s*)(>).*";
		System.out.println("</soap:Envelope> POST /InStock HTTP/1.1".matches(endSoapRegex));
	}

}
