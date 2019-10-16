package ai.myTest;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import action.Action;
import action.DropAction;
import action.UnitAction;
import ai.AI;
import ai.myTest.MyGreedy.Greedy;
import ai.myTest.MyNtbea2.Ntbea;
import game.GameState;
import model.HaMap;
import util.Auxiliares;
import util.GuardaNumAcciones;

public class MyNtbeaAgent implements AI {
	private int turno, numAccion;
	private Action[] acciones;
	public static GuardaNumAcciones guardadorDeNumAcciones;
	
	private final String file = "";
	private final boolean restart = true;
	private final boolean save = false;

	private Set<Integer> tiempos = new HashSet<Integer>();
	
	private Ntbea ntbea;
	public MyNtbeaAgent(GuardaNumAcciones z) {
		this.guardadorDeNumAcciones= z;
		turno = -1;
		// TODO Auto-generated constructor stub
	}
	public MyNtbeaAgent() {
		turno = -1;
		// TODO Auto-generated constructor stub
	}
	/*
	 * 0 - Soldado
	 * 1 - Arquero
	 * 2 - Healer
	 * 3 - Mago
	 * 4 - Ninja
	 * 5 - Fuego
	 * 6 - Pocion
	 * 7 - Espada
	 * 8 - Perga
	 * 9 - Escudo
	 * 10- Yelmo
	 * 11- ???
	 */
	
	@Override
	public Action act(GameState state, long ms) {
		hazUnaVez(state, ms);
		//return null;
	    if(numAccion < acciones.length) return acciones[numAccion++];
	    else return null;
	}

		//Esta función se ejecuta una vez por tueno, al inicio de cada turno
	private void hazUnaVez(GameState state, long ms) {
		long start = System.currentTimeMillis();
		if(turno == state.turn) return;
		turno = state.turn;
		numAccion = 0;
		acciones = new Action[5];
		ntbea = new Ntbea();
		GameState copia = state.copy();
	    List<Action> actions = new LinkedList<Action>();
	    copia.possibleActions(actions);
	    Random rand = new Random();
    	if(guardadorDeNumAcciones!=null) guardadorDeNumAcciones.add(state.turn, actions.size());
    	
    	acciones = ntbea.searchBestCombination(copia);
    	long elapsedTime = System.currentTimeMillis() - start;
    	
    	FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter("times.txt", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    PrintWriter printWriter = new PrintWriter(fileWriter);
	    printWriter.println(elapsedTime);
	    printWriter.close();
		
	}
	@Override
	public void init(GameState state, long ms) {
		// TODO Auto-generated method stub
	}

	@Override
	public AI copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String header() {
		// TODO Auto-generated method stub
		return "header";
	}

	@Override
	public String title() {
		// TODO Auto-generated method stub
		return "My NTBEA Agent";
	}
	
}
