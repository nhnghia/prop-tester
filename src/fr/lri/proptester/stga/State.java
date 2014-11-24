/**
 * Jul 25, 2012 1:19:45 PM
 * @author nhnghia
 */
package fr.lri.proptester.stga;

import java.util.List;

import fr.lri.schora.expr.*;
public class State {
	public String name;
	public List<Variable> freeVariables;
	
	@Override
	public boolean equals(Object obj){
		if (obj instanceof State){
			State s = (State) obj;
			return s.name.equalsIgnoreCase(name);
		}
		return false;
	}
	
	public String toString(){
		return name;
	}
}
