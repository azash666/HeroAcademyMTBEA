package ai.myTest.MyNtbfea;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class LModel {
	public Map<Integer, Map<String, Double>> score_1D;
	public Map<Integer, Map<String, Integer>> visits_1D;
	public Map<Integer, Double> average_1D;
	public Map<Integer, Integer> quantity_1D;
	
	public Map<Integer, Map<String, Double>> score_2D;
	public Map<Integer, Map<String, Integer>> visits_2D;
	public Map<Integer, Double> average_2D;
	public Map<Integer, Integer> quantity_2D;
	
	public Map<String, Double> score_5D;
	public Map<String, Integer> visits_5D;
	public double average_5D;
	public int quantity_5D;
	//public int total;
	
	public int numberOfActions;
	
	public double constante;
	public Map<String, Object[]> stringToActionsTranslator;
	
	public LModel(int numberOfActions) {
		
		score_1D = new HashMap<Integer, Map<String, Double>>();
		visits_1D = new HashMap<Integer, Map<String,Integer>>();
		average_1D = new HashMap<Integer, Double>();
		quantity_1D = new HashMap<Integer, Integer>();
		
		score_2D = new HashMap<Integer, Map<String,Double>>();
		visits_2D = new HashMap<Integer, Map<String,Integer>>();
		average_2D = new HashMap<Integer, Double>();
		quantity_2D = new HashMap<Integer, Integer>();
		
		score_5D = new HashMap<String, Double>();
		visits_5D = new HashMap<String, Integer>();
		average_5D = 0.0;
		quantity_5D = 0;
		
		stringToActionsTranslator = new HashMap<String, Object[]>();
		constante = 1 / Math.sqrt(2);

		this.numberOfActions = numberOfActions;
		for(int numActionIndex = 0; numActionIndex < numberOfActions; numActionIndex++) {
			score_1D.put(numActionIndex, new HashMap<String, Double>());
			visits_1D.put(numActionIndex, new HashMap<String,Integer>());
			average_1D.put(numActionIndex, 0.0);
			quantity_1D.put(numActionIndex, 0);
			for(int numAction2Index = numActionIndex+1; numAction2Index < numberOfActions; numAction2Index++) {
				int key_2D = numActionIndex*numberOfActions+ numAction2Index;
				score_2D.put(key_2D, new HashMap<String, Double>());
				visits_2D.put(key_2D, new HashMap<String,Integer>());
				average_2D.put(key_2D, 0.0);
				quantity_2D.put(key_2D, 0);
			}
		}
	}
	
	public void addToLModel(Object[] current, double value) {
		if(current[0] == null) return;
		Map<String, Double> localScore_1D;
		Map<String, Integer> localVisits_1D;
		Map<String, Double> localScore_2D;
		Map<String, Integer> localVisits_2D;
		
		for (int numActionIndex = 0; numActionIndex < current.length; numActionIndex++) {
			Object action = current[numActionIndex];
			String keyBandit_1D = action.toString();
			
			localScore_1D = score_1D.get(numActionIndex);
			localVisits_1D = visits_1D.get(numActionIndex);
																					// 1 Dimension Bandit
			if (localScore_1D.containsKey(keyBandit_1D)) {
				
				localScore_1D.put(keyBandit_1D, (((localScore_1D.get(keyBandit_1D)*localVisits_1D.get(keyBandit_1D)+value)/(localVisits_1D.get(keyBandit_1D)+1))));
				localVisits_1D.put(keyBandit_1D, localVisits_1D.get(keyBandit_1D)+1);
			}else {
				localScore_1D.put(keyBandit_1D, value);
				localVisits_1D.put(keyBandit_1D, 1);
			}
			average_1D.put(numActionIndex,(average_1D.get(numActionIndex)*quantity_1D.get(numActionIndex)+value)/(quantity_1D.get(numActionIndex)+1));
			quantity_1D.put(numActionIndex, quantity_1D.get(numActionIndex)+1);
			
																					// 2 Dimensions Bandit
			for (int numAction2Index = numActionIndex+1; numAction2Index < current.length; numAction2Index ++) {
				int keyBandit_2D = numActionIndex*numberOfActions+ numAction2Index ;
				String keyActions_2D = action.toString()+ current[numAction2Index ].toString();
				
				localScore_2D = score_2D.get(keyBandit_2D);
				localVisits_2D = visits_2D.get(keyBandit_2D);
				
				if(localVisits_2D.containsKey(keyActions_2D)) {
					localScore_2D.put(keyActions_2D, (localScore_2D.get(keyActions_2D)*localVisits_2D.get(keyActions_2D)+value)/(localVisits_2D.get(keyActions_2D)+1));
					localVisits_2D.put(keyActions_2D, localVisits_2D.get(keyActions_2D)+1);
					
				}else {
					localScore_2D.put(keyActions_2D, value);
					localVisits_2D.put(keyActions_2D, 1);
				}
				average_2D.put(keyBandit_2D, (average_2D.get(keyBandit_2D)*quantity_2D.get(keyBandit_2D)+value)/(quantity_2D.get(keyBandit_2D)+1));
				quantity_2D.put(keyBandit_2D, quantity_2D.get(keyBandit_2D)+1);
			}
		}
																					// 5 Dimensions Bandit
		String keyActions_5D = "";
		for (Object aux: current) {
			keyActions_5D = keyActions_5D + aux.toString();
		}
		
		stringToActionsTranslator.put(keyActions_5D, current);

		if (visits_5D.containsKey(keyActions_5D)) {
			score_5D.put(keyActions_5D, (score_5D.get(keyActions_5D)*visits_5D.get(keyActions_5D)+value)/(visits_5D.get(keyActions_5D)+1));
			visits_5D.put(keyActions_5D, visits_5D.get(keyActions_5D)+1);
		}else {
			score_5D.put(keyActions_5D, value);
			visits_5D.put(keyActions_5D, 1);
		}
		average_5D = (average_5D*quantity_5D+value)/(quantity_5D+1);
		quantity_5D = quantity_5D+1;
		
	}
	
	public void combineLModel(LModel lModel) {
		for(int i = 0; i < numberOfActions; i++) {
			combineSet(lModel.score_1D.get(i), lModel.visits_1D.get(i), score_1D.get(i), visits_1D.get(i));
			if((quantity_1D.get(i)+lModel.quantity_1D.get(i))>0)
				average_1D.put(i, (average_1D.get(i)*quantity_1D.get(i)+lModel.average_1D.get(i)*lModel.quantity_1D.get(i))/(quantity_1D.get(i)+lModel.quantity_1D.get(i)));
			else
				average_1D.put(i, 0.0);
			quantity_1D.put(i, quantity_1D.get(i)+lModel.quantity_1D.get(i));
			for(int j = i+1; j < numberOfActions; j++) {
				int par = i*numberOfActions+ j;
				combineSet(lModel.score_2D.get(par), lModel.visits_2D.get(par), score_2D.get(par), visits_2D.get(par));
				
				if((quantity_2D.get(par)+lModel.quantity_2D.get(par))>0)
					average_2D.put(par, (average_2D.get(par)*quantity_2D.get(par)+lModel.average_2D.get(par)*lModel.quantity_2D.get(par))/(quantity_2D.get(par)+lModel.quantity_2D.get(par)));
				else
					average_2D.put(par, 0.0);
				quantity_2D.put(par, quantity_2D.get(par)+lModel.quantity_2D.get(par));
			}
		}
		combineSet(lModel.score_5D, lModel.visits_5D, score_5D, visits_5D);

		if((quantity_5D+lModel.quantity_5D)>0)
			average_5D = (average_5D*quantity_5D+lModel.average_5D*lModel.quantity_5D)/(quantity_5D+lModel.quantity_5D);
		else
			average_5D = 0.0;
		quantity_5D = quantity_5D+lModel.quantity_5D;
		stringToActionsTranslator.putAll(lModel.stringToActionsTranslator);
	}
	
	public double eval(Object[] actions) {
		return eval(actions, true);
	}
	
	public double eval(Object[] actions, boolean explore) {
		double acm = 0;
		Random r = new Random();
		//1D
		int bandidoNum = 0;
		for(bandidoNum = 0; bandidoNum<actions.length; bandidoNum++) {
		//for (Object action: actions) {
			if(actions[bandidoNum]==null) return -1000000000;
			String key = actions[bandidoNum].toString();
			if (score_1D.get(bandidoNum).containsKey(key))
				acm += score_1D.get(bandidoNum).get(key)+(explore?constante*Math.sqrt(Math.log(quantity_1D.get(bandidoNum))/visits_1D.get(bandidoNum).get(key)):0);
			else
				acm += explore?(10000000+r.nextDouble()):average_1D.get(bandidoNum); //Big number
			bandidoNum++;
		}
		//2D
		for (int i = 0; i<actions.length-1; i++) {
			for (int j = i+1; j<actions.length; j++) {
				int par = i*numberOfActions+ j;
				String pair = actions[i].toString()+ actions[j].toString();
				Map<String, Double> pareja = score_2D.get(par);
				if (pareja.containsKey(pair)) acm += pareja.get(pair)+(explore?constante*Math.sqrt(Math.log(quantity_2D.get(par))/visits_2D.get(par).get(pair)):0);
				else acm += explore?(10000000+r.nextDouble()):average_2D.get(par); //Big number
			}
		}
		//5D
		String cadena = "";
		for (Object aux2: actions) {
			cadena = cadena + aux2.toString();
		}
		
		if (score_5D.containsKey(cadena)) acm += score_5D.get(cadena)+(explore?constante*Math.sqrt(Math.log(quantity_5D)/visits_5D.get(cadena)):0);
		else acm += explore?(10000000+r.nextDouble()):average_5D; //Big number

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

	
	public Set<Object[]> getBestActionsSet() {
		Set<Object[]> devolver = new HashSet<Object[]>();
		for(String key : score_5D.keySet()) {
			//Auxiliares.imprime(diccionario.keySet());
			devolver.add(stringToActionsTranslator.get(key));
		}

		return devolver;
	}
	
	
	public LModel copy() {
		// Aqui hay un fallo
		LModel lmodel = new LModel(numberOfActions);
		lmodel.combineLModel(this);
		return lmodel;
	}
}
