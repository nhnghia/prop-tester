/**
 * Jul 25, 2012 12:50:19 PM
 * @author nhnghia
 */
package fr.lri.proptester.global;

import java.util.ArrayList;
import java.util.List;

/**
 * Read verdict from a list of stream
 * @author nhnghia
 *
 */
public class LocalResult {
	List<ResultLocalFilter> resultLst;
	
	public LocalResult(List<String> lst){
		resultLst = new ArrayList<ResultLocalFilter>();
		for (String s : lst)
			resultLst.add(new ResultLocalFilter(s));
	}
	
	/**
	 * Get an element &lt;vdict/&gt; from list of stream<br>
	 * Rule:<br>
	 *   - Which stream has &lt;vdict/&gt; first will be get<br>
	 *   - Each stream is visited from one by one 
	 * @return an element &lt;vdict/&gt; or <br>
	 * <code>null</code> when no more vdict 
	 */
	public String getResult(){
		int currentIndex=0; 	//index of current stream is beging get
		while(true){
			
			//OK, all streams were closed
			if (resultLst.size() == 0){
				return null;
			}
			
			//round robin
			if (currentIndex >= resultLst.size())
				currentIndex = 0;
			
			ResultLocalFilter rf = resultLst.get(currentIndex);
			if (!rf.isEndOfStream()){
				currentIndex ++;	//round robin
				String s = rf.getMessage();
				if (s != null)
					return s;
			}else{
				//This stream is closed
				rf.close();
				resultLst.remove(currentIndex);
			}

		}
	}
	
}
