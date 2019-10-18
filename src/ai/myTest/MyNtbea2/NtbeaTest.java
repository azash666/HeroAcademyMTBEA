package ai.myTest.MyNtbea2;

import java.util.Set;

import util.Auxiliares;

public class NtbeaTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Ntbea ntbea = new Ntbea();
		
		Objeto[] current = new Objeto[5];
		current[0] = new Objeto("A"); current[1] = new Objeto("B"); current[2] = new Objeto("C"); current[3] = new Objeto("D"); current[4] = new Objeto("E");
		Set<Objeto[]> vecinos = ntbea.neighbors(null, current, 10, 0.0, true);
		for(Objeto[] i : vecinos) {
			Auxiliares.imprime(i);
		}
	}

}

