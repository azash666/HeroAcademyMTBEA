package ai.myTest.MyNtbfea;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import action.Action;
import action.EndTurnAction;
import ai.evaluation.IStateEvaluator;
import ai.myTest.mutators.NaturalMutator;
import ai.util.ActionPruner;
import game.GameState;
//import util.Auxiliares;

public class Ntbfea {
	
	public Random rand;
	public static long evaluatorTime;
	public static long evolutionTime;
	public static long almostFullTime;
	public static AtomicInteger iterations;
	public static int numParents = 5;
	public static int numOffsprings = 50;
	private int budget;

	private double evaluatorTimeWeight = 1;
	private double evolutionTimeWeight = .97;
	private double almostFullTimeWeight = .999;


	public int NUM_THREADS = 1;

	private IStateEvaluator evaluator;
	
	public Ntbfea(IStateEvaluator evaluator, int budget, int numParents, int numNeighbors) {
		this.budget = budget;
		Ntbfea.evaluatorTime = (long )((double)budget*evaluatorTimeWeight)+System.currentTimeMillis();
		Ntbfea.evolutionTime = (long)((double)budget*evolutionTimeWeight)+System.currentTimeMillis();
		Ntbfea.almostFullTime = (long)((double)budget*almostFullTimeWeight)+System.currentTimeMillis();
		this.evaluator = evaluator;
		Ntbfea.numParents = numParents;
		Ntbfea.numOffsprings = numNeighbors;
		rand = new Random();
		Ntbfea.iterations = new AtomicInteger(0);
	}
	
	public Ntbfea setWeights(double evaluatorTimeWeight, double evolutionTimeWeight, double almostFullTimeWeight) {

		this.evaluatorTimeWeight = evaluatorTimeWeight;
		this.evolutionTimeWeight = evolutionTimeWeight;
		this.almostFullTimeWeight = almostFullTimeWeight;
		Ntbfea.evaluatorTime = (long )((double)budget*evaluatorTimeWeight)+System.currentTimeMillis();
		Ntbfea.evolutionTime = (long)((double)budget*evolutionTimeWeight)+System.currentTimeMillis();
		Ntbfea.almostFullTime = (long)((double)budget*almostFullTimeWeight)+System.currentTimeMillis();
		return this;
	}

	
	/*******************************************************/
	/**    It returns the best action found to a turn     **/
	/*******************************************************/

	public Action[] searchBestCombination(GameState state) {
		//long a = System.currentTimeMillis();				//Used to measure time used searching the best action
		List<Action[]> parents = new LinkedList<Action[]>();
		
		//First parent = Greedy
		parents.add(buildGreedy(state));
		
		//Other parents = Random
	    buildRandomParents(state, 1, 50, parents);
		
		ExchangeZone z = new ExchangeZone(state).putParents(parents);
		if(NUM_THREADS >1) {	
			Thread[] hilos = new MyThread[NUM_THREADS];
			for(int i=0; i<NUM_THREADS; i++) {
				hilos[i] = new MyThread(i, z, state, evaluator);
				hilos[i].start();
			}
			for(int i=0; i<NUM_THREADS; i++) {
				try {
					hilos[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		else {
			Thread h = new MyThread(0, z, state, evaluator);
			h.run();
		}

		Action[] actionsTurn = z.getBest();
		//long c = System.currentTimeMillis();				//Used to measure time used searching the best action

		//System.out.println("Ntbfea --> tiempo: "+(c-a)+"  iteraciones: "+Ntbfea.iterations.get());	//Used to show iterations and time
		
		return actionsTurn;
	}


	/*******************************************************/
	/**      It fills parents from initial to last-1      **/
	/**                with random actions                **/
	/*******************************************************/
	
	private void buildRandomParents(GameState state, int initial, int last, List<Action[]> parents) {
	    int numberOfActions = state.turn==1?3:5;
		Action[] actionsTurn;
		List<Action> possibleActions = new LinkedList<Action>();
	    ActionPruner pruner = new ActionPruner();
	    GameState copy = new GameState(state.map);
		for(int i=initial; i<last; i++) {
			copy.imitate(state);
			actionsTurn = new Action[numberOfActions];
			for (int j=0; j<numberOfActions; j++) {
				
				copy.possibleActions(possibleActions);
				pruner.prune(possibleActions, copy);
				if(possibleActions.size()==0) actionsTurn[j] = new EndTurnAction();
				else actionsTurn[j] = possibleActions.get(rand.nextInt(possibleActions.size()));
				copy.update(actionsTurn[j]);
			}
			if(!listContainsArray(parents, actionsTurn)) parents.add(actionsTurn);
			else i--;
		}
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


	/*******************************************************/
	/**           It returns the greedy action            **/
	/*******************************************************/
	
	private Action[] buildGreedy(GameState state) {
	    int numberOfActions = state.turn==1?3:5;
	    List<Action> possibleActions = new LinkedList<Action>();
		Action[] actions = new Action[numberOfActions];
	    GameState copy = new GameState(state.map); //In order to be in a mini-turn.
		GameState copy2 = new GameState(state.map); //In order to be a mini-turn forward to evaluate the action.
	    ActionPruner pruner = new ActionPruner();
		copy.imitate(state);
		for (int i=0; i<numberOfActions; i++) {
			copy.possibleActions(possibleActions);
			pruner.prune(possibleActions, copy);
			if(possibleActions.size()==0) actions[i] = new EndTurnAction();
			else {
				double score = -999999;
				for(Action action: possibleActions) {
					copy2.imitate(copy);
					copy2.update(action);
					double aux = evaluator.eval(copy2, state.p1Turn);
					if(aux>score) {
						actions[i] = action;
						score = aux;
					}
				}
			}
			copy.update(actions[i]);
		}
		return actions;
	}


	/*******************************************************/
	/**        It returns a crossover and mutation        **/
	/**	           of the parents (Offsprings)            **/
	/*******************************************************/

	public static Set<Action[]> neighbors(GameState state, List<Action[]> parents, int numberOfSons, HashMap<Long, List<Action>> legalActions, double mutationProbability) {
		
		NaturalMutator mutator = new NaturalMutator()
				.putParents(parents)
				.putMutationProbability(mutationProbability)
				.putValidator(state, legalActions)
				.putNumOffspring(numberOfSons);
		return mutator.getSons();
	}
}
