package ai.myTest.mutators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import action.Action;
import ai.myTest.MyNtbfea.Ntbfea;
import ai.util.ActionPruner;
import game.GameState;


public class NaturalMutator {
	private List<Action[]> parents;
	private Set<Action[]> sons;
	private double mutationProbability = 0;
	private int numOffspring;
	private long almostFullTime = Ntbfea.almostFullTime;
	private HashMap<Long, List<Action>> legalActionsMap;
	private GameState state;
	private ActionPruner pruner;

	
	public NaturalMutator putParents(List<Action[]> parents) {
		this.parents = parents;
		return this;
	}


	public NaturalMutator putValidator(GameState state, HashMap<Long, List<Action>> legalActions) {
		this.legalActionsMap = legalActions;
		this.pruner = new ActionPruner();
		this.state = new GameState(state.map);
		this.state.imitate(state);
		return this;
	}

	
	public NaturalMutator putMutationProbability(double prob) {
		this.mutationProbability = prob;
		return this;
	}

	
	public NaturalMutator putNumOffspring(int numOffspring) {
		this.numOffspring = numOffspring;
		return this;
	}


	/*******************************************************/
	/**         It crossover and mutate in order          **/
	/**         to build the sons and return them         **/
	/*******************************************************/
	
	public Set<Action[]> getSons() {
		Random r = new Random();
		sons = new HashSet<Action[]>();
		sons.addAll(parents);
		GameState stateCopy = new GameState(state.map);
		int numberOfActions = state.turn==1?3:5;
		int numberOfParents = parents.size();
		while(true) {
			if(sons.size()>=numOffspring) break;
			int i = r.nextInt(numberOfParents);
			int j = r.nextInt(numberOfParents);
			while(i==j) j = r.nextInt(numberOfParents);	//In order to avoid to crossover the parent with itself. 
			
			Action[] parentA = parents.get(i);
			Action[] parentB = parents.get(j);
			
			if(System.currentTimeMillis() >= almostFullTime) break;
			
			Action[] newSon = new Action[numberOfActions];
			stateCopy.imitate(state);
			int mutateIndex=-1;
			double p = r.nextDouble();
			if (p<mutationProbability) {
				mutateIndex=r.nextInt(numberOfActions);
			}
			for(int index=0; index<numberOfActions; index++) {
				if(System.currentTimeMillis() >= almostFullTime) break;

														//Searching for legal actions
				List<Action> legalActions = new LinkedList<Action>();
				long key = stateCopy.hash();
				if(legalActionsMap.containsKey(key)) {
					legalActions = legalActionsMap.get(key);
				}
				else {
					stateCopy.possibleActions(legalActions); 
					pruner.prune(legalActions, stateCopy);
					legalActionsMap.put(key, legalActions);
				}
				if(legalActions.size()==0) break;
				
				if(r.nextBoolean() && mutateIndex!=index) {		//Genome of son = genome of parent B
					newSon[index] = parentB[index];

				}else if(mutateIndex!=index){					//Genome of son = genome of parent A
					newSon[index] = parentA[index];
				}else {				//Genome of son = random genome.
					newSon[index] = legalActions.get(r.nextInt(legalActions.size()));
				}
				
				
				if(index>0 && !legalActions.contains(newSon[index])) {			//If genome is not valid
					if(index<numberOfActions-1) 
						if(legalActions.contains(parentA[index+1])) newSon[index] = parentA[index+1];
						else if(legalActions.contains(parentB[index+1])) newSon[index] = parentB[index+1];
						else newSon[index] = legalActions.get(r.nextInt(legalActions.size()));
					else
						newSon[index] = legalActions.get(r.nextInt(legalActions.size()));
				}

				stateCopy.update(newSon[index]);
			}
			sons.add(newSon);
		}
		return sons;
	}

}
