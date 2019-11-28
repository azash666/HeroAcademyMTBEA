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
import game.GameState;
import model.HaMap;
import util.Auxiliares;
import util.GuardaNumAcciones;

public class MyRandomAgent implements AI {
	private int turno, numAccion;
	private Action[] acciones;
	public static GuardaNumAcciones z;
	public MyRandomAgent(GuardaNumAcciones z) {
		this.z= z;
		turno = -1;
		// TODO Auto-generated constructor stub
	}
	public MyRandomAgent() {
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
	    	boolean elegido = false;
	    	UnitAction aux = null;
		    List<Action> lista = new LinkedList<Action>();
		    /*for(int j=0; j<actions.size(); j++) {
		    	if(actions.get(j) instanceof UnitAction) {
		    		aux = (UnitAction) actions.get(j);
		    		if(copia.units[aux.to.x][aux.to.y] != null) {
		    			if(copia.units[aux.to.x][aux.to.y].p1Owner != copia.p1Turn) {
			    			elegido = true;
			    			lista.add(aux);
		    			}
		    		}
		    	}
		    }*/
		    if(elegido) {
		    	acciones[i] = lista.get(rand.nextInt(lista.size()));
		    }else {
		    	if(actions.size()>0) acciones[i] = actions.get(rand.nextInt(actions.size()));
		    }

	    	actions.remove(acciones[i]);
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
