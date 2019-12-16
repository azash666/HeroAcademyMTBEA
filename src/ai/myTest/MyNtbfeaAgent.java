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
	private int budget;
	private int numOfSons;
	private int numOfParents;
	private double evaluatorTime;
	private double evolutionTime;
	private double almostFullTime;
	private boolean actionMapUse = true;
	private String name;
	
	
	public MyNtbfeaAgent(IStateEvaluator evaluator, int budget, int numParents, int numOfSons) {
		this.budget = budget;
		this.numOfSons = numOfSons;
		this.evaluatorClass = evaluator;
		this.numOfParents = numParents;
		currentTurn = -1;
		evaluatorTime = .2;
		evolutionTime = .95;
		almostFullTime = 1;
	}
	
	public MyNtbfeaAgent putEvaluatorTime(double time) {
		this.evaluatorTime = time;
		return this;
	}

	public MyNtbfeaAgent putEvolutionTime(double time) {
		this.evolutionTime = time;
		return this;
	}

	public MyNtbfeaAgent putAlmostFullTime(double time) {
		this.almostFullTime = time;
		return this;
	}
	
	
	// Disable the use of map to store actions. It is quicker, but uses a huge bunch of memory for large budgets.
	
	public MyNtbfeaAgent disableActionMap() {
		this.actionMapUse = false;
		return this;
	}

	public MyNtbfeaAgent putName(String name) {
		this.name = name;
		return this;
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
		ntbfeaClass = new Ntbfea(evaluatorClass, this.budget, this.numOfParents, this.numOfSons).setWeights(evaluatorTime, evolutionTime, almostFullTime)
				.putUseOfActionMap(actionMapUse);
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
		return name;
	}

	
}
