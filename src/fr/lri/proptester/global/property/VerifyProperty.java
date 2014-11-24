package fr.lri.proptester.global.property;
import java.io.*;

import fr.lri.proptester.local.xqueryOnWindow.Query;
public class VerifyProperty {
	OutputStream output;
	Query query0; //
	public int nPass, nFail;
	
	public VerifyProperty(Property p, OutputStream out){
		output = out;
		
	}
}
