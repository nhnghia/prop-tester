package fr.lri.proptester.local.xqueryOnWindow;

/**
 * Abstract class to execute xquery, which then is implemented with MXQuery, Saxon, ...
 * @author nhnghia
 *
 */

public abstract class Query {
	String queryStr;
	public Query(String queryStr){
		this.queryStr = queryStr;
	}
	/**
	 * Exec queryStr on xmlDocument then return result
	 * @param queryStr
	 * @param xmlDocument
	 * @return
	 * @throws Exception 
	 */
	public abstract Object query (String xmlDocument) throws Exception;
}
