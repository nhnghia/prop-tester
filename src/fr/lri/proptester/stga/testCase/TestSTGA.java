/**
 * Jul 25, 2012 5:07:27 PM
 * @author nhnghia
 */
package fr.lri.proptester.stga.testCase;

import java.util.ArrayList;
import java.util.Hashtable;

import org.junit.Test;

import fr.lri.proptester.stga.*;
import fr.lri.schora.expr.*;
import fr.lri.schora.util.Debug;

public class TestSTGA {

	/**
	 * Test method for {@link fr.lri.proptester.stga.STGA#saveToFile(java.lang.String)}.
	 */
	//@Test
	public final void testSaveToFile() throws Exception{
		STGA stga = new STGA();
		State s  = new State();
		s.name = "s0";
		s.freeVariables = new ArrayList<Variable>();
		Variable v = ExprFactory.eINSTANCE.createVariable();
		v.setName("x");
		s.freeVariables.add(v);
		
		stga.initState = s;
		
		State[] states = new State[6];
		states[0] = stga.initState;
		
		stga.states = new ArrayList<State>();
		stga.states.add(s);
		
		for (int i=1; i<5; i++){
			s = new State();
			s.name = "s" + i ;
			stga.states.add(s);
			states[i] = s;
		}
		
		stga.transitions = new ArrayList<Transition>();
		for (int i=1; i<5; i++){
			Transition t = new Transition();
			t.sourceState = states[i-1];
			t.destinationState = states[i];
			if (i % 2 == 0)
				t.event = new Tau();
			else{
				Communication com = new Communication();
				com.operationName = "Send" + i;
				com.sender= "emmetteur";
				com.receiver = "recepteur";
				com.direction = CommunicationType.INTERACTION;
				com.data = new Hashtable<String, String>();
				com.data.put("x", "quantity");
				com.data.put("y", "information/id");
				t.event = com;
			}
			t.guard = new Guard("x > 0");
			t.actions = new ArrayList<Action>();
			for (int j=0; j<3; j++){
				Action a = new Action("x" + j + ":=0");
				t.actions.add(a);
			}
			stga.transitions.add(t);
		}
		
		stga.saveToFile("bin/stga.xml");
			
	}
	
	@Test
	public final void testGetSTGA(){
		try{
			STGA stga = STGA.getSTGA("bin/testCase/client.stga.xml");
			stga = STGA.getSTGA("bin/testCase/quotation.stga.xml");
			stga = STGA.getSTGA("bin/testCase/accounting.stga.xml");
			stga = STGA.getSTGA("bin/testCase/bank.stga.xml");
			
			stga.saveToFile("bin/stga1.xml");
		}catch(Exception ex){
			Debug.print(ex);
		}
	}

}
