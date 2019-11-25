package ai.myTest;

import action.Action;
import ai.AI;
import ai.evaluation.IStateEvaluator;
import ai.myTest.MyNtbfea.Ntbfea;
import game.GameState;
//import util.Auxiliares;

public class MyNtbfeaAgent implements AI {
	private int currentTurn, subActionNumber;
	private Action[] fullActions;
	
	private Ntbfea ntbfeaClass;
	private IStateEvaluator evaluatorClass;
	private int nbEvals;
	private int numOfSons;
	private int numOfParents;
	
	public MyNtbfeaAgent(IStateEvaluator evaluator, int nbEvals, int numParents, int numOfSons) {
		this.nbEvals = nbEvals;
		this.numOfSons = numOfSons;
		this.evaluatorClass = evaluator;
		this.numOfParents = numParents;
		currentTurn = -1;
	}
	/*
	 * 0 - Soldier
	 * 1 - Archer
	 * 2 - Healer
	 * 3 - Mage
	 * 4 - Ninja
	 * 5 - Fire
	 * 6 - Potion
	 * 7 - Sword
	 * 8 - Scroll
	 * 9 - Shield
	 * 10- Helmet
	 * 11- ¿Crystal?
	 */
	
	@Override
	public Action act(GameState state, long ms) {
		doOnce(state);
	    if(subActionNumber < fullActions.length) return fullActions[subActionNumber++];
	    else return null;
	}

	// This function is executed once per turn.
	private void doOnce(GameState state) {
		if(currentTurn == state.turn) return;
		currentTurn = state.turn;
		subActionNumber = 0;
		fullActions = new Action[5];
		ntbfeaClass = new Ntbfea(evaluatorClass, this.nbEvals, this.numOfParents, this.numOfSons);
		GameState stateCopy = state.copy();
    	
    	fullActions = ntbfeaClass.searchBestCombination(stateCopy);
    	
		
	}
	@Override
	public void init(GameState state, long ms) {
		// TODO Auto-generated method stub
	}

	@Override
	public AI copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String header() {
		// TODO Auto-generated method stub
		return "header";
	}

	@Override
	public String title() {
		// TODO Auto-generated method stub
		return "My NTBFEA Agent";
	}
	
}
