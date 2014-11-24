/**
 * Jul 25, 2012 1:18:45 PM
 * @author nhnghia
 */
package fr.lri.proptester.stga;

import fr.lri.schora.expr.Expression;
import fr.lri.schora.expr.util.DataExpression;

public class Action {
	String variableName;
	Expression expression;
	
	public Action(String con) throws Exception {
		String [] s = con.split(":=");
		if (s.length != 2)
			throw new Exception("Assignment := is expected");
		
		variableName = s[0];
		expression = DataExpression.parser(s[1]);
	}
	
	public String toString(){
		return String.format("%s := %s", variableName, expression.toString());
	}
}
