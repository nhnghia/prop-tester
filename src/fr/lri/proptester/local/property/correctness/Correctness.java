/**
 * Jul 23, 2012 1:54:25 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.property.correctness;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import fr.lri.proptester.stga.Communication;
import fr.lri.proptester.stga.CommunicationType;
import fr.lri.proptester.stga.STGA;
import fr.lri.proptester.stga.State;
import fr.lri.proptester.stga.Tau;
import fr.lri.proptester.stga.Transition;
import fr.lri.proptester.local.property.CandidateEvent;
import fr.lri.proptester.local.property.Property;
import fr.lri.schora.expr.Condition;
import fr.lri.schora.expr.ExprFactory;
import fr.lri.schora.expr.RelationalCondition;
import fr.lri.schora.expr.Variable;
import fr.lri.schora.expr.util.BoolExpression;

public class Correctness {
	Property property;
	STGA stga;
	int length;
	Solver solver;
	public boolean isCorrect(Property p, STGA stga, int length, String z3Solver){
		this.property = p;
		this.stga = stga;
		this.length = length;
		solver = new Solver(z3Solver);
		
		isExistOnePathPass = false;
		path = new Path();
		try{
			visitSTGA(stga, stga.initState);
		}catch (Exception ex){
			fr.lri.schora.util.Debug.print(ex);
			return false;
		}
		return isExistOnePathPass;
	}

	
	Path path;
	
	void visitSTGA(STGA stga, State s) throws Exception{
		List<Transition> trans = stga.getOutgoingTransitions(s);

		if (trans.size() == 0 || path.size() >= length){
			verifyPath(property, path);
		}
		
		Path rootPath = null;
		if (trans.size() > 1){
			rootPath = (Path)path.clone();
		}
		
		for (Transition t : trans){
			if (rootPath != null)
				path = (Path) rootPath.clone();

			path.add(t);
			visitSTGA(stga, t.destinationState);
		}
		
	}

	boolean isExistOnePathPass;
	void verifyPath(Property prop, Path pa) throws Exception{
		List<Communication> eLst = new ArrayList<Communication>();
		for (CandidateEvent c : prop.context)
			eLst.add(c.event);
		
		int index = 0;
		while (pa.isContain(index, eLst)){
			index = pa.end + 1; 
			fr.lri.schora.util.Debug.println(pa.start + " - " + pa.end);
			Condition con = getPathCondition(pa, pa.start, pa.end, prop.context, true);
			
			int n = index + prop.numberOfMessage + 1;
			if (n > pa.size())
				n = pa.size();
			
			if (solver.isSatisfy(con)){
				for (CandidateEvent ca : prop.consequence)
					for (int i = index; i<n; i++){
						if (ca.event.isCompatible(pa.get(i).event)){
							Condition con2 = pa.get(index).guard.getCondition();
							for (int j=index; j<i; j++){
								con2 = BoolExpression.createAnd(con2, pa.get(j).guard.getCondition());
							}
							con2 = BoolExpression.createAnd(con2, ca.predicate);
							con2 = BoolExpression.createAnd(con2, getCompatibleCondition(ca.event, (Communication)pa.get(i).event));
							
							con = BoolExpression.createAnd(con, con2);
							
							if (solver.isSatisfy(con))
								isExistOnePathPass = true;
							else
								throw new Exception("not satisfy");
						}
					}
			}
			//tools.Debug.println(con.toString());
		}
	}
	
	/**
	 * Get path of conditions with adding predicate in "ceLst" from "start" to "end"
	 * @param p
	 * @param start
	 * @param end
	 * @param ceLst
	 * @return
	 * @throws Exception 
	 */
	Condition getPathCondition(Path p, int start, int end, List<CandidateEvent> ceLst, boolean takeFirstPart) throws Exception{
		if (start < 0 || end < 0 || p == null || ceLst == null
				|| start >= p.size() || end >= p.size() || (end - start) > ceLst.size()){
			throw new Exception("malformatted");
		}
		
		Condition bTrue = ExprFactory.eINSTANCE.createBTrue();		
		Condition con = bTrue;
		
		if (takeFirstPart){
			con = p.get(0).guard.getCondition();
			for (int i=1; i<start; i++)
				con = BoolExpression.createAnd(con, p.get(i).guard.getCondition());
		}
		
		Condition varphi = bTrue;
		
		for (int i=start; i<= end; i++){
			
			con = BoolExpression.createAnd(con, p.get(i).guard.getCondition());
			
			if (p.get(i).event instanceof Tau)
				continue;
			
			CandidateEvent ce = ceLst.get(i-start);
			
			varphi = BoolExpression.createAnd(varphi, getCompatibleCondition((Communication)p.get(i).event, ce.event));
			
			if (ce.event.direction == CommunicationType.RECEPTION){
				con = BoolExpression.createAnd(con, varphi);
				
				varphi = ce.predicate;
			}
			else{
				con = BoolExpression.createAnd(con, ce.predicate);
				con = BoolExpression.createAnd(con, varphi);
				varphi = bTrue;
			}
		}
		
		con = BoolExpression.createAnd(con, varphi);
		
		return con;
	}
	
	/**
	 * c1 is compatible with c2, now consider identify variable condition
	 * @param c1
	 * @param c2
	 * @return
	 */
	Condition getCompatibleCondition(Communication c1, Communication c2){
		Condition con = ExprFactory.eINSTANCE.createBTrue();
		Set<String> keys1 = c1.data.keySet();
		Set<String> keys2 = c2.data.keySet();
		for (String k1 : keys1){
			String v1 = c1.data.get(k1);
			
			for (String k2 : keys2){
				String v2 = c2.data.get(k2);
				if (v1.equalsIgnoreCase(v2)){
					con = BoolExpression.createAnd(con, createRelationCondition(k1, k2));
				}
			}
		}
		return con;
	}
	
	Condition createRelationCondition(String var1, String var2){
		RelationalCondition rel = ExprFactory.eINSTANCE.createRelationalCondition();
		Variable v1 = ExprFactory.eINSTANCE.createVariable();
		v1.setName(var1);
		
		Variable v2 = ExprFactory.eINSTANCE.createVariable();
		v2.setName(var2);
		rel.setLeft(v1);
		rel.setRight(v2);
		rel.setOp("==");
		return rel;
	}
}
