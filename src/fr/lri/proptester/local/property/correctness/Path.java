/**
 * Jul 26, 2012 6:42:29 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.property.correctness;

import java.util.ArrayList;
import java.util.List;

import fr.lri.proptester.stga.Communication;
import fr.lri.proptester.stga.Event;
import fr.lri.proptester.stga.Transition;


class Path extends ArrayList<Transition>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 10121234L;
	
	public Path(){
		super();
	}
	
	
	int start;
	int end;
	/**
	 * Whether the path has a sub-sequence of transitions such that its events are compatible with eLst
	 * @param eLst
	 * @return
	 */
	public boolean isContain(int startAt, List<Communication> eLst){
		//tools.Debug.println(this.toString());
		//tools.Debug.println(tools.List.toString(eLst, ","));
		start = end = -1;
		int n = size();
		int currentIndex = 0;
		for (int i=startAt; i<n; i++){
			Event e = this.get(i).event;
			if (!(e instanceof Communication))
				continue;
			Communication com = eLst.get(currentIndex);
			if (com.isCompatible(e)){
				if (start == -1){
					start = i;
				}
				currentIndex ++;
				if (currentIndex >= eLst.size()){
					end = i;
					return true;
				}
			}else{
				if (start != -1){
					start = -1;
					currentIndex = 0;
				}
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		String str = "";
		for (Transition t : this){
			str += ", " + t.event.toString();
		}
		if (str.length() > 0)
			str = str.substring(2);
		return str;
	}
}
