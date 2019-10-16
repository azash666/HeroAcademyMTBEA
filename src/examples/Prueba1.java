package examples;

import ai.AI;
import ai.myTest.MyRandomAgent;
import ai.myTest.MyGreedyAgent;
import ai.myTest.MyNtbeaAgent;
import model.DECK_SIZE;
import util.GuardaNumAcciones;
import game.Game;
import game.GameArguments;

public class Prueba1 {
	private static GuardaNumAcciones z;
	static int gana =0, pierde=0, empata=0, cantidad=50;
	final String file = "save2.txt";
	
	public static void main(String[] args) {
		
		
		//humanVsHuman();
		//humanVsAI(false);
		//AIVsAI(false);
		long ini = System.currentTimeMillis();
		for(int i=0; i<cantidad; i++) {
			System.out.println("Partida "+(i+1));
			//z = new ZonaIntercambio(file);
			noGfx();
			//z.close();
			System.out.println("gana: "+gana+"   pierde: "+pierde+"   empata: "+empata);
		}
		long fin = System.currentTimeMillis();
		System.out.println("Ganadas = "+gana*100/cantidad+"%");
		System.out.println("Perdidas = "+pierde*100/cantidad+"%");
		System.out.println("Empatadas = "+empata*100/cantidad+"%");
		System.out.println("Tiempo = "+(fin-ini)+" milisegundos");
		
	}

	private static void noGfx() {
		
		AI p1 = new MyGreedyAgent();
		AI p2 = new MyNtbeaAgent();
		
		GameArguments gameArgs = new GameArguments(false, p1, p2, "a", DECK_SIZE.STANDARD);
		gameArgs.gfx = false; 
		Game game = new Game(null, gameArgs);
		
		game.run();
		int num = game.state.getWinner();
		if (num == 2) gana++;
		else if (num==1) pierde++;
		else empata++;
	}

	private static void humanVsAI(boolean guardar) {
		
		int budget = 4000; // 4 sec for AI's
		
		AI p2 = null;
		//AI p2 = new RandomAI(RAND_METHOD.BRUTE);
		//AI p2 = new GreedyActionAI(new HeuristicEvaluator(false));
		//AI p2 = new GreedyTurnAI(new HeuristicEvaluator(false), budget);

		AI p1 = guardar? new MyNtbeaAgent(z) : new MyNtbeaAgent();
		//AI p2 = new OnlineIslandEvolution(true, 100, 0.1, 0.5, budget, new HeuristicEvaluator(false));
		
		GameArguments gameArgs = new GameArguments(true, p1, p2, "a", DECK_SIZE.STANDARD);
		gameArgs.budget = budget; 
		Game game = new Game(null, gameArgs);
		
		game.run();
		
	}
		
	private static void AIVsAI(boolean guardar) {
		
		int budget = 4000; // 4 sec for AI's
		AI p2= guardar? new MyRandomAgent(z) : new MyRandomAgent();
		//AI p2 = new RandomAI(RAND_METHOD.BRUTE);
		//AI p2 = new GreedyActionAI(new HeuristicEvaluator(false));
		//AI p2 = new GreedyTurnAI(new HeuristicEvaluator(false), budget);
		AI p1 = guardar? new MyNtbeaAgent(z) : new MyNtbeaAgent();
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

