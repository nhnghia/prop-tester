package fr.lri.proptester.stga;

import java.util.List;

/**
 * Jul 23, 2012 2:30:37 PM
 * @author nhnghia
 */

public class Transition {
	public Event event;
	public Guard guard;
	public List<Action> actions;
	public State sourceState, destinationState;

	public String toString(){
		return event.toString();
	}
}
