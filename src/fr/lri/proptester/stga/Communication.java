/**
 * Jul 25, 2012 1:18:18 PM
 * @author nhnghia
 */
package fr.lri.proptester.stga;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class Communication extends Event {

	public String operationName;
	public String sender;
	public String receiver;
	public CommunicationType direction;
	
	/**
	 * variable -- label 
	 */
	public Hashtable<String, String> data;
	
	
	/**
	 * Compare whether 2 events are the same control part (operation, direction, sender, receiver, labels)
	 * @param e
	 * @return
	 */
	public boolean isCompatible(Event e){
		if (e instanceof Tau)
			return false;
		
		Communication com = (Communication) e;
		if (com.operationName.equalsIgnoreCase(operationName)
				&& com.sender.equalsIgnoreCase(sender) 
				&& com.receiver.equalsIgnoreCase(receiver)
				&& com.direction == direction
				&& hasSameLabelSet(com)
				)
			return true;
		
		return false;
	}
	
	boolean hasSameLabelSet(Communication com){
		if (com.data.size() != data.size())
			return false;
		
		Set<String> keys = data.keySet();
		for (String k : keys){
			String v = data.get(k);
			if (!com.data.containsValue(v))
				return false;
		}		
		
		keys = com.data.keySet();
		for (String k : keys){
			String v = com.data.get(k);
			if (!data.containsValue(v))
				return false;
		}		
		
		return true;
	}
	
	@Override
	public String toString() {
		String str = "!";
		if (direction == CommunicationType.RECEPTION)
			str = "?";
		else if (direction == CommunicationType.INTERACTION)
			str = ".";
		
		String sData = "";
		if (data != null && data.size() > 0){
			Set<String> keys = data.keySet();
			for (String k : keys)
				sData += String.format(", \"%s\"=%s", data.get(k), k);
			sData = sData.substring(2);
		}
		return String.format("%s%s[%s,%s](%s)", operationName, str, sender, receiver,sData);
	}

}
