package ai.myTest.MyGreedy;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import action.Action;
import ai.RandomAI;
import ai.evaluation.HeuristicEvaluator;
import ai.evaluation.IStateEvaluator;
import ai.evaluation.MaterialEvaluator;
import ai.evaluation.RolloutEvaluator;
import ai.util.RAND_METHOD;
import game.GameState;
import model.Card;
import model.HaMap;
import model.SquareType;
import model.Unit;
import util.Auxiliares;


public class Greedy {
	static final int ARCHER=0, CLERIC=1, KNIGHT=2, NINJA=3, WIZARD = 4, CRYSTAL = 5;
	static final int DRAGONSCALE=0, RUNEMETAL=1, HELMET=2, SCROLL=3;
	static final int ASSAULT=0, DEPLOY=1, DEFENSE=2, POWER=3, NONE=-1;
	
	static int NUM_HILOS = 2;
	static IStateEvaluator evaluator;
	
	private static int equipmentBonus(int unit, int equip) {
		if (unit == -1) return 0;
		int[][] matriz = {{30, 30, 30, 30, 20},
				{40, 20, -50, 20, 40},
				{20, 20, 20, 10, 20},
				{50, 30, -40, 40, 50}};
		return matriz[equip][unit];
	}
	
	private static int standingBonus(int unit, int square) {
		if (square == Greedy.NONE || unit == -1) return 0;
		int[][] matriz = {{40, 10, 120, 50, 40},
				{-75, -75, -75, -75, -75},
				{80, 20, 30, 60, 70},
				{120, 40, 30, 70, 100}};
		return matriz[square][unit];
	}
	
	public static void setEvaluator(IStateEvaluator evaluator) {
		Greedy.evaluator = evaluator;
	}
	
	public static double actAndEvalFunction(GameState state, Action action) {
		GameState copia = state.copy();
		copia.update(action);
		double value = evalFuncion(copia);
		return value;
	}

	
	private static double evalFuncion(GameState state) {
		/*
		Unit[][] units = state.units;
		HaMap mapa = state.map;
		int value = 0;
		for (int i=0; i< units.length; i++ )
			for (int j=0; j<units[i].length; j++) {
				if (units[i][j] != null) {
					int unidad=-1;
					Unit aux = units[i][j];
					String nombre = aux.toString().split(",")[1];
					if (nombre.equals("ARCHER")) unidad = Greedy.ARCHER;
					else if (nombre.equals("CLERIC")) unidad = Greedy.CLERIC;
					else if (nombre.equals("KNIGHT")) unidad = Greedy.KNIGHT;
					else if (nombre.equals("NINJA")) unidad = Greedy.NINJA;
					else if (nombre.equals("WIZARD")) unidad = Greedy.WIZARD;
					
					int squareEval = 0;
					switch (mapa.squares[i][j]){
					case NONE:
						break;
					case ASSAULT:
						squareEval = standingBonus(unidad, Greedy.ASSAULT);
						break;
					case DEPLOY_1:
						squareEval = aux.p1Owner?standingBonus(unidad, Greedy.DEPLOY):0;
						break;
					case DEPLOY_2:
						squareEval = aux.p1Owner?0:standingBonus(unidad, Greedy.DEPLOY);
						break;
					case DEFENSE:
						squareEval = standingBonus(unidad, Greedy.DEFENSE);
						break;
					case POWER:
						squareEval = standingBonus(unidad, Greedy.POWER);
						break;
					}
					
					
					int equipEval = 0;
					for (Card buff : aux.equipment) {
						switch (buff) {
						case DRAGONSCALE:
							equipEval = equipmentBonus(unidad, Greedy.DRAGONSCALE);
							break;
						case RUNEMETAL:
							equipEval = equipmentBonus(unidad, Greedy.RUNEMETAL);
							break;
						case SHINING_HELM:
							equipEval = equipmentBonus(unidad, Greedy.HELMET);
							break;
						case SCROLL:
							equipEval = equipmentBonus(unidad, Greedy.SCROLL);
							break;
						}
					}
					int individualValue = aux.hp + aux.maxHP()*(aux.hp==0?1:2);
					individualValue += equipEval*(aux.hp==0?0:2);
					individualValue += squareEval*(aux.hp==0?-1:1);
					//individualValue += (aux.p1Owner!=state.p1Turn&&nombre.equals("CRYSTAL")&&aux.hp<=0)?10000:0;
					boolean perteneciaAux = aux.p1Owner;
					boolean turno = state.p1Turn;
					
					value+=individualValue*(aux.p1Owner==state.p1Turn?1:-1);
					
				}
			}
			*/
		double value = evaluator.eval(state, state.p1Turn);
		return value;
	}
	
