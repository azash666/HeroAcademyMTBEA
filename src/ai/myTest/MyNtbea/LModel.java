package ai.myTest.MyNtbea;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import action.Action;
import util.Auxiliares;

public class LModel {
	private Map<Action, Double> individual;
	private Map<Action[], Double> parejas;
	private Map<Action[], Double> completo;
	private Map<Action, Integer> visitasIndividual;
	private Map<Action[], Integer> visitasParejas;
	private Map<Action[], Integer> visitasCompleto;
	private int total;
	private double constante;
	
	public LModel() {

		individual = new HashMap<Action, Double>();
		parejas = new HashMap<Action[], Double>();
		completo = new HashMap<Action[], Double>();
		visitasIndividual = new HashMap<Action, Integer>();
		visitasParejas = new HashMap<Action[], Integer>();
		visitasCompleto = new HashMap<Action[], Integer>();
		total = 0;
		constante = 0.5;
	}
	
	public void addToLModel(Action[] current, double value) {
		total++;
		for (int i=0; i<current.length; i++) {
			Action a = current[i];
			if (individual.containsKey(a)) {
				individual.put(a, (individual.get(a)*visitasIndividual.get(a)+value)/(visitasIndividual.get(a)+1));
				visitasIndividual.put(a, visitasIndividual.get(a)+1);
			}else {
				individual.put(a, value);
				visitasIndividual.put(a, 1);
			}
			for (int j=i+1; j<current.length; j++) {
				Action b = current[j];
				Action[] pair = new Action[] {a, b};
				if(visitasParejas.containsKey(pair)) {
					parejas.put(pair, (parejas.get(pair)*visitasParejas.get(pair)+value)/(visitasParejas.get(pair)+1));
					visitasParejas.put(pair, visitasParejas.get(pair)+1);
				}else {
					parejas.put(pair, value);
					visitasParejas.put(pair, 1);
				}
			}
		} 
		if (visitasCompleto.containsKey(current)) {
			completo.put(current, (completo.get(current)*visitasCompleto.get(current)+value)/(visitasCompleto.get(current)+1));
			visitasCompleto.put(current, visitasCompleto.get(current)+1);
		}else {
			completo.put(current, value);
			visitasCompleto.put(current, 1);
		}
	}
	
	public void combineLModel(LModel lModel) {
		combineSet(lModel.individual, lModel.visitasIndividual, individual, visitasIndividual);
		combineSet(lModel.parejas, lModel.visitasParejas, parejas, visitasParejas);
		combineSet(lModel.completo, lModel.visitasCompleto, completo, visitasCompleto);
		
		total+=lModel.total;
		
	}
	
	public double puntua(Action[] actions) {
		return puntua(actions, true);
	}
	
	public double puntua(Action[] actions, boolean explore) {
		double unvisited = explore?1000000:0;
		double acm = 0;
		//1D
		for (Action a: actions) {
			if (individual.containsKey(a)) acm += individual.get(a)+constante*Math.sqrt(Math.log(total)/visitasIndividual.get(a));
			else acm = unvisited; //Big number
		}
		//2D
		for (int i=0; i<actions.length-1; i++) {
			for (int j= i+1; j<actions.length; j++) {
				Action[] pair = new Action[] {actions[i], actions[j]};
				if (parejas.containsKey(pair)) acm += parejas.get(pair)+constante*Math.sqrt(Math.log(total)/visitasParejas.get(pair));
				else acm = unvisited; //Big number
			}
		}
		//FullD
		if (completo.containsKey(actions)) acm += completo.get(actions)+constante*Math.sqrt(Math.log(total)/visitasCompleto.get(actions));
		else acm = unvisited; //Big number
		return acm/(actions.length + (actions.length*(actions.length-1))/2+1);
	}

	private <K> void combineSet(Map<K, Double> otroMap, Map<K, Integer> otroContador, Map<K, Double> esteMap, Map<K, Integer> esteContador) {
		for(K action: otroMap.keySet()) {
			if (esteMap.containsKey(action)) {
				esteMap.put(action, (esteMap.get(action)*esteContador.get(action)+otroMap.get(action)*otroContador.get(action))/(esteContador.get(action)+otroContador.get(action)));
				esteContador.put(action, esteContador.get(action)+otroContador.get(action));
			}else {
				esteMap.put(action, otroMap.get(action));
				esteContador.put(action, otroContador.get(action));
			}
		}
	}

	public Action[] getBestActions() {
		boolean vacio = true;
		Action[] current = null;
		double value = 0;
		Map<Action[], Double> todos = new HashMap<Action[], Double>();
		for(Action[] acciones: completo.keySet()) {
			if(vacio) {
				current = acciones;
				value = completo.get(acciones);
				vacio = false;
			}else {
				double aux = completo.get(acciones);
				if (aux>value) {
					value = aux;
					current = acciones;
				}
			}
		}
		return current;
	}

	public Set<Action[]> getBestActionsSet() {
		//Auxiliares.imprime(completo);
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<Action[], Double> todos = new HashMap<Action[], Double>();
		/*for(Action[] acciones: completo.keySet()) {
			if(vacio) {
				current = acciones;
				value = completo.get(acciones);
				vacio = false;
			}else {
				double aux = completo.get(acciones);
				if (aux>value) {
					value = aux;
					current = acciones;
				}
			}
		}*/
		return todos.keySet();
	}
	
	public LModel copy() {
		LModel lmodel = new LModel();
		lmodel.combineLModel(this);
		return lmodel;
	}
}
