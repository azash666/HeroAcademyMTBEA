package ai.myTest.MyNtbfea;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import action.Action;
import action.EndTurnAction;
import ai.evaluation.IStateEvaluator;
import ai.myTest.mutators.NaturalMutator;
import ai.util.ActionPruner;
import game.GameState;
import util.Auxiliares;

public class Ntbfea {
	
	public Random rand;
	private int numParents = 5;
	private long aBitTime;
	private long halfTime;
	private long almostFullTime;
	private AtomicInteger iteraciones;


	public int NUM_THREADS = 1;

	private IStateEvaluator evaluator;
	private int numNeighbors;
	
	public Ntbfea(IStateEvaluator evaluator, int budget, int numParents, int numNeighbors) {
		this.aBitTime = (long )((double)budget*.35)+System.currentTimeMillis();
		this.halfTime=(long)((double)budget*.85)+System.currentTimeMillis();
		this.almostFullTime=(long)((double)budget*.999)+System.currentTimeMillis();
		this.evaluator = evaluator;
		this.numParents = numParents;
		this.numNeighbors = numNeighbors;
		rand = new Random();
		this.iteraciones = new AtomicInteger(0);
	}


	@SuppressWarnings("unused")
	public Action[] searchBestCombination(GameState state) {
		long a = System.currentTimeMillis();
	    int cantidad = state.turn==1?3:5;
	    List<Action> actions = new LinkedList<Action>();
	    GameState copia = new GameState(state.map);
	    GameState copia2 = new GameState(state.map);
	    ActionPruner pruner = new ActionPruner();
		List<Action[]> parents = new LinkedList<Action[]>();
		Action[] current;
		copia.imitate(state);
		current = new Action[cantidad];
		for (int j=0; j<cantidad; j++) {
			
			copia.possibleActions(actions);
			pruner.prune(actions, copia);
			if(actions.size()==0) current[j] = new EndTurnAction();
			else {
				double puntuacion = -99999;
				for(Action action: actions) {
					copia2.imitate(copia);
					copia2.update(action);
					double aux = evaluator.eval(copia2, state.p1Turn);
					if(aux>puntuacion) {
						current[j] = action;
						puntuacion = aux;
					}
				}
			}
			copia.update(current[j]);
		}
		long b = System.currentTimeMillis();
		parents.add(current);
		for(int i=1; i<50; i++) {
			copia.imitate(state);
			current = new Action[cantidad];
			for (int j=0; j<cantidad; j++) {
				
				copia.possibleActions(actions);
				pruner.prune(actions, copia);
				if(actions.size()==0) current[j] = new EndTurnAction();
				else current[j] = actions.get(rand.nextInt(actions.size()));
				copia.update(current[j]);
			}
			if(!parents.contains(current)) parents.add(current);
			else i--;
			
			
		}
		List<Action[]> vacio = new LinkedList<Action[]>();
		for(int i=0; i<numParents; i++) {
			for(int j=0; j<cantidad; j++) {
				current[j] = new EndTurnAction();
			}
			vacio.add(current);
		}
		ZonaIntercambio z = new ZonaIntercambio(vacio, state);
		z.addPadres(parents);
		if(NUM_THREADS >1) {	
			Thread[] hilos = new MiHebra[NUM_THREADS];
			for(int i=0; i<NUM_THREADS; i++) {
				hilos[i] = new MiHebra(i, NUM_THREADS, z, state, numParents, numNeighbors);
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
			MiHebra h = new MiHebra(0, 1, z, state, numParents, numNeighbors);
			h.run();
		}

		current = z.getBest();
		long c = System.currentTimeMillis();

		System.out.println("Ntbfea --> tiempo: "+(c-a)+"  iteraciones: "+iteraciones.get());
		return current;/**/
	}

	/*private Action searchBestAction(GameState copia) {
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
	}*/


	public Set<Action[]> neighbors(GameState state, List<Action[]> bests, List<Action[]> others, int numberOfSons, HashMap<Long, List<Action>> movimientosLegales, double probMutar) {
		
		NaturalMutator mutator = new NaturalMutator();
		mutator.setParents(bests);
		mutator.setOthers(others);
		mutator.setMutationProbability(probMutar);
		mutator.setTiempoLimite(halfTime);
		mutator.setValidator(state, movimientosLegales);
		mutator.setNumHijos(numberOfSons);
			
		return mutator.getSons();
	}

	
	class MiHebra extends Thread {
		private ZonaIntercambio z;
		private GameState state;
		private Action[] current;
		private boolean salir;
		//private ConcurrentHashMap<Long, List<Action>> movimientosLegales;
		private HashMap<Long, List<Action>> movimientosLegales;
		private LModel lModel;
		private List<Action[]> others;
		private int numPadres = 5;
		private int numHijos = 50;
		private double probMutar = .2;

		public MiHebra(int miId, int numThreads, ZonaIntercambio z, GameState state, int numPadres, int numHijos) {
			this.z = z;
			this.state= z.getState();
			this.lModel = z.getLModel();
			this.numHijos=numHijos;
			this.numPadres=numPadres;
			salir = false;
			this.movimientosLegales = new HashMap<Long, List<Action>>();
			//this.movimientosLegales.putAll(z.getMovimientosLegales());;
			//this.movimientosLegalesLocal.putAll(movimientosLegales);
		}
		
		public void run() {
			GameState clone = new GameState(state.map);
			others = new LinkedList<Action[]>();
			List<Action[]> parents = new LinkedList<Action[]>();
			
			parents = z.getParents();

			
			while(!salir) {
				/*synchronized(this) {
					System.out.println("-------------");
					for(Action[] a: parents) {
						Auxiliares.imprime(a);
					}
				}*/
				iteraciones.addAndGet(1);
				if(System.currentTimeMillis() >= halfTime) break;
				int cantidad = state.turn==1?3:5;
				for(int indi =0; indi<parents.size(); indi++) {
					clone.imitate(state);
					current = parents.get(indi);
					for(int i=0; i<cantidad; i++)
						clone.update(current[i]);
					double value =evaluator.eval(clone, state.p1Turn);
					//**add <current,value> to LModel
					this.lModel.addToLModel(current, value);
				}
				Set<Action[]> population = neighbors(state, parents, others, numHijos, movimientosLegales, probMutar);

				if(System.currentTimeMillis() >= halfTime) break;
				//current <-- argmax(Population)
				//Action[] aux = current;
				double peorPadre = this.lModel.puntua(parents.get(numPadres-1));
				double puntuaciones[] = new double[numPadres];
				for(int i=0; i<numPadres; i++) {
					if(System.currentTimeMillis() >= aBitTime) puntuaciones[i] = this.lModel.puntua(parents.get(i));
					else {
						GameState copia = new GameState(state.map);
						copia.imitate(state);
						for(Action action: parents.get(i)) copia.update(action);
						puntuaciones[i] = evaluator.eval(copia, state.p1Turn);
					}
				}
				for(Action[] a : population) {
					double nuevoValor = this.lModel.puntua(a);

					if(nuevoValor>peorPadre) {
						ListIterator<Action[]> iter = parents.listIterator();
						int ind = 0;
						while(iter.hasNext()) {
							Action[] accion = iter.next();
							double valor;
							if(System.currentTimeMillis() >= aBitTime) valor= this.lModel.puntua(accion);
							else {
								GameState copia = new GameState(state.map);
								copia.imitate(state);
								for(Action action: accion) copia.update(action);
								valor = evaluator.eval(copia, state.p1Turn);
							}
							if(valor>nuevoValor) ind++;
							else break;
						}
						if(!parents.contains(a)) {
							parents.add(ind, a);
							parents.remove(numPadres);
							if(System.currentTimeMillis() >= aBitTime) peorPadre= this.lModel.puntua(a);
							else {
								GameState copia = new GameState(state.map);
								copia.imitate(state);
								for(Action action: a) copia.update(action);
								peorPadre = evaluator.eval(copia, state.p1Turn);
							}
						}
					}
				}
				GameState copia = new GameState(state.map);
				for(int i=1; i<numPadres; i++) {
					if(puntuaciones[i] > puntuaciones[i-1]) {
						double puntuacion_aux = puntuaciones[i];
						puntuaciones[i] = puntuaciones[i-1];
						puntuaciones[i-1] = puntuacion_aux;
						Action[] acciones_aux = parents.remove(i);
						parents.add(i-1, acciones_aux);
						if(i>1) i-=2;
					}
				}
				
				System.out.println("---------------");
				for(Action[] acciones : parents) {
					copia.imitate(state);
					for(Action aa: acciones) copia.update(aa);
					Auxiliares.imprime(acciones);
					System.out.println(this.lModel.puntua(acciones, false)+" - "+ evaluator.eval(copia, state.p1Turn));
				}
				
				//z.addPadres(parents);
				//parents = z.getParents();
				if(System.currentTimeMillis() >= halfTime) break;
			}
			z.actualiza(lModel);
			//movimientosLegales.putAll(movimientosLegalesLocal);
		}
		
		
	}
	
	class ZonaIntercambio{
		private LModel lModel;
		private GameState state;
		private ConcurrentHashMap<Long, List<Action>> movimientosLegales;
		private List<Action[]> parents;
		
		public ZonaIntercambio(List<Action[]> parents, GameState state) {
			this.parents = parents;
			this.lModel = new LModel(5);
			this.state = new GameState(state.map);
			this.state.imitate(state);
			movimientosLegales = new ConcurrentHashMap<Long, List<Action>>();
		}

		public synchronized void addPadres(List<Action[]> parents2) {
			double peorPadre = this.lModel.puntua(parents.get(numParents-1));
			for(Action[] a : parents2) {
				double nuevoValor = this.lModel.puntua(a);
				
				if(nuevoValor>peorPadre) {
					parents.remove(numParents-1);
					ListIterator<Action[]> iter = parents.listIterator();
					int ind = 0;
					while(iter.hasNext()) {
						Action[] accion = iter.next();
						double valor = this.lModel.puntua(accion);
						if(valor>nuevoValor) ind++;
						else break;
					}if(!parents.contains(a)) {
						parents.add(ind, a);
						peorPadre = this.lModel.puntua(parents.get(numParents-1));
					}
				}
				
			}
		}

		public synchronized List<Action[]> getParents() {
			// TODO Auto-generated method stub
			List<Action[]> devolver = new LinkedList<Action[]>();
			devolver.addAll(parents);
			return devolver;
		}

		public synchronized ConcurrentHashMap<Long, List<Action>> getMovimientosLegales() {
			return movimientosLegales;
		}

		public synchronized LModel getLModel() {
			return lModel.copy();
		}

		public synchronized void actualiza(LModel lModel) {
			this.lModel.combineLModel(lModel);
		}

		public synchronized Action[] getBest() {
			/*boolean vacio = true;
			Action[] current = null;
			double value = 0;
			GameState copia = new GameState(state.map);

			Set<Object[]> todo = lModel.getBestActionsSet();
			List<Action[]> todoList =new LinkedList<Action[]>();
			for(Object[] o: todo) todoList.add((Action[]) o);
			addPadres(todoList);
			for(Action[] acciones : parents) {
				copia.imitate(state);
				for(Action a : acciones) {
					copia.update(a);
				}
				if(vacio) {
					vacio=false;
					current = acciones;
					value = evaluator.eval(copia, state.p1Turn);
				}else {
					double aux = evaluator.eval(copia, state.p1Turn);
					if(aux>value) {
						current = acciones;
						value = aux;
					}
				}
				if(almostFullTime < System.currentTimeMillis()) break;
			}
			return current;/**/
			Set<Object[]> todo = lModel.getBestActionsSet();
			
			boolean vacio = true;
			Action[] current = null;
			double value = 0;
			for (Object[] aux2 : todo) {
				Action[] acciones = (Action[]) aux2;
				if(vacio) {
					vacio=false;
					current = acciones;
					value = lModel.puntua(current, false);
				}else {
					double aux = lModel.puntua(acciones, false);
					if(aux>value) {
						current = acciones;
						value = aux;
					}
				}
				if(almostFullTime < System.currentTimeMillis()) { break;}
			}
			return current;/**/
		}

		public synchronized GameState getState() {
			return state;
		}
		
	}
	
	
}