	public static Action searchBestAction(GameState state) {
		Action action = null;
		double score = 0;
		boolean initialized = false;
		List<Action> actions = new LinkedList<Action>();
		state.possibleActions(actions);
		//if(actions.size()<20) {
			for (Action i : actions) {
				if (!initialized) {
					action = i;
					score = actAndEvalFunction(state, i);
					initialized = true;
				}else {
					double temporalScore = actAndEvalFunction(state, i);
					//System.out.println(temporalScore + " -> "+i);
					if(temporalScore>score) {
						score = temporalScore;
						action = i;
					}
				}
			}
			return action;
		/*}else {
			Thread[] hilos = new Thread[NUM_HILOS];
			ZonaIntercambio z = new ZonaIntercambio();
			for (int i=0; i<NUM_HILOS; i++) {
				hilos[i] = new MyThread(i, NUM_HILOS, z, state, actions);
				hilos[i].start();
			}
			for (int i=0; i<NUM_HILOS; i++) {
				try {
					hilos[i].join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return z.getAccion();
		}*/
	}
	

	private static class MyThread extends Thread{
		double maxLocal;
		private int myId, numThreads;
		private Action accionLocal;
		private boolean vacio;
		private ZonaIntercambio z;
		private GameState state;
		private List<Action> actionList;
		
		public MyThread(int id, int numThreads, ZonaIntercambio zonaIntercambio, GameState state, List<Action> actionList) {
			maxLocal = 0;
			accionLocal = null;
			vacio = true;
			this.myId = id;
			this.numThreads = numThreads;
			this.z = zonaIntercambio;
			this.state = state;
			this.actionList = actionList;
		}
		
		@Override
		public void run() {
			for(int i=myId; i<actionList.size(); i+= numThreads) {
				Action aux = actionList.get(i);
				double score = actAndEvalFunction(state, aux);
				if (vacio) {
					accionLocal = aux;
					maxLocal = score;
					vacio = false;
				}else {
					if(score>maxLocal) {
						maxLocal = score;
						accionLocal = aux;
					}
				}
			}
			
			z.actualizaMaximo(maxLocal, accionLocal);
		}
	}
	
	private static class ZonaIntercambio{
		private double maximo;
		private Action accion;
		private boolean vacio;
		
		public ZonaIntercambio() {
			maximo = 0;
			vacio = true;
			accion = null;
		}

		public synchronized void actualizaMaximo(double maxLocal, Action accionLocal) {
			if (vacio) {
				accion = accionLocal;
				maximo = maxLocal;
				vacio = false;
			}else {
				if(maxLocal>maximo) {
					accion = accionLocal;
					maximo = maxLocal;
				}
			}
		}
		
		public synchronized Action getAccion() {
			return accion;
		}
	}

	public static double evalActions(GameState state, Action[] acciones) {
		GameState copia = state.copy();
		int i=0;
		for (Action act : acciones) {
			copia.update(act);
			boolean a = copia.p1Turn;
			int turno = copia.turn;
			i++;
		}
		boolean a = copia.p1Turn;
		double val = evalFuncion(copia);
		return val;
	}
}
