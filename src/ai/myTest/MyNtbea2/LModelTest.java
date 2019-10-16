package ai.myTest.MyNtbea2;

import java.util.HashSet;
import java.util.Set;

import util.Auxiliares;

public class LModelTest {
	
	public static void main(String[] args) {
		LModel lmodel = new LModel(5);
		LModel lmodel2 = new LModel(5);
		testing_addToModel(lmodel, true);
		testing_combineLModel(lmodel, lmodel2, true);
		testing_puntua_exploracion(lmodel, true);
		testing_puntua_explotacion(lmodel, true);
		//testing_getBestActions(lmodel, true);
		testing_getBestActionsSet(lmodel, true);
	}
	
	private static void testing_getBestActionsSet(LModel lmodel, boolean print) {
		Set<Object[]> current =  lmodel.getBestActionsSet();
		Set<Objeto[]> objs = new HashSet<Objeto[]>();
		for(Object obj: current) {
			Objeto[] aux =  (Objeto[]) obj;
			objs.add(aux);
			if(print) Auxiliares.imprime(aux);
			if (print) System.out.println(lmodel.puntua(aux, false));
		}
	}
	
/*
	private static void testing_getBestActions(LModel lmodel, boolean print) {
		Objeto[] current = (Objeto[]) lmodel.getBestActions();
		if(print) Auxiliares.imprime(current);
		if (print) System.out.println(lmodel.puntua(current));
	}
*/
	private static void testing_puntua_exploracion(LModel lmodel, boolean print) {
		System.out.println("Exploracion");
		Objeto[] current = new Objeto[5];
		current[0] = new Objeto("A"); current[1] = new Objeto("B"); current[2] = new Objeto("C"); current[3] = new Objeto("D"); current[4] = new Objeto("E");
		if(print) System.out.println(lmodel.puntua(current));
		
		current[0] = new Objeto("C"); current[1] = new Objeto("C"); current[2] = new Objeto("C"); current[3] = new Objeto("D"); current[4] = new Objeto("E");
		if(print) System.out.println(lmodel.puntua(current));
		
		lmodel.addToLModel(current, 0);
		if(print) System.out.println(lmodel.puntua(current));
		System.out.println();
		
		Auxiliares.imprime(lmodel);
	}
	private static void testing_puntua_explotacion(LModel lmodel, boolean print) {
		System.out.println("Explotacion");
		Objeto[] current = new Objeto[5];
		current[0] = new Objeto("A"); current[1] = new Objeto("B"); current[2] = new Objeto("C"); current[3] = new Objeto("D"); current[4] = new Objeto("E");
		if(print) System.out.println(lmodel.puntua(current, false));
		
		current[0] = new Objeto("C"); current[1] = new Objeto("B"); current[2] = new Objeto("C"); current[3] = new Objeto("D"); current[4] = new Objeto("E");
		if(print) System.out.println(lmodel.puntua(current, false));
		System.out.println();
	}

	private static void testing_combineLModel(LModel lmodel, LModel lmodel2, boolean print) {
		Objeto[] current = new Objeto[5];
		current[0] = new Objeto("A"); current[1] = new Objeto("B"); current[2] = new Objeto("C"); current[3] = new Objeto("D"); current[4] = new Objeto("E");
		double val = 5;

		lmodel2.addToLModel(current, val);
		
		current = new Objeto[5];
		current[0] = new Objeto("B"); current[1] = new Objeto("B"); current[2] = new Objeto("D"); current[3] = new Objeto("C"); current[4] = new Objeto("E");
		val = 6;
		
		lmodel2.addToLModel(current, val);		
		
		lmodel.combineLModel(lmodel2);
		if(print) Auxiliares.imprime(lmodel);
	}

	private static void testing_addToModel(LModel lmodel, boolean print) {
		Objeto[] current = new Objeto[5];
		current[0] = new Objeto("A"); current[1] = new Objeto("B"); current[2] = new Objeto("C"); current[3] = new Objeto("D"); current[4] = new Objeto("E");
		double val = 5;
		
		lmodel.addToLModel(current, val);
		

		current = new Objeto[5];
		current[0] = new Objeto("A"); current[1] = new Objeto("B"); current[2] = new Objeto("D"); current[3] = new Objeto("C"); current[4] = new Objeto("E");
		val = 4;

		lmodel.addToLModel(current, val);
		
		current = new Objeto[5];
		current[0] = new Objeto("A"); current[1] = new Objeto("B"); current[2] = new Objeto("D"); current[3] = new Objeto("C"); current[4] = new Objeto("F");
		val = 4;
		
		lmodel.addToLModel(current, val);
	
		if(print) Auxiliares.imprime(lmodel);
	}

	
}

class Objeto{
	String id;
	public Objeto (String id) {
		this.id = id;
	}
	
	public String toString() {
		return id;
	}
}