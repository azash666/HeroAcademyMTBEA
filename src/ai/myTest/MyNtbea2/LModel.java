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
	public Map<Integer, Map<String, Integer>> visitasIndividual;
	public Map<Integer, Double> mediaIndividual;
	public Map<Integer, Integer> cantidadIndividual;
	
	public Map<Integer, Map<String, Double>> parejas;
	public Map<Integer, Map<String, Integer>> visitasParejas;
	public Map<Integer, Double> mediaParejas;
	public Map<Integer, Integer> cantidadParejas;
	
	public Map<String, Double> completo;
	public Map<String, Integer> visitasCompleto;
	public double mediaCompleto;
	public int cantidadCompleto;
	public int total;
	
	public int size;
	
	public double constante;
	public Map<String, Object[]> diccionario;
	
	public LModel(int size) {
		
		individual = new HashMap<Integer, Map<String, Double>>();
		visitasIndividual = new HashMap<Integer, Map<String,Integer>>();
		mediaIndividual = new HashMap<Integer, Double>();
		cantidadIndividual = new HashMap<Integer, Integer>();
		
		parejas = new HashMap<Integer, Map<String,Double>>();
		visitasParejas = new HashMap<Integer, Map<String,Integer>>();
		mediaParejas = new HashMap<Integer, Double>();
		cantidadParejas = new HashMap<Integer, Integer>();
		
		completo = new HashMap<String, Double>();
		visitasCompleto = new HashMap<String, Integer>();
		cantidadCompleto = 0;
		mediaCompleto = 0.0;
		
		diccionario = new HashMap<String, Object[]>();
		total = 0;
		constante = 1 / Math.sqrt(2);

		this.size = size;
		for(int i=0; i<size; i++) {
			individual.put(i, new HashMap<String, Double>());
			visitasIndividual.put(i, new HashMap<String,Integer>());
			mediaIndividual.put(i, 0.0);
			cantidadIndividual.put(i, 0);
			for(int j=i+1; j<size; j++) {
				int par = i*size+ j;
				parejas.put(par, new HashMap<String, Double>());
				visitasParejas.put(par, new HashMap<String,Integer>());
				mediaParejas.put(par, 0.0);
				cantidadParejas.put(par, 0);
			}
		}
	}
	
	public void addToLModel(Object[] current, double value) {
		if(current[0] == null) return;
		total++;
		for (int i=0; i<current.length; i++) {
			//Auxiliares.imprime(current);
			Object action = current[i];
			//if (Action== null) break;
			String hash = action.toString();
			double media = mediaIndividual.get(i)*individual.get(i).size();
			if (individual.get(i).containsKey(hash)) {
				double newvalue;
				media = media-individual.get(i).get(hash);
				individual.get(i).put(hash, (newvalue=((individual.get(i).get(hash)*visitasIndividual.get(i).get(hash)+value)/(visitasIndividual.get(i).get(hash)+1))));
				visitasIndividual.get(i).put(hash, visitasIndividual.get(i).get(hash)+1);
				media = (media+newvalue);
			}else {
				individual.get(i).put(hash, value);
				visitasIndividual.get(i).put(hash, 1);
				media = (media+value);
			}
			cantidadIndividual.put(i, individual.get(i).size());
			mediaIndividual.put(i,media/individual.get(i).size());
			for (int j=i+1; j<current.length; j++) {
				int par = i*size+ j;
				//Auxiliares.imprime(mediaParejas.keySet());
				//System.out.println(par);
				Object b = current[j];
				String pair = action.toString()+ b.toString();
				media = mediaParejas.get(par)*parejas.get(par).size();
				if(visitasParejas.get(par).containsKey(pair)) {
					double newvalue;
					media = media-parejas.get(par).get(pair);
					parejas.get(par).put(pair, newvalue=(parejas.get(par).get(pair)*visitasParejas.get(par).get(pair)+value)/(visitasParejas.get(par).get(pair)+1));
					visitasParejas.get(par).put(pair, visitasParejas.get(par).get(pair)+1);
					media = (media+newvalue);
				}else {
					parejas.get(par).put(pair, value);
					visitasParejas.get(par).put(pair, 1);
					media = (media+value);
				}
				mediaParejas.put(par, media/parejas.get(par).size());
				cantidadParejas.put(par, parejas.get(par).size());
			}
		}
		String cadena = "";
		for (Object aux: current) {
			cadena = cadena + aux.toString();
		}
		
		diccionario.put(cadena, current);

		double media = mediaCompleto*completo.size();
		if (visitasCompleto.containsKey(cadena)) {
			double newvalue;
			media = media-completo.get(cadena);
			completo.put(cadena, newvalue=(completo.get(cadena)*visitasCompleto.get(cadena)+value)/(visitasCompleto.get(cadena)+1));
			visitasCompleto.put(cadena, visitasCompleto.get(cadena)+1);
			media = (media+newvalue);
		}else {
			completo.put(cadena, value);
			visitasCompleto.put(cadena, 1);
			media = (media+value);
		}
		mediaCompleto = media/completo.size();
		cantidadCompleto=completo.size();
	}
	
	public void combineLModel(LModel lModel) {
		for(int i=0; i<size; i++) {
			combineSet(lModel.individual.get(i), lModel.visitasIndividual.get(i), individual.get(i), visitasIndividual.get(i));
			double media = 0;
			for(String key: individual.get(i).keySet()) {
				media+=individual.get(i).get(key);
			}
			mediaIndividual.put(i, media/individual.get(i).size());
			cantidadIndividual.put(i, individual.get(i).size());
			for(int j=i+1; j<size; j++) {
				int par = i*size+ j;
				combineSet(lModel.parejas.get(par), lModel.visitasParejas.get(par), parejas.get(par), visitasParejas.get(par));
				media = 0;
				for(String key: parejas.get(par).keySet()) {
					media+=parejas.get(par).get(key);
				}
				mediaParejas.put(par, media/parejas.get(par).size());
				cantidadParejas.put(par, parejas.get(par).size());
			}
		}
		combineSet(lModel.completo, lModel.visitasCompleto, completo, visitasCompleto);
		double media = 0;
		for(String key: completo.keySet()) {
			media+=completo.get(key);
		}
		mediaCompleto = media/completo.size();
		cantidadCompleto = completo.size();
		total+=lModel.total;
		diccionario.putAll(lModel.diccionario);
	}
	
	public double puntua(Object[] actions) {
		return puntua(actions, true);
	}
	
	public double puntua(Object[] actions, boolean explore) {
		if(actions.length==0) return -1000000000;
		double acm = 0;
		Random r = new Random();
		//1D
		int aux=0;
		for (Object action: actions) {
			String a = action.toString();
			if (individual.get(aux).containsKey(a)) acm += individual.get(aux).get(a)+(explore?2*constante*Math.sqrt(2*Math.log(total)/visitasIndividual.get(aux).get(a)):0);
			else acm += explore?(10000000+1000*r.nextDouble()):mediaIndividual.get(aux); //Big number
			aux++;
		}
		//2D
		for (int i=0; i<actions.length-1; i++) {
			for (int j= i+1; j<actions.length; j++) {
				int par = i*size+ j;
				String pair = actions[i].toString()+ actions[j].toString();
				if (parejas.get(par).containsKey(pair)) acm += parejas.get(par).get(pair)+(explore?2*constante*Math.sqrt(2*Math.log(total)/visitasParejas.get(par).get(pair)):0);
				else acm += explore?(10000000+1000*r.nextDouble()):mediaParejas.get(par); //Big number
			}
		}
		//FullD
		String cadena = "";
		for (Object aux2: actions) {
			cadena = cadena + aux2.toString();
		}
		if (completo.containsKey(cadena)) acm += completo.get(cadena)+(explore?2*constante*Math.sqrt(2*Math.log(total)/visitasCompleto.get(cadena)):0);
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
/*
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
*/
	public Set<Object[]> getBestActionsSet() {
		Set<Object[]> devolver = new HashSet<Object[]>();
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
