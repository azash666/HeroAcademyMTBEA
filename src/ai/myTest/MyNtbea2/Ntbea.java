package ai.myTest.MyNtbea2;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import action.Action;
import ai.myTest.MyGreedy.Greedy;
import ai.myTest.MyNtbea2.LModelTest;
import game.GameState;
import util.Auxiliares;

public class Ntbea {
	
	public Random rand;
	
	public int nbEvals = 5; //Number total of evaluations allowed
	public int numNeighbors = 5;


	public final int NUM_THREADS = 1;
	
	public Ntbea() {
		rand = new Random();
	}


	@SuppressWarnings("unused")
	public Action[] searchBestCombination(GameState state) {

		
		GameState copia = state.copy();
		Action[] current = new Action[5];
	    List<Action> actions = new LinkedList<Action>();
	    copia.possibleActions(actions);
	    
		for(int i=0; i<5; i++) {
			copia.possibleActions(actions);
		    
		    if(actions.size()>0) current[i] = Greedy.searchBestAction(copia);
		    
		    copia.update(current[i]);
	    }
		copia = state.copy();
		//for (int j = 0; j<repeticiones; j++) {
		ZonaIntercambio z = new ZonaIntercambio(current, copia);
		if(NUM_THREADS >1) {	
			Thread[] hilos = new MiHebra[NUM_THREADS];
			for(int i=0; i<NUM_THREADS; i++) {
				hilos[i] = new MiHebra(i, NUM_THREADS, z, copia, nbEvals, current);
				hilos[i].start();
			}

			for(int i=0; i<NUM_THREADS; i++) {
				try {
					hilos[i].join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			MiHebra h = new MiHebra(0, 1, z, copia, nbEvals, current);
			h.run();
		}
			
		current = z.getBest();
		//Auxiliares.imprime(z.lModel);
		Auxiliares.imprime(current);
		System.out.println(Greedy.evalActions(state, current));
		//throw new RuntimeException();
		return current;
	}

	public Set<Action[]> neighbors(GameState state, Action[] current, int numberOfNeighbors, double mutationProbability_A_BORRAR, boolean flipOnce) {
		Set<Action[]> population= new HashSet<Action[]>();
		for (int k=0; k<numberOfNeighbors; k++) {
			GameState copia = state.copy();
			Action[] neighbor = current.clone();
			List<Action> acciones = new LinkedList<Action>();
			int i=flipOnce?rand.nextInt(current.length):0;
			for (int j=0; j<current.length; j++) {
				//*******************************
				//acciones = current[j].possibleActions();
			    copia.possibleActions(acciones);
				//*******************************
			    if(acciones.size()<1) break;
				if (j==i) {
					Action aux2 = acciones.get(rand.nextInt(acciones.size()));
				    if(aux2.toString().equals("EndTurnAction []")) break;
				    neighbor[j] = aux2;
				}else if(j>i) {
					if (!acciones.contains(neighbor[j])) 
						neighbor[j] = acciones.get(rand.nextInt(acciones.size()));//Greedy.searchBestAction(copia);
				}
				copia.update(neighbor[j]);
				
			}
			population.add(neighbor);
		}
		return population;
	}

	public Ntbea putNbEvals(int nbEvals) {
		this.nbEvals = nbEvals;
		return this;
	}
	
	class MiHebra extends Thread {
		private int miId, numThreads, nbEvals;
		private ZonaIntercambio z;
		private GameState copia;
		private Action[] current;
		private double value;
		
		private LModel lModel;

		public MiHebra(int miId, int numThreads, ZonaIntercambio z, GameState state, int nbEvals, Action[] current) {
			this.miId = miId;
			this.numThreads = numThreads;
			this.z = z;
			this.copia= z.getState();
			this.nbEvals = nbEvals;
			this.current = current;
			//this.value = Greedy.evalActions(this.copia, this.current);
			this.lModel = z.getLModel();
		}
		
		public void run() {

			for(int t=miId; t<nbEvals; t+=numThreads) {
				double value = Greedy.evalActions(copia, current);
				//**add <current,value> to LModel
				this.lModel.addToLModel(current, value);
				
				//**Population<--Neighbors(Model, current, n, p, flipOnce)
				Set<Action[]> population = new HashSet<Action[]>();
			    
				population = neighbors(copia, current, numNeighbors, 0.5, true);
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
		private GameState copia;
		
		public ZonaIntercambio(Action[] current, GameState state) {
			this.current = current;
			this.lModel = new LModel(5);
			this.copia = state.copy();
			
		}

		public synchronized LModel getLModel() {
			return lModel.copy();
		}

		public synchronized void actualiza(LModel lModel) {
			this.lModel.combineLModel(lModel);
		}

		public synchronized Action[] getBest() {
			Set<Object[]> todo = lModel.getBestActionsSet();
			Set<Object[]> todo2 = lModel.getBestActionsSet();
			for (Object[] objetos : todo) {
				Action[] acciones = (Action[]) objetos;
				Set<Action[]> auxiliar = neighbors(copia, acciones, numNeighbors, 0.5, true);
				for (Action[] vecino : auxiliar) {
					todo2.add(vecino);
				}
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
			}
			
			return current;
		}

		public synchronized GameState getState() {
			return copia;
		}
		
	}
	
}
