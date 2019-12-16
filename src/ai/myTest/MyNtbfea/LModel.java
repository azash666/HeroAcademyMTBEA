package ai.myTest.MyNtbfea;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class LModel {
	public Map<Integer, Map<Integer, Double>> score_1D;
	public Map<Integer, Map<Integer, Integer>> visits_1D;
	public Map<Integer, Double> average_1D;
	public Map<Integer, Integer> quantity_1D;

	public Map<Integer, Map<Integer, Double>> score_2D;
	public Map<Integer, Map<Integer, Integer>> visits_2D;
	public Map<Integer, Double> average_2D;
	public Map<Integer, Integer> quantity_2D;

	public Map<Integer, Double> score_5D;
	public Map<Integer, Integer> visits_5D;
	public double average_5D;
	public int quantity_5D;
	//public int total;

	public int numberOfActions;

	public double constant;
	public Map<Integer, Object[]> stringToActionsTranslator;

	public LModel(int numberOfActions) {

		score_1D = new HashMap<Integer, Map<Integer, Double>>();
		visits_1D = new HashMap<Integer, Map<Integer,Integer>>();
		average_1D = new HashMap<Integer, Double>();
		quantity_1D = new HashMap<Integer, Integer>();

		score_2D = new HashMap<Integer, Map<Integer,Double>>();
		visits_2D = new HashMap<Integer, Map<Integer,Integer>>();
		average_2D = new HashMap<Integer, Double>();
		quantity_2D = new HashMap<Integer, Integer>();

		score_5D = new HashMap<Integer, Double>();
		visits_5D = new HashMap<Integer, Integer>();
		average_5D = 0.0;
		quantity_5D = 0;

		stringToActionsTranslator = new HashMap<Integer, Object[]>();
		constant = 1 / Math.sqrt(2);

		this.numberOfActions = numberOfActions;
		for(int numActionIndex = 0; numActionIndex < numberOfActions; numActionIndex++) {
			score_1D.put(numActionIndex, new HashMap<Integer, Double>());
			visits_1D.put(numActionIndex, new HashMap<Integer,Integer>());
			average_1D.put(numActionIndex, 0.0);
			quantity_1D.put(numActionIndex, 0);
			for(int numAction2Index = numActionIndex+1; numAction2Index < numberOfActions; numAction2Index++) {
				int key_2D = numActionIndex*numberOfActions+ numAction2Index;
				score_2D.put(key_2D, new HashMap<Integer, Double>());
				visits_2D.put(key_2D, new HashMap<Integer,Integer>());
				average_2D.put(key_2D, 0.0);
				quantity_2D.put(key_2D, 0);
			}
		}
	}

	
	/*******************************************************/
	/**      It adds a new individual to this lModel      **/
	/*******************************************************/
	
	public void addToLModel(Object[] newActions, double newScore) {
		if(newActions == null) return;
		for(Object action : newActions) if(action==null) return;

		Map<Integer, Double> localScore_1D;
		Map<Integer, Integer> localVisits_1D;
		Map<Integer, Double> localScore_2D;
		Map<Integer, Integer> localVisits_2D;

		for (int numActionIndex = 0; numActionIndex < newActions.length; numActionIndex++) {
			Object action = newActions[numActionIndex];
			Integer keyBandit_1D = action.hashCode();

			localScore_1D = score_1D.get(numActionIndex);
			localVisits_1D = visits_1D.get(numActionIndex);
			// 1 Dimension Bandit
			if (localScore_1D.containsKey(keyBandit_1D)) {

				localScore_1D.put(keyBandit_1D, (((localScore_1D.get(keyBandit_1D)*localVisits_1D.get(keyBandit_1D)+newScore)/(localVisits_1D.get(keyBandit_1D)+1))));
				localVisits_1D.put(keyBandit_1D, localVisits_1D.get(keyBandit_1D)+1);
			}else {
				localScore_1D.put(keyBandit_1D, newScore);
				localVisits_1D.put(keyBandit_1D, 1);
			}
			average_1D.put(numActionIndex,(average_1D.get(numActionIndex)*quantity_1D.get(numActionIndex)+newScore)/(quantity_1D.get(numActionIndex)+1));
			quantity_1D.put(numActionIndex, quantity_1D.get(numActionIndex)+1);

			// 2 Dimensions Bandit
			for (int numAction2Index = numActionIndex+1; numAction2Index < newActions.length; numAction2Index ++) {
				int keyBandit_2D = numActionIndex*numberOfActions+ numAction2Index ;
				LinkedList<Integer> keyList = new LinkedList<Integer>();
				keyList.add(action.hashCode());
				keyList.add(newActions[numAction2Index ].hashCode());
				Integer keyActions_2D = keyList.hashCode();

				localScore_2D = score_2D.get(keyBandit_2D);
				localVisits_2D = visits_2D.get(keyBandit_2D);

				if(localVisits_2D.containsKey(keyActions_2D)) {
					localScore_2D.put(keyActions_2D, (localScore_2D.get(keyActions_2D)*localVisits_2D.get(keyActions_2D)+newScore)/(localVisits_2D.get(keyActions_2D)+1));
					localVisits_2D.put(keyActions_2D, localVisits_2D.get(keyActions_2D)+1);

				}else {
					localScore_2D.put(keyActions_2D, newScore);
					localVisits_2D.put(keyActions_2D, 1);
				}
				average_2D.put(keyBandit_2D, (average_2D.get(keyBandit_2D)*quantity_2D.get(keyBandit_2D)+newScore)/(quantity_2D.get(keyBandit_2D)+1));
				quantity_2D.put(keyBandit_2D, quantity_2D.get(keyBandit_2D)+1);
			}
		}

		LinkedList<Integer> keyList = new LinkedList<Integer>();
		for (Object action: newActions) {
			keyList.add(action.hashCode());
		}
		Integer keyActions_5D = keyList.hashCode();

		stringToActionsTranslator.put(keyActions_5D, newActions);

		if (visits_5D.containsKey(keyActions_5D)) {
			score_5D.put(keyActions_5D, (score_5D.get(keyActions_5D)*visits_5D.get(keyActions_5D)+newScore)/(visits_5D.get(keyActions_5D)+1));
			visits_5D.put(keyActions_5D, visits_5D.get(keyActions_5D)+1);
		}else {
			score_5D.put(keyActions_5D, newScore);
			visits_5D.put(keyActions_5D, 1);
		}
		average_5D = (average_5D*quantity_5D + newScore)/(quantity_5D+1);
		quantity_5D = quantity_5D+1;

	}
	

	/*******************************************************/
	/**        It add other lModel to this lModel         **/
	/*******************************************************/
	
	public void combineLModel(LModel lModel) {
		for(int numActionIndex = 0; numActionIndex < numberOfActions; numActionIndex++) {
			combineSet(lModel.score_1D.get(numActionIndex), lModel.visits_1D.get(numActionIndex), score_1D.get(numActionIndex), visits_1D.get(numActionIndex));
			if((quantity_1D.get(numActionIndex)+lModel.quantity_1D.get(numActionIndex))>0)
				average_1D.put(numActionIndex, (average_1D.get(numActionIndex)*quantity_1D.get(numActionIndex)+lModel.average_1D.get(numActionIndex)*lModel.quantity_1D.get(numActionIndex))/(quantity_1D.get(numActionIndex)+lModel.quantity_1D.get(numActionIndex)));
			else
				average_1D.put(numActionIndex, 0.0);
			quantity_1D.put(numActionIndex, quantity_1D.get(numActionIndex)+lModel.quantity_1D.get(numActionIndex));
			for(int numAction2Index = numActionIndex+1; numAction2Index < numberOfActions; numAction2Index++) {
				int keyBandit_2D = numActionIndex*numberOfActions+ numAction2Index;
				combineSet(lModel.score_2D.get(keyBandit_2D), lModel.visits_2D.get(keyBandit_2D), score_2D.get(keyBandit_2D), visits_2D.get(keyBandit_2D));

				if((quantity_2D.get(keyBandit_2D)+lModel.quantity_2D.get(keyBandit_2D))>0)
					average_2D.put(keyBandit_2D, (average_2D.get(keyBandit_2D)*quantity_2D.get(keyBandit_2D)+lModel.average_2D.get(keyBandit_2D)*lModel.quantity_2D.get(keyBandit_2D))/(quantity_2D.get(keyBandit_2D)+lModel.quantity_2D.get(keyBandit_2D)));
				else
					average_2D.put(keyBandit_2D, 0.0);
				quantity_2D.put(keyBandit_2D, quantity_2D.get(keyBandit_2D)+lModel.quantity_2D.get(keyBandit_2D));
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
	

	/*******************************************************/
	/**   It evaluates a full action using this lModel.   **/
	/**       Explore new combinations if possible.       **/
	/*******************************************************/
	
	public double eval(Object[] actionsToEval) {
		return eval(actionsToEval, true);
	}

	
	/*******************************************************/
	/**   It evaluates a full action using this lModel.   **/
	/**   explore == true -> Explore new combinations.    **/
	/**  explore == false -> Evaluate without exploring.  **/
	/*******************************************************/
	
	public double eval(Object[] actionsToEval, boolean explore) {
		double currentScore = 0;
		Random r = new Random();
		//1D
		for(int numActionIndex = 0; numActionIndex<actionsToEval.length; numActionIndex++) {
			if(actionsToEval[numActionIndex]==null) return -1000000000;
			Integer keyBandit_1D = actionsToEval[numActionIndex].hashCode();
			if (score_1D.get(numActionIndex).containsKey(keyBandit_1D))
				currentScore += score_1D.get(numActionIndex).get(keyBandit_1D)+(explore?constant*Math.sqrt(Math.log(quantity_1D.get(numActionIndex))/visits_1D.get(numActionIndex).get(keyBandit_1D)):0);
			else
				currentScore += explore?(10000000+r.nextDouble()):average_1D.get(numActionIndex); //Big number
				numActionIndex++;
		//2D
			for (int numAction2Index = numActionIndex+1; numAction2Index<actionsToEval.length; numAction2Index++) {
				int keyBandit_2D = numActionIndex*numberOfActions+ numAction2Index;

				LinkedList<Integer> keyList = new LinkedList<Integer>();
				if(actionsToEval[numActionIndex]==null || actionsToEval[numAction2Index]==null) break;
				keyList.add(actionsToEval[numActionIndex].hashCode());
				keyList.add(actionsToEval[numAction2Index].hashCode());
				Integer keyActions_2D = keyList.hashCode();

				if (score_2D.get(keyBandit_2D).containsKey(keyActions_2D)) currentScore += score_2D.get(keyBandit_2D).get(keyActions_2D)+(explore?constant*Math.sqrt(Math.log(quantity_2D.get(keyBandit_2D))/visits_2D.get(keyBandit_2D).get(keyActions_2D)):0);
				else currentScore += explore?(10000000+r.nextDouble()):average_2D.get(keyBandit_2D); //Big number
			}
		}
		//5D
		LinkedList<Integer> keyList = new LinkedList<Integer>();
		for (Object action: actionsToEval) {
			keyList.add(action.hashCode());
		}
		Integer keyActions_5D = keyList.hashCode();

		if (score_5D.containsKey(keyActions_5D)) currentScore += score_5D.get(keyActions_5D)+(explore?constant*Math.sqrt(Math.log(quantity_5D)/visits_5D.get(keyActions_5D)):0);
		else currentScore += explore?(10000000+r.nextDouble()):average_5D; //Big number

		return 2*currentScore/(actionsToEval.length + (actionsToEval.length*(actionsToEval.length-1))/2+1);
	}


	/*******************************************************/
	/** It returns a set with the full actions introduced **/
	/*******************************************************/
	
	public Set<Object[]> getBestActionsSet() {
		Set<Object[]> setWithFullActions_toReturn = new HashSet<Object[]>();
		for(Integer key : score_5D.keySet()) {
			setWithFullActions_toReturn.add(stringToActionsTranslator.get(key));
		}
		return setWithFullActions_toReturn;
	}


	/*******************************************************/
	/**       It returns a copy of the this lModel        **/
	/*******************************************************/
	
	public LModel copy() {
		LModel lmodel = new LModel(numberOfActions);
		lmodel.combineLModel(this);
		return lmodel;
	}
}
