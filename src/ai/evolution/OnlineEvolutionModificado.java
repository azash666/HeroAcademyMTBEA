package ai.evolution;

import game.GameState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ui.UI;
import util.Auxiliares;
import action.Action;
import ai.AI;
import ai.evaluation.IStateEvaluator;

public class OnlineEvolutionModificado implements AI, AiVisualizor {

	public int popSize;
	public int budget;
	public double killRate;
	public double mutRate;
	public IStateEvaluator evaluator;
	
	public List<Double> generations;
	public List<Double> bestVisits;

	public List<Action> actions;
	
	public final List<Genome> pop;
	public Map<Integer, Double> fitnesses;
	public List<List<Action>> bestActions;
	
	// Island evolution
	public OnlineEvolutionModificado neighbor;
	public List<Genome> newcomers;
	
	private OnlineEvolutionVisualizor visualizor;
	private final Random random;
	public SharedStateTable table;
	public boolean useHistory;

	private AtomicInteger iteraciones;
	
	public OnlineEvolutionModificado(boolean useHistory, int popSize, double mutRate, double killRate, int budget, IStateEvaluator evaluator) {
		super();
		this.popSize = popSize;
		this.mutRate = mutRate;
		this.budget = budget;
		this.evaluator = evaluator;
		this.killRate = killRate;
		pop = new ArrayList<Genome>();
		actions = new ArrayList<Action>();
		random = new Random();
		this.generations = new ArrayList<Double>();
		this.bestVisits = new ArrayList<Double>();
		this.fitnesses = new HashMap<Integer, Double>();
		this.bestActions = new ArrayList<List<Action>>();
		this.table = new SharedStateTable();
		this.newcomers = new ArrayList<Genome>();
		this.useHistory = useHistory;
		
	}
	
	public void enableVisualization(UI ui){
		this.visualizor = null;//new OnlineEvolutionVisualizor(ui, this);
	}
	
	private Action searchBestAction(GameState copia) {
		Action action = null;
		double score = 0;
		boolean initialized = false;
		List<Action> actions = new LinkedList<Action>();
		copia.possibleActions(actions);
		for (Action i : actions) {
			GameState clone = copia.copy();
			clone.update(i);
			if (!initialized) {
				action = i;
				score = evaluator.eval(clone, copia.p1Turn);
				initialized = true;
			}else {
				double temporalScore = evaluator.eval(clone, copia.p1Turn);
				//System.out.println(temporalScore + " -> "+i);
				if(temporalScore>score) {
					score = temporalScore;
					action = i;
				}
			}
		}
		return action;
	}

	@Override
	public Action act(GameState state, long ms) {

		if (actions.isEmpty()) {
			long a = System.currentTimeMillis();
			search(state);

			long c = System.currentTimeMillis();
			System.out.println("OEP --> tiempo: "+(c-a)+"  iteraciones: "+iteraciones.get());
		}

		table.clear();
		final Action next = actions.get(0);
		actions.remove(0);
		return next;
	}

	public void search(GameState state) {

		Long start = System.currentTimeMillis();
		
		fitnesses.clear();
		bestActions.clear();
		
		setup(state);

		final List<Genome> killed = new ArrayList<Genome>();
		final GameState clone = new GameState(state.map);
		clone.imitate(state);
		
		int g = 0;

		this.iteraciones = new AtomicInteger(0);
		while (System.currentTimeMillis() < start + budget) {

			iteraciones.incrementAndGet();
			g++;
			
			// Test pop
			double val = 0;
			for (final Genome genome : pop) {
				// System.out.print("|");
				clone.imitate(state);
				clone.update(genome.actions);
				val = evaluator.eval(clone, state.p1Turn);
				if (genome.visits == 0 || val < genome.value){
					if (useHistory){
						Long hash = clone.hash();
						val = table.visit(hash, val);
					}
					genome.value = val;
				}
				genome.visits++;
			}

			// Kill worst genomes
			Collections.sort(pop);
			killed.clear();
			final int idx = (int) Math.floor(pop.size() * killRate);
			for (int i = idx; i < pop.size(); i++)
				killed.add(pop.get(i));

			// Crossover new ones
			for (int i = 0; i < killed.size(); i++) {
				final int a = random.nextInt(idx);
				int b = random.nextInt(idx);
				while (b == a)
					b = random.nextInt(idx);

				clone.imitate(state);
				killed.get(i).crossover(pop.get(a), pop.get(b), clone);

				// Mutation
				if (Math.random() < mutRate) {
					clone.imitate(state);
					killed.get(i).mutate(clone);
				}
			}
			//long bb = System.currentTimeMillis();
			
			// add newcomers
			takeinNewcomers();
			
			// TODO: Only if needed?!
			fitnesses.put(g, pop.get(0).fitness());
			bestActions.add(clone(pop.get(0).actions));

			//long cc = System.currentTimeMillis();
			
		}
		//System.out.println("Best Genome: " + pop.get(0).actions);
		//System.out.println("Visits: " + pop.get(0).visits);
		//System.out.println("Value: " + pop.get(0).avgValue());

		if (visualizor != null){
			visualizor.p1 = state.p1Turn;
			try{
				visualizor.update();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			while(visualizor.rendering){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}
		
		actions = pop.get(0).actions;
		generations.add((double)g);
		bestVisits.add((double)(pop.get(0).visits));

	}
	
	public void recieveGenome(Genome g) {
		synchronized (this) {
			newcomers.add(g);
		}		
	}
	
	private void takeinNewcomers() {
		synchronized (this) {
			if (!newcomers.isEmpty()){
				Genome newcomer = newcomers.get(0);
				newcomers.remove(0);
				pop.add(newcomer);
			}
		}
		
		if (neighbor != null && pop.size() > popSize-5){
			neighbor.recieveGenome(pop.get(pop.size()-2));
			pop.remove(pop.get(pop.size()-2));
		}		
	}

	private Map<Integer, Double> clone(Map<Integer, Double> other) {
		Map<Integer, Double> clone = new HashMap<Integer, Double>();
		for (Integer i : other.keySet())
			clone.put(i, other.get(i));
		return clone;
	}
	
	private List<Action> clone(List<Action> other) {
		List<Action> actions = new ArrayList<Action>();
		actions.addAll(other);
		return actions;
	}

	private void setup(GameState state) {

		pop.clear();
		final GameState clone = new GameState(state.map);

		for (int i = 0; i < popSize; i++) {
			clone.imitate(state);
			final Genome genome = new WeakGenome();
			genome.random(clone);
			pop.add(genome);
		}

	}

	@Override
	public void init(GameState state, long ms) {
		actions = new ArrayList<Action>();
	}
	
	@Override
	public String header() {
		String name = title()+"\n";
		name += "Pop. size = " + popSize + "\n";
		name += "Budget = " + budget + "ms.\n";
		name += "Mut. rate = " + mutRate + "\n";
		name += "Kill rate = " + killRate + "\n";
		name += "State evaluator = " + evaluator.title() + "\n";
		name += "History = " + useHistory + "\n";
		return name;
	}


	@Override
	public String title() {
		return "Online Evilution";
	}

	@Override
	public AI copy() {
		if (visualizor!=null){
			OnlineEvolutionModificado evo = new OnlineEvolutionModificado(useHistory, popSize, mutRate, killRate, budget, evaluator.copy());
			return evo;
		}
		
		return new OnlineEvolutionModificado(useHistory, popSize, mutRate, killRate, budget, evaluator.copy());
		
	}

}
