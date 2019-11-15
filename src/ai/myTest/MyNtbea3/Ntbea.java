package ai.myTest.MyNtbea3;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import action.Action;
import ai.evaluation.IStateEvaluator;
import ai.myTest.MyGreedy.Greedy;
import ai.myTest.MyNtbea2.LModelTest;
import game.GameState;
import util.Auxiliares;

public class Ntbea {
	
	public Random rand;
	
	private int numNeighbors = 50;
	private long halfTime;
	private long almostFullTime;
	private AtomicInteger iteraciones;


	public final int NUM_THREADS = 4;

	private IStateEvaluator evaluator;
	
	public Ntbea(IStateEvaluator evaluator, int budget, int numNeighbors) {
		this.halfTime=(long)((double)budget*0.65)+System.currentTimeMillis();
		this.almostFullTime=(long)((double)budget*0.999)+System.currentTimeMillis();
		this.numNeighbors = numNeighbors;
		this.evaluator = evaluator;
		rand = new Random();
		this.iteraciones = new AtomicInteger(0);
	}


	@SuppressWarnings("unused")
	public Action[] searchBestCombination(GameState state) {
		long a = System.currentTimeMillis();
	    int cantidad = state.turn==1?3:5;
		Action[] current = new Action[cantidad];
		Action[] greedy = new Action[cantidad];
	    List<Action> actions = new LinkedList<Action>();
	    GameState copia = new GameState(state.map);
	    copia.imitate(state);
		//GameState copia = state.copy();
	    copia.possibleActions(actions);
		for(int i=0; i<cantidad; i++) {
			
			
			copia.possibleActions(actions);
			
		    
		    if(actions.size()>0) current[i] = searchBestAction(copia);
		    copia.update(current[i]);
	    }
		double gred =evaluator.eval(copia, state.p1Turn);
		greedy = current.clone();
		//GameState clone = state.copy();
		//System.out.println(state.turn);
		//copia = new GameState(state.map);
		//for (int j = 0; j<repeticiones; j++) {
		ZonaIntercambio z = new ZonaIntercambio(current, state);
		if(NUM_THREADS >1) {	
			Thread[] hilos = new MiHebra[NUM_THREADS];
			for(int i=0; i<NUM_THREADS; i++) {
				hilos[i] = new MiHebra(i, NUM_THREADS, z, state, current);
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
			MiHebra h = new MiHebra(0, 1, z, state, current);
			h.run();
		}

		long b = System.currentTimeMillis();
		current = z.getBest();
		//Auxiliares.imprime(z.lModel);
		//System.out.println();
		//clone = new GameState(state.map);
	    copia.imitate(state);
		for(int i=0; i<cantidad; i++)
			copia.update(current[i]);
		double d = evaluator.eval(copia, state.p1Turn);
		if(d<gred) current = greedy;
		//throw new RuntimeException();

		long c = System.currentTimeMillis();
		//System.out.println("Ntbea --> tiempo: "+(c-a)+"  iteraciones: "+iteraciones.get());
		return current;
	}

	private Action searchBestAction(GameState copia) {
		Action action = null;
		double score = 0;
		boolean initialized = false;
		List<Action> actions = new LinkedList<Action>();
		
		
			copia.possibleActions(actions);
			
		GameState clone = new GameState(copia.map);
		for (Action i : actions) {
			clone.imitate(copia);
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


	public Set<Action[]> neighbors(GameState state, Action[] current, int numberOfNeighbors, double mutationProbability_A_BORRAR, boolean flipOnce, HashMap<Long, List<Action>> movimientosLegales) {
		
		Set<Action[]> population= new HashSet<Action[]>();
		GameState copia = new GameState(state.map);
		GameState anterior = new GameState(state.map);
		for (int k=0; k<numberOfNeighbors; k++) {
			copia.imitate(state);
			anterior.imitate(state);
			Action[] neighbor = current.clone();
			List<Action> acciones = new LinkedList<Action>();
			int i=flipOnce?rand.nextInt(current.length):0;
			for (int j=0; j<current.length; j++) {
				//*******************************
				//acciones2 = current[j].possibleActions();
				//*******************************
			    //if(acciones.size()<1) break;
				if (j==i) {
					long key = copia.hash();
					if(movimientosLegales.containsKey(key)) acciones = movimientosLegales.get(key);
					else {
						copia.possibleActions(acciones); 
						movimientosLegales.putIfAbsent(key, acciones);
					}
				    if(acciones.size()<1) break;
				    //System.out.println("--------------------------------");
					/*Auxiliares.imprime(acciones);
					copia.possibleActions(acciones);
					Auxiliares.imprime(acciones);*/
				    
					neighbor[j] = acciones.get(rand.nextInt(acciones.size()));
				    //if(aux2.toString().equals("EndTurnAction []")) break;
				    
				}else if(j>i) {
					//System.out.println(copia.equals(anterior));
					long key = copia.hash();
					if(movimientosLegales.containsKey(key)) acciones = movimientosLegales.get(key);
					else {
						copia.possibleActions(acciones);
						movimientosLegales.putIfAbsent(key, acciones);
					}
					//System.out.println(!acciones.contains(neighbor[j]));
					if (!acciones.contains(neighbor[j])) {

					    if(acciones.size()<1) break;
						neighbor[j] = acciones.get(rand.nextInt(acciones.size()));
						/*Auxiliares.imprime(neighbor);
						Auxiliares.imprime(acciones);
						throw new RuntimeException();*/
					}
				}
				copia.update(neighbor[j]);
				
			}
			population.add(neighbor);

		}
		return population;
	}

	
	class MiHebra extends Thread {
		private int miId, numThreads;
		private ZonaIntercambio z;
		private GameState state;
		private Action[] current;
		private double value;
		private boolean salir;
		private HashMap<Long, List<Action>> movimientosLegales;
		private LModel lModel;

		public MiHebra(int miId, int numThreads, ZonaIntercambio z, GameState state, Action[] current) {
			this.miId = miId;
			this.numThreads = numThreads;
			this.z = z;
			this.state= z.getState();
			this.current = current;
			this.lModel = z.getLModel();
			this.movimientosLegales = new HashMap<Long, List<Action>>();
			salir = false;
		}
		
		public void run() {
			GameState clone = new GameState(state.map);
			while(!salir) {
				iteraciones.addAndGet(1);
				salir = System.currentTimeMillis() >= halfTime;
			//for(int t=miId; t<nbEvals; t+=numThreads) {
				int cantidad = state.turn==1?3:5;
				clone.imitate(state);
				for(int i=0; i<cantidad; i++)
					clone.update(current[i]);
				double value =evaluator.eval(clone, state.p1Turn);
				//**add <current,value> to LModel
				this.lModel.addToLModel(current, value);
				
				//**Population<--Neighbors(Model, current, n, p, flipOnce)
				Set<Action[]> population = new HashSet<Action[]>();
			    
				population = neighbors(state, current, numNeighbors, 0.5, true, movimientosLegales);
				//current <-- argmax(Population)
				//Action[] aux = current;
				double valor = value;
				for(Action[] a : population) {
					double nuevoValor = this.lModel.puntua(a);
					if (valor<nuevoValor) {
						valor=nuevoValor;
						current = a;
					}
				}
			}
			z.actualiza(lModel);
		}
		
		
	}
	
	class ZonaIntercambio{
		private Action[] current;
		private LModel lModel;
		private GameState state;
		
		public ZonaIntercambio(Action[] current, GameState state) {
			this.current = current;
			this.lModel = new LModel(5);
			this.state = new GameState(state.map);
			this.state.imitate(state);
			
		}

		public synchronized LModel getLModel() {
			return lModel.copy();
		}

		public synchronized void actualiza(LModel lModel) {
			this.lModel.combineLModel(lModel);
		}

		public synchronized Action[] getBest() {
			HashMap<Long, List<Action>> movimientosLegales = new HashMap<Long, List<Action>>();
			Set<Object[]> todo = lModel.getBestActionsSet();
			Set<Object[]> todo2 = lModel.getBestActionsSet();
			//TODO Cuello de botella temporal
			for (Object[] objetos : todo) {
				Action[] acciones = (Action[]) objetos;
				Set<Action[]> auxiliar = neighbors(state, acciones, numNeighbors, 0.5, true, movimientosLegales);
				
				for (Action[] vecino : auxiliar) {
					todo2.add(vecino);
				}
				if((halfTime*3+almostFullTime)/4 < System.currentTimeMillis()) break;
			}
			
			boolean vacio = true;
			Action[] current = this.current;
			double value = 0;
			
			for (Object[] acciones : todo2) {
				if(vacio) {
					vacio=false;
					current = (Action[]) acciones;
					value = lModel.puntua(current, false);
				}else {
					double aux = lModel.puntua(acciones, false);
					if(aux>value) {
						current = (Action[]) acciones;
						value = aux;
					}
				}
				if(almostFullTime < System.currentTimeMillis()) break;
			}
			return current;
		}

		public synchronized GameState getState() {
			return state;
		}
		
	}
	
}
