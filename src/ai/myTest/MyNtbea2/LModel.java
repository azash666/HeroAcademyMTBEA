package ai.myTest.MyNtbea2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import action.Action;
import util.Auxiliares;

public class LModel {
	public Map<Integer, Map<String, Double>> individual;
	private Map<Integer, Map<String, Integer>> visitasIndividual;
	private Map<Integer, Double> mediaIndividual;
	private Map<Integer, Long> cantidadIndividual;
	
	public Map<Integer, Map<String, Double>> parejas;
	private Map<Integer, Map<String, Integer>> visitasParejas;
	private Map<Integer, Double> mediaParejas;
	private Map<Integer, Long> cantidadParejas;
	
	public Map<String, Double> completo;
	private Map<String, Integer> visitasCompleto;
	private double mediaCompleto;
	private long cantidadCompleto;
	private int total;
	
	public int size;
	
	private double constante;
	public Map<String, Action[]> diccionario;
	
	public LModel(int size) {
		
		individual = new HashMap<Integer, Map<String, Double>>();
		visitasIndividual = new HashMap<Integer, Map<String,Integer>>();
		mediaIndividual = new HashMap<Integer, Double>();
		cantidadIndividual = new HashMap<Integer, Long>();
		
		parejas = new HashMap<Integer, Map<String,Double>>();
		visitasParejas = new HashMap<Integer, Map<String,Integer>>();
		mediaParejas = new HashMap<Integer, Double>();
		cantidadParejas = new HashMap<Integer, Long>();
		
		completo = new HashMap<String, Double>();
		visitasCompleto = new HashMap<String, Integer>();
		cantidadCompleto = 0;
		mediaCompleto = 0.0;
		
		diccionario = new HashMap<String, Action[]>();
		total = 0;
		constante = 1 / Math.sqrt(2);

		this.size = size;
		for(int i=0; i<size; i++) {
			individual.put(i, new HashMap<String, Double>());
			visitasIndividual.put(i, new HashMap<String,Integer>());
			mediaIndividual.put(i, 0.0);
			cantidadIndividual.put(i, 0L);
			for(int j=i+1; j<size; j++) {
				int par = i*size+ j;
				parejas.put(par, new HashMap<String, Double>());
				visitasParejas.put(par, new HashMap<String,Integer>());
				mediaParejas.put(par, 0.0);
				cantidadParejas.put(par, 0L);
			}
		}
	}
	
	public void addToLModel(Action[] current, double value) {
		total++;
		for (int i=0; i<current.length; i++) {
			Action action = current[i];
			String hash = action.toString();
			mediaIndividual.put(i,(mediaIndividual.get(i)*cantidadIndividual.get(i)+value)/(cantidadIndividual.get(i)+1));
			cantidadIndividual.put(i, cantidadIndividual.get(i)+1);
			if (individual.get(i).containsKey(hash)) {
				individual.get(i).put(hash, (individual.get(i).get(hash)*visitasIndividual.get(i).get(hash)+value)/(visitasIndividual.get(i).get(hash)+1));
				visitasIndividual.get(i).put(hash, visitasIndividual.get(i).get(hash)+1);
			}else {
				individual.get(i).put(hash, value);
				visitasIndividual.get(i).put(hash, 1);
			}
			for (int j=i+1; j<current.length; j++) {
				int par = i*size+ j;
				//Auxiliares.imprime(mediaParejas.keySet());
				//System.out.println(par);
				mediaParejas.put(par, ((mediaParejas.get(par)*cantidadParejas.get(par)+value))/(cantidadParejas.get(par)+1));
				cantidadParejas.put(par, cantidadParejas.get(par)+1);
				Action b = current[j];
				String pair = action.toString()+ b.toString();
				if(visitasParejas.get(par).containsKey(pair)) {
					parejas.get(par).put(pair, (parejas.get(par).get(pair)*visitasParejas.get(par).get(pair)+value)/(visitasParejas.get(par).get(pair)+1));
					visitasParejas.get(par).put(pair, visitasParejas.get(par).get(pair)+1);
				}else {
					parejas.get(par).put(pair, value);
					visitasParejas.get(par).put(pair, 1);
				}
			}
		}
		mediaCompleto = (mediaCompleto*cantidadCompleto+value)/(cantidadCompleto+1);
		cantidadCompleto++;
		String cadena = "";
		for (Action aux: current) {
			cadena = cadena + aux.toString();
		}
		
		diccionario.put(cadena, current);
		
		if (visitasCompleto.containsKey(cadena)) {
			completo.put(cadena, (completo.get(cadena)*visitasCompleto.get(cadena)+value)/(visitasCompleto.get(cadena)+1));
			visitasCompleto.put(cadena, visitasCompleto.get(cadena)+1);
		}else {
			completo.put(cadena, value);
			visitasCompleto.put(cadena, 1);
		}
	}
	
	public void combineLModel(LModel lModel) {
		for(int i=0; i<size; i++) {
			combineSet(lModel.individual.get(i), lModel.visitasIndividual.get(i), individual.get(i), visitasIndividual.get(i));
			mediaIndividual.put(i, mediaIndividual.get(i)*cantidadIndividual.get(i)+lModel.mediaIndividual.get(i)*lModel.cantidadIndividual.get(i)/(cantidadIndividual.get(i)+lModel.cantidadIndividual.get(i)));
			cantidadIndividual.put(i, cantidadIndividual.get(i)+lModel.cantidadIndividual.get(i));
			for(int j=i+1; j<size; j++) {
				int par = i*size+ j;
				combineSet(lModel.parejas.get(par), lModel.visitasParejas.get(par), parejas.get(par), visitasParejas.get(par));
				mediaParejas.put(par,mediaParejas.get(par)*cantidadParejas.get(par)+lModel.mediaParejas.get(par)*lModel.cantidadParejas.get(par)/(cantidadParejas.get(par)+lModel.cantidadParejas.get(par)));
				cantidadParejas.put(par, cantidadParejas.get(par)+ lModel.cantidadParejas.get(par));
			}
		}
		combineSet(lModel.completo, lModel.visitasCompleto, completo, visitasCompleto);
		mediaCompleto = (mediaCompleto*cantidadCompleto+lModel.mediaCompleto*lModel.cantidadCompleto)/(cantidadCompleto+lModel.cantidadCompleto);
		cantidadCompleto += lModel.cantidadCompleto;
		total+=lModel.total;
		diccionario.putAll(lModel.diccionario);
	}
	
	public double puntua(Action[] actions) {
		return puntua(actions, true);
	}
	
	public double puntua(Action[] actions, boolean explore) {
		double acm = 0;
		Random r = new Random();
		//1D
		int aux=0;
		for (Action action: actions) {
			String a = action.toString();
			if (individual.get(aux).containsKey(a)) acm += individual.get(aux).get(a)+2*constante*Math.sqrt(2*Math.log(total)/visitasIndividual.get(aux).get(a));
			else acm += explore?(10000000+1000*r.nextDouble()):mediaIndividual.get(aux); //Big number
			aux++;
		}
		//2D
		for (int i=0; i<actions.length-1; i++) {
			for (int j= i+1; j<actions.length; j++) {
				int par = i*size+ j;
				String pair = actions[i].toString()+ actions[j].toString();
				if (parejas.get(par).containsKey(pair)) acm += parejas.get(par).get(pair)+2*constante*Math.sqrt(2*Math.log(total)/visitasParejas.get(par).get(pair));
				else acm += explore?(10000000+1000*r.nextDouble()):mediaParejas.get(par); //Big number
			}
		}
		//FullD
		String cadena = "";
		for (Action aux2: actions) {
			cadena = cadena + aux2.toString();
		}
		if (completo.containsKey(cadena)) acm += completo.get(cadena)+2*constante*Math.sqrt(2*Math.log(total)/visitasCompleto.get(cadena));
		else acm += explore?(10000000+1000*r.nextDouble()):mediaCompleto; //Big number
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
		//Map<Action[], Double> todos = new HashMap<Action[], Double>();
		for(String key: completo.keySet()) {
			Action[] acciones = diccionario.get(key);
			if(vacio) {
				current = acciones;
				value = completo.get(key);
				vacio = false;
			}else {
				double aux = completo.get(key);
				if (aux>value) {
					value = aux;
					current = acciones;
				}
			}
		}
		return current;
	}

	public Set<Action[]> getBestActionsSet() {
		Set<Action[]> devolver = new HashSet<Action[]>();
		for(String key : completo.keySet()) {
			//Auxiliares.imprime(diccionario.keySet());
			devolver.add(diccionario.get(key));
		}
		return devolver;
	}
	
	public LModel copy() {
		LModel lmodel = new LModel(size);
		lmodel.combineLModel(this);
		return lmodel;
	}
}
