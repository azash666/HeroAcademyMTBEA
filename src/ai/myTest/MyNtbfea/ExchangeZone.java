package ai.myTest.MyNtbfea;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import action.Action;
import action.EndTurnAction;
import ai.evaluation.IStateEvaluator;
import game.GameState;
//import util.Auxiliares;

class ExchangeZone{
	private LModel lModel;
	private GameState state;
	private List<Action[]> parents;

	//private IStateEvaluator evaluator;
	
	public ExchangeZone(GameState state, IStateEvaluator evaluator) {
		parents = new LinkedList<Action[]>();
		int numberOfActions = state.turn==1?3:5;
		Action[] empty;
		for(int i=0; i<Ntbfea.numParents; i++) {
			empty = new Action[numberOfActions];
			for(int j=0; j<numberOfActions; j++) {
				empty[j] = new EndTurnAction();
			}
			parents.add(empty);
		}
		this.lModel = new LModel(5);
		this.state = new GameState(state.map);
		this.state.imitate(state);
	}

	
	/*******************************************************/
	/**   It puts a list of new parents in the list of    **/
	/**   current parents and remains the best of them    **/
	/*******************************************************/
	/**          NOTE: In case of using threads,          **/
	/**           uncomment the "synchronized"            **/
	/*******************************************************/

	public /*synchronized*/ ExchangeZone putParents(List<Action[]> newParents) {
		double worstParentScore = this.lModel.eval(this.parents.get(Ntbfea.numParents-1));
		for(Action[] newParent : newParents) {
			double newParentScore = this.lModel.eval(newParent);
			if(newParentScore>worstParentScore) {
				this.parents.remove(Ntbfea.numParents-1);
				ListIterator<Action[]> parentsIterator = this.parents.listIterator();
				int index = 0;
				//Sorting the parents
				while(parentsIterator.hasNext()) {
					Action[] parentActions = parentsIterator.next();
					double parentScore = this.lModel.eval(parentActions);
					if(parentScore>newParentScore) index++;
					else break;
				}if(!listContainsArray(this.parents, newParent)) {
					this.parents.add(index, newParent);
					worstParentScore = this.lModel.eval(this.parents.get(Ntbfea.numParents-1));
				}
			}
		}
		return this;
	}
	
	
	/*******************************************************/
	/**              It compares two arrays:              **/
	/**        true if their contents are equals,         **/
	/**                  false otherwise                  **/
	/*******************************************************/
	
	private boolean arrayComarison(Action[] array1, Action[] array2) {
		if(array1.length != array2.length) return false;
		for(int i=0; i<array1.length; i++) {
			if(array1[i]==null || array2[i]==null) return false;
			if(!array1[i].equals(array2[i])) return false;
		}
		return true;
	}
	
	
	/*******************************************************/
	/**            It says if a list of arrays            **/
	/**            contains an specific array             **/
	/*******************************************************/
	
	private boolean listContainsArray(List<Action[]> listOfArrays, Action[] array) {
		for(Action[] elementOfTheList: listOfArrays) {
			if(arrayComarison(elementOfTheList, array)) return true;
		}
		return false;
	}

	
	/*******************************************************/
	/**     It returns the current parents in a List      **/
	/*******************************************************/
	/**          NOTE: In case of using threads,          **/
	/**           uncomment the "synchronized"            **/
	/*******************************************************/

	public /*synchronized*/ List<Action[]> getParents() {
		List<Action[]> newListOfParents = new LinkedList<Action[]>();
		newListOfParents.addAll(parents);
		return newListOfParents;
	}

	
	/*******************************************************/
	/**           It returns the current lModel           **/
	/*******************************************************/
	/**          NOTE: In case of using threads,          **/
	/**           uncomment the "synchronized"            **/
	/*******************************************************/

	public /*synchronized*/ LModel getLModel() {
		return lModel.copy();
	}

	
	/*******************************************************/
	/** It combines the current lModel with another one.  **/
	/*******************************************************/
	/**          NOTE: In case of using threads,          **/
	/**           uncomment the "synchronized"            **/
	/*******************************************************/

	public /*synchronized*/ void update(LModel lModel) {
		this.lModel.combineLModel(lModel);
	}

	
	/*******************************************************/
	/** It returns the best parent of the current lModel  **/
	/*******************************************************/
	/**          NOTE: In case of using threads,          **/
	/**           uncomment the "synchronized"            **/
	/*******************************************************/

	public /*synchronized*/ Action[] getBest() {
		
		Set<Object[]> allParents = lModel.getBestActionsSet();
		
		boolean bestUnset = true;
		Action[] bestActions = null;
		double bestScore = 0;
		for (Object[] uncastedActions : allParents) {
			Action[] currentActions = (Action[]) uncastedActions;
			if(bestUnset) {
				bestUnset=false;
				bestActions = currentActions;
				bestScore = lModel.eval(bestActions, false);
			}else {
				double currentScore = lModel.eval(currentActions, false);
				if(currentScore>bestScore) {
					bestActions = currentActions;
					bestScore = currentScore;
				}
			}
		}
		GameState copia = state.copy();
		for(Action i: bestActions) copia.update(i);
		return bestActions;
	}

	
	/*******************************************************/
	/**             It returns current state              **/
	/*******************************************************/
	/**          NOTE: In case of using threads,          **/
	/**           uncomment the "synchronized"            **/
	/*******************************************************/

	public /*synchronized*/ GameState getState() {
		return state;
	}
	
}
