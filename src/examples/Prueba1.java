package examples;

import ai.AI;
import ai.RandomAI;
import ai.evaluation.HeuristicEvaluator;
import ai.evaluation.IStateEvaluator;
import ai.evaluation.RolloutEvaluator;
import ai.evolution.OnlineEvolution;
import ai.evolution.OnlineEvolutionModificado;
import ai.mcts.Mcts;
import ai.myTest.MyRandomAgent;
import ai.util.RAND_METHOD;
import ai.myTest.MyGreedyAgent;
import ai.myTest.MyNtbeaAgent;
import ai.myTest.MyNtbfeaAgent;
import model.DECK_SIZE;
import util.GuardaNumAcciones;
import game.Game;
import game.GameArguments;

public class Prueba1 {
	private static GuardaNumAcciones z;
	static int gana =0, pierde=0, empata=0, cantidad=100;
	final String file = "save2.txt";
	static IStateEvaluator evaluator, evaluator2;
	
	public static void main(String[] args) {
		
		evaluator = new HeuristicEvaluator(false);
		evaluator2 = new RolloutEvaluator(1, 1, new RandomAI(RAND_METHOD.TREE), new HeuristicEvaluator(false));

		AI oep = new OnlineEvolutionModificado(false, 45, .2, .1, 1000, evaluator);
		AI greedy = new MyGreedyAgent(evaluator);
		AI ntbfea = new MyNtbfeaAgent(evaluator, 1000, 5, 50);
		AI random = new MyRandomAgent();
		
		//humanVsHuman();
		//humanVsAI(false);
		//AIVsAI(false, ntbfea, oep);
		long ini = System.currentTimeMillis();
		for(int i=0; i<cantidad; i++) {
			long a = System.currentTimeMillis();
			System.out.println("Partida "+(i+1));
			//z = new ZonaIntercambio(file);
			
			noGfx(i%2==1, ntbfea, oep);
			
			//z.close();
			System.out.print("ntbfea(0,30) vs oep -->gana: "+gana+"   pierde: "+pierde+"   empata: "+empata);
			long b = System.currentTimeMillis()-a;
			System.out.println("   Tiempo de la partida: "+b);
		}
		long fin = System.currentTimeMillis();
		System.out.println("Ganadas = "+gana*100/cantidad+"%");
		System.out.println("Perdidas = "+pierde*100/cantidad+"%");
		System.out.println("Empatadas = "+empata*100/cantidad+"%");
		System.out.println("Tiempo = "+(fin-ini)+" milisegundos");
		/**/
	}

	private static void noGfx(boolean revancha, AI p1, AI p2) {
		GameArguments gameArgs;
		if(revancha) {

			gameArgs = new GameArguments(false, p1, p2, "a", DECK_SIZE.STANDARD);
		}else {
			gameArgs = new GameArguments(false, p2, p1, "a", DECK_SIZE.STANDARD);
		}
		gameArgs.gfx = false; 
		Game game = new Game(null, gameArgs);
		
		game.run();
		int num = game.state.getWinner();
		if (num == 2) {if(revancha) pierde++; else gana++;}
		else if (num==1) {if(revancha) gana++; else pierde++;}
		else empata++;
	}

	private static void humanVsAI(AI p1) {
		
		int budget = 4000; // 4 sec for AI's
		
		AI p2 = null;
		//AI p2 = new RandomAI(RAND_METHOD.BRUTE);
		//AI p2 = new GreedyActionAI(new HeuristicEvaluator(false));
		//AI p2 = new GreedyTurnAI(new HeuristicEvaluator(false), budget);

		//AI p1 = new MyNtbeaAgent(evaluator, 1200, 40);
		//AI p2 = new OnlineIslandEvolution(true, 100, 0.1, 0.5, budget, new HeuristicEvaluator(false));
		
		GameArguments gameArgs = new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD);
		gameArgs.budget = budget; 
		Game game = new Game(null, gameArgs);
		
		game.run();
		
	}
		
	private static void AIVsAI(boolean guardar, AI p1, AI p2) {
		
		int budget = 4000; // 4 sec for AI's
		//AI p2 = new OnlineEvolutionModificado(false, 50, .1, .1, 2500, evaluator);
		//AI p2 = new RandomAI(RAND_METHOD.BRUTE);
		//AI p2 = new GreedyActionAI(new HeuristicEvaluator(false));
		//AI p2 = new GreedyTurnAI(new HeuristicEvaluator(false), budget);
		//AI p2 = new OnlineIslandEvolution(true, 100, 0.1, 0.5, budget, new HeuristicEvaluator(false));
		
		GameArguments gameArgs = new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD);
		gameArgs.budget = budget; 
		Game game = new Game(null, gameArgs);
		
		game.run();
		
	}


	private static void humanVsHuman() {
		
		AI p1 = null;
		AI p2 = null;
		
		Game game = new Game(null, new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD));
		game.run();
		
	}
	

}

