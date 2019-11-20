package ai.myTest.MyNtbfea;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import action.Action;
import ai.evaluation.IStateEvaluator;
import game.GameState;
import util.Auxiliares;

class MyThread extends Thread {
	private ExchangeZone z;
	private GameState state;
	private Action[] current;
	private boolean salir;
	//private ConcurrentHashMap<Long, List<Action>> movimientosLegales;
	private HashMap<Long, List<Action>> movimientosLegales;
	private LModel lModel;
	private int numParents = 5;
	private int numOffsprings = 50;
	private double probMutar = .2;
	private long evolutionTime;
	private long evaluatorTime;
	private IStateEvaluator evaluator;

	public MyThread(int miId, ExchangeZone z, GameState state, IStateEvaluator evaluator) {
		this.z = z;
		this.state = z.getState();
		this.lModel = z.getLModel();
		this.numOffsprings = Ntbfea.numOffsprings;
		this.numParents = Ntbfea.numParents;
		salir = false;
		this.movimientosLegales = new HashMap<Long, List<Action>>();
		evolutionTime = Ntbfea.evolutionTime;
		evaluatorTime = Ntbfea.evaluatorTime;
		this.evaluator = evaluator;
	}
	
	public void run() {
		GameState clone = new GameState(state.map);
		List<Action[]> parents = new LinkedList<Action[]>();
		
		parents = z.getParents();

		
		while(!salir) {
			//Ntbfea.iterations.addAndGet(1);				//Used to measure number of generations per turn
			if(System.currentTimeMillis() >= evolutionTime) break;
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
			Set<Action[]> population = Ntbfea.neighbors(state, parents, numOffsprings, movimientosLegales, probMutar);

			if(System.currentTimeMillis() >= evolutionTime) break;
			//current <-- argmax(Population)
			//Action[] aux = current;
			//double peorPadre = this.lModel.puntua(parents.get(numPadres-1));
			Double puntuaciones[] = new Double[numParents];
			for(int i=0; i<parents.size(); i++) {
				if(System.currentTimeMillis() >= evaluatorTime) puntuaciones[i] = this.lModel.eval(parents.get(i));
				else {
					GameState copia = new GameState(state.map);
					copia.imitate(state);
					for(Action action: parents.get(i)) copia.update(action);
					puntuaciones[i] = evaluator.eval(copia, state.p1Turn);
				}
			}
			for(Action[] acciones : population) {
				double nuevoValor = this.lModel.eval(acciones);
				if(System.currentTimeMillis() >= evaluatorTime) nuevoValor= this.lModel.eval(acciones);
				else {
					GameState copia = new GameState(state.map);
					copia.imitate(state);
					for(Action action: acciones) copia.update(action);
					nuevoValor = evaluator.eval(copia, state.p1Turn);
				}
				int anyadidos = 0;
				if((numParents > parents.size() || nuevoValor>puntuaciones[numParents-1-anyadidos] )&& !Auxiliares.containsVector(parents, acciones)) {
					if(numParents == parents.size()) {
						parents.remove(numParents-1-anyadidos);
						puntuaciones[numParents-1-anyadidos] = nuevoValor;
						parents.add(numParents-1-anyadidos, acciones);
						anyadidos++;
					}else {
						puntuaciones[parents.size()] = nuevoValor;
						parents.add(acciones);
					}
				}
			}
			
			//Ordenar padres
			for(int i=1; i<numParents; i++) {
				if(puntuaciones[i] > puntuaciones[i-1]) {
					double puntuacion_aux = puntuaciones[i];
					puntuaciones[i] = puntuaciones[i-1];
					puntuaciones[i-1] = puntuacion_aux;
					Action[] acciones_aux = parents.remove(i);
					parents.add(i-1, acciones_aux);
					if(i>1) i-=2;
				}
			}
			
			if(System.currentTimeMillis() >= evolutionTime) break;
		}
		z.update(lModel);
	}
	
	
}
