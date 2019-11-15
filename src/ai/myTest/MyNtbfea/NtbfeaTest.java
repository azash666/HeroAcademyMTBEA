package ai.myTest.MyNtbfea;

import ai.evaluation.HeuristicEvaluator;
import game.GameState;
import model.HaMap;
import model.SquareType;

public class NtbfeaTest {
	private static Ntbfea agente;
	private static GameState state;
	private static SquareType[][] map;
	

	public static void main(String[] args) {
		agente = new Ntbfea(new HeuristicEvaluator(false), 1000, 5, 50);
		state = setState();
		agente.NUM_THREADS=1;
		searchBestCombinationTest();
	}

	public static GameState setState() {
		setMap();
		GameState state = new GameState(new HaMap(5, 5, map, "Asd"));
		state.imitate(state);
		return state;
	}

	private static void setMap() {
		map = new SquareType[5][5];
		for(int i=0; i<5; i++)
			for(int j=0; j<5; j++)
				map[i][j] = SquareType.NONE;
		map[2][2] = SquareType.POWER;
		map[0][0] = SquareType.DEPLOY_1;
		map[4][4] = SquareType.DEPLOY_2;
		map[0][4] = SquareType.DEFENSE;
		map[4][0] = SquareType.ASSAULT;
	}

	private static void searchBestCombinationTest() {
		// TODO Auto-generated method stub
		
	}
}
