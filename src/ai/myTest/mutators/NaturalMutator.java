package ai.myTest.mutators;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import action.Action;
import ai.util.ActionPruner;
import game.GameState;
import util.Auxiliares;

public class NaturalMutator implements MutatorInterface<Action> {
	private List<Action[]> parents;
	private Set<Action[]> sons;
	private Set<Action[]> others;
	private double mutationProbability = 0;
	private int numHijos;
	private long tiempoLimite = 99999999999999L;
	private HashMap<Long, List<Action>> movimientosLegales;
	private GameState state;
	private ActionPruner pruner;

	@Override
	public void setParents(List<Action[]> parents) {
		this.parents = parents;
	}

	public void setOthers(List<Action[]> others) {
		this.others = new HashSet<Action[]>();
		for(Action[] accion: others) this.others.add(accion);
	}

	public void setTiempoLimite(long time) {
		this.tiempoLimite = time;
	}

	public void setValidator(GameState state, HashMap<Long, List<Action>> movimientosLegales) {
		this.movimientosLegales = movimientosLegales;
		this.pruner = new ActionPruner();
		this.state = new GameState(state.map);
		this.state.imitate(state);
	}

	public void setMutationProbability(double prob) {
		this.mutationProbability = prob;
	}

	public void setNumHijos(int numHijos) {
		this.numHijos=numHijos;
	}

	@Override
	public Set<Action[]> getSons() {
		long ab=0, bc=0, cd=0;
		int cantidad = parents.size();
		Random r = new Random();
		sons = new HashSet<Action[]>();
		sons.addAll(parents);
		GameState copia = new GameState(state.map);
		while(true) {
			long a = System.nanoTime();
			if(cantidad>numHijos) break;
			int i = r.nextInt(parents.size());
			int j = r.nextInt(parents.size());
			while(i==j) j = r.nextInt(parents.size());
			if(System.currentTimeMillis() >= tiempoLimite) break;
			if(cantidad>numHijos) break;
			if(cantidad>=numHijos) break;
			//int index = r.nextInt(parents.get(i).length);
			Action[] current = parents.get(i).clone();
			copia.imitate(state);
			int muta=-1;
			double p = r.nextDouble();
			if (p<mutationProbability) {
				muta=r.nextInt(parents.get(i).length);
			}
			long b = System.nanoTime(); ab += b-a;
			//long d = System.nanoTime(); cd += d-c;
			for(int k=0; k<parents.get(i).length; k++) {
				if(System.currentTimeMillis() >= tiempoLimite) break;
				List<Action> acciones = new LinkedList<Action>();
				long key = copia.hash();
				if(movimientosLegales.containsKey(key)) {
					acciones = movimientosLegales.get(key);
				}
				else {
					copia.possibleActions(acciones); 
					pruner.prune(acciones, copia);
					movimientosLegales.put(key, acciones);
				}

				if(System.currentTimeMillis() >= tiempoLimite) break;
				if(r.nextBoolean() && muta!=k) {
					current[k] = parents.get(j)[k];

				}else if(muta==k){
					if(acciones.size()==0) break;
					current[k] = acciones.get(r.nextInt(acciones.size()));
				}
				if(System.currentTimeMillis() >= tiempoLimite) break;
				if(!acciones.contains(current[k])) {
					if(acciones.size()==0) break;
					if(k<current.length-1) 
						if(acciones.contains(current[k+1])) current[k] = current[k+1];
						else if(acciones.contains(parents.get(j)[k+1])) current[k] = parents.get(j)[k+1];
						else
							current[k] = acciones.get(r.nextInt(acciones.size()));
					else
						current[k] = acciones.get(r.nextInt(acciones.size()));
				}

				copia.update(current[k]);
			}
			long c = System.nanoTime(); bc += c-b;
			sons.add(current);
			cantidad++;
			

			//}
			//}
			//if(cantidad>=numHijos) veces = (numHijos/parents.size());
		}
		//System.out.println("ab "+ab/1000+" bc "+bc/1000+" cd "+cd/1000);
		return sons;
	}

	@Override
	public void mutate(double probability) {
		// TODO Auto-generated method stub

	}

}
