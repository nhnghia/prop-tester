package fr.lri.proptester.stga;

import fr.lri.schora.expr.Condition;
import fr.lri.schora.expr.ExprFactory;
import fr.lri.schora.expr.util.BoolExpression;

/**
 * Jul 23, 2012 2:31:29 PM
 * @author nhnghia
 */

public class Guard{
	Condition condition;
	
	public Guard(){
		condition = ExprFactory.eINSTANCE.createBTrue();
	}
	
	public Guard(String con) throws Exception{
		if (con == null || con == "")
			condition = ExprFactory.eINSTANCE.createBTrue();
		else
			condition = BoolExpression.parser(con);
	}
	
	/**
	 * @return the condition
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * @param condition the condition to set
	 */
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
	public String toString(){
		return condition.toString();
	}
}
