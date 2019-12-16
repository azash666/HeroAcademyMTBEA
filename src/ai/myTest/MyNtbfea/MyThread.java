package ai.myTest.MyNtbfea;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import action.Action;
import ai.evaluation.IStateEvaluator;
import game.GameState;
//import util.Auxiliares;

class MyThread extends Thread {
	private ExchangeZone z;
	private GameState state;
	private Action[] current;
	private boolean salir;
	//private ConcurrentHashMap<Long, List<Action>> movimientosLegales;
	private HashMap<Long, List<Action>> legalMoves;
	private LModel lModel;
	private int numParents = 5;
	private int numOffsprings = 50;
	private double probMutar = .2;
	private long evolutionTime;
	private long evaluatorTime;
	private IStateEvaluator evaluator;
	private boolean useMap;

	public MyThread(int miId, ExchangeZone z, GameState state, IStateEvaluator evaluator, boolean useMap) {
		this.z = z;
		this.state = z.getState();
		this.lModel = z.getLModel();
		this.numOffsprings = Ntbfea.numOffsprings;
		this.numParents = Ntbfea.numParents;
		salir = false;
		this.legalMoves = new HashMap<Long, List<Action>>();
		evolutionTime = Ntbfea.evolutionTime;
		evaluatorTime = Ntbfea.evaluatorTime;
		this.evaluator = evaluator;
		this.useMap = useMap;
	}
	
	public void run() {
		GameState clone = new GameState(state.map);
		List<Action[]> parents = new LinkedList<Action[]>();
		
		parents = z.getParents();

		while(!salir) {

			Ntbfea.iterations.addAndGet(1);				//Used to measure number of generations per turn
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
			
			Set<Action[]> population = Ntbfea.neighbors(state, parents, numOffsprings, legalMoves, probMutar, useMap);

			if(System.currentTimeMillis() >= evolutionTime) break;
			Double puntuaciones[] = new Double[numParents];
			for(int i=0; i<parents.size(); i++) {
				if(System.currentTimeMillis() >= evaluatorTime) {
					puntuaciones[i] = this.lModel.eval(parents.get(i));
				}
				else {
					GameState copia = new GameState(state.map);
					copia.imitate(state);
					for(Action action: parents.get(i)) copia.update(action);
					puntuaciones[i] = evaluator.eval(copia, state.p1Turn);
				}
			}

			
			for(Action[] turnActions : population) {
				double newValue;
				if(System.currentTimeMillis() >= evaluatorTime) {
					newValue= this.lModel.eval(turnActions);
				}
				else { 
					GameState GS_Copy = new GameState(state.map);
					GS_Copy.imitate(state);
					for(Action action: turnActions) GS_Copy.update(action);
					newValue = evaluator.eval(GS_Copy, state.p1Turn);
				}
				int anyadidos = 0;
				if((numParents > parents.size() || newValue>puntuaciones[numParents-1-anyadidos] )&& !listContainsArray(parents, turnActions)) {
					if(numParents == parents.size()) {
						parents.remove(numParents-1-anyadidos);
						puntuaciones[numParents-1-anyadidos] = newValue;
						parents.add(numParents-1-anyadidos, turnActions);
						anyadidos++;
					}else {
						puntuaciones[parents.size()] = newValue;
						parents.add(turnActions);
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
	
	
}
