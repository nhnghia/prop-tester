package fr.lri.proptester.local.property.correctness;

import java.util.ArrayList;
import java.util.List;

import xsmt.command.Assert;
import xsmt.command.Check_sat;
import xsmt.command.Declare_fun;
import xsmt.command.Pop;
import xsmt.command.Push;
import xsmt.command.Set_option;
import xsmt.expression.Expression;
import xsmt.response.IResponse;
import xsmt.solver.z3.Z3Solver;
import fr.lri.schora.expr.Condition;
import fr.lri.schora.expr.Variable;

public class Solver {
	
	Z3Solver z3Solver;	
	/**
	 * Create a solver
	 * @param z3SolverPath : path to executable Z3 Solver, e.g. in Mac OS: /Users/toto/soft/z3/bin/z3
	 */
	public Solver(String z3SolverPath){
		z3Solver = new Z3Solver(z3SolverPath);
		isZ3Started = false;
	}
	
	/**
	 * Verify whether condition is TRUE
	 * @param condition
	 * @return
	 * @throws Exception
	 */
	boolean isZ3Started;
	
	public boolean isSatisfy(Condition condition) throws Exception{
		
		try {
			if (!isZ3Started){
				z3Solver.start();
				z3Solver.execute(new Set_option("print-warning", "false"));
				isZ3Started = true;
			}
			z3Solver.execute(new Push(1));
			
			//Declare variables
			List<Variable> vars = condition.freeVariables();
			for (Variable v : vars){
				z3Solver.execute(createDeclareFun(v.getName()));
			}
			
			
			z3Solver.execute(createAssert(condition.toZ3Format(), true));
			
			IResponse response = z3Solver.execute(new Check_sat());
			
			//i.e.: Z3 not found at least one evaluation of variable for assertedFunction is TRUE
			String resp = response.toString().trim();
			if (resp.equalsIgnoreCase("sat")){
				z3Solver.execute(new Pop(1));
				return true;
			}
			z3Solver.execute(new Pop(1));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private Declare_fun createDeclareFun(String var){
		Declare_fun fun = new Declare_fun(var, "Int", new ArrayList<String>());
		return fun;
	}
	
	private Assert createAssert(String assertedFunction, boolean val){
		String str = String.format("(= %s %s)", assertedFunction, (val?"true":"false"));
		fr.lri.schora.util.Debug.println(str);
		return new Assert(new Expression(str));
	}
}
