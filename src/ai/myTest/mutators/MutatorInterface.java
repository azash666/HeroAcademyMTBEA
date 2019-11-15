package ai.myTest.mutators;

import java.util.List;
import java.util.Set;

import action.Action;

public interface MutatorInterface <E>{
	public void setParents(List<E[]> parents);
	public void mutate(double probability);
	public Set<E[]> getSons();
}
