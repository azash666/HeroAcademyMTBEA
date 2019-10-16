package ai.myTest;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import action.Action;
import action.DropAction;
import action.UnitAction;
import ai.AI;
import ai.myTest.MyGreedy.Greedy;
import game.GameState;
import model.HaMap;
import util.Auxiliares;
import util.GuardaNumAcciones;

public class MyGreedyAgent implements AI {
	private int turno, numAccion;
	private Action[] acciones;
	public static GuardaNumAcciones z;
	public MyGreedyAgent(GuardaNumAcciones z) {
		this.z= z;
		turno = -1;
		// TODO Auto-generated constructor stub
	}
	public MyGreedyAgent() {
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
		if(turno == state.turn) return;
		turno = state.turn;
		numAccion = 0;
		acciones = new Action[5];
		GameState copia = state.copy();
	    List<Action> actions = new LinkedList<Action>();
	    copia.possibleActions(actions);
	    Random rand = new Random();
    	if(z!=null) z.add(state.turn, actions.size());
	    for(int i=0; i<5; i++) {
	    	copia.possibleActions(actions);
		    
		    if(actions.size()>0) acciones[i] = Greedy.searchBestAction(copia);
		    

	    	copia.update(acciones[i]);
	    }
	    
		
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
		return "My Fabulous AI";
	}
	
}
