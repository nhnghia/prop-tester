/**
 * Jun 20, 2012 4:03:09 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.property;


import fr.lri.proptester.stga.Communication;
import fr.lri.schora.expr.Condition;

public class CandidateEvent {
	public Communication event;
	public Condition predicate;
	public String name;
	
	public CandidateEvent(Communication com, Condition con){
		this.event = com;
		this.predicate = con;
	}
	
	@Override
	public String toString(){
		return String.format("%s/(%s)", event.toString(), predicate.toString());
	}
}
