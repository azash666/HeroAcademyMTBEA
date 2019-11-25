package ai.myTest.MyNtbfea;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import action.Action;
import action.EndTurnAction;
import game.GameState;
//import util.Auxiliares;

class ExchangeZone{
	private LModel lModel;
	private GameState state;
	private List<Action[]> parents;
	
	public ExchangeZone(GameState state) {
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

	public /*synchronized*/ ExchangeZone putParents(List<Action[]> parents2) {
		double peorPadre = this.lModel.eval(parents.get(Ntbfea.numParents-1));
		for(Action[] a : parents2) {
			double nuevoValor = this.lModel.eval(a);
			
			if(nuevoValor>peorPadre) {
				parents.remove(Ntbfea.numParents-1);
				ListIterator<Action[]> iter = parents.listIterator();
				int ind = 0;
				while(iter.hasNext()) {
					Action[] accion = iter.next();
					double valor = this.lModel.eval(accion);
					if(valor>nuevoValor) ind++;
					else break;
				}if(!listContainsArray(parents, a)) {
					parents.add(ind, a);
					peorPadre = this.lModel.eval(parents.get(Ntbfea.numParents-1));
				}
			}
			
		}
		return this;
	}
	
	private boolean arrayComarison(Action[] array1, Action[] array2) {
		if(array1.length != array2.length) return false;
		for(int i=0; i<array1.length; i++) {
			if(array1[i]==null || array2[i]==null) return false;
			if(!array1[i].equals(array2[i])) return false;
		}
		return true;
	}
	
	private boolean listContainsArray(List<Action[]> listOfArrays, Action[] array) {
		for(Action[] elementOfTheList: listOfArrays) {
			if(arrayComarison(elementOfTheList, array)) return true;
		}
		return false;
	}

	public /*synchronized*/ List<Action[]> getParents() {
		List<Action[]> devolver = new LinkedList<Action[]>();
		devolver.addAll(parents);
		return devolver;
	}


	public /*synchronized*/ LModel getLModel() {
		return lModel.copy();
	}

	public /*synchronized*/ void update(LModel lModel) {
		this.lModel.combineLModel(lModel);
	}

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
			if(Ntbfea.almostFullTime < System.currentTimeMillis()) break;
		}
		return bestActions;
	}

	public /*synchronized*/ GameState getState() {
		return state;
	}
	
}
