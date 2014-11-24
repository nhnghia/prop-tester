package fr.lri.proptester.local.xquery;


public abstract class XQuery {
	protected Output output;
	public XQuery(Output output){
		this.output = output;
	}
	
    /**
	 * Put data into stream of query
	 * Put NULL to end stream
	 * @param msg
	 * @throws MXQueryException 
	 */
	public abstract void putData(String msg) throws Exception;
	
	
	// If there is no verdict being emit before, an INCONCLUSIVE verdict will be given
	// Print also statistic: number of pass, fail
	public void printStat(){
		output.printStat();
	}
}
