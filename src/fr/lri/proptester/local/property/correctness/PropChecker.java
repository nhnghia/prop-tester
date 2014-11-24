/**
 * Jul 28, 2012 8:44:35 PM
 * @author nhnghia
 */
package fr.lri.proptester.local.property.correctness;

import java.util.List;

import fr.lri.proptester.stga.STGA;
import fr.lri.proptester.local.property.Property;

public class PropChecker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 4){
			System.out.println("java -jar prop-checker.jar propertyFile stgaFile depth z3SolverFile");
			return;
		}
		
		List<Property> propLst;
		try {
			propLst = Property.getProperties(args[0]);
			int length=100;
			length = Integer.parseInt(args[2]);

			System.out.println("List of properties: ");
			System.out.print(fr.lri.schora.util.List.toString(propLst, "\n"));
			System.out.println();
			
			STGA stga = STGA.getSTGA(args[1]);
			Correctness cor = new Correctness();
			
			System.out.println("Checking the correctness of properties ....");
			for (Property p : propLst){
				boolean b = cor.isCorrect(p, stga, length, args[3]); 
				System.out.println(String.format("%s ===> %s", p.name, (b?"correct":"incorrect")));
			}
			
		}catch (NumberFormatException e){
			System.out.println("ERROR: A number is expected for the depth parameter");
		}catch (Exception e) {
			System.out.println("ERROR: " + e.getMessage());
			fr.lri.schora.util.Debug.print(e);
		}
	}

}
