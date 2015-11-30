package sdsra.pairs;

import data_structures.Pair;

public class TargetPair<T> extends Pair<T> {
	
	public TargetPair(T d, T a) {
		super(d, a);
	}
	
	public T getCovered() {
		return this.getLeft();
	}
	
	public T getUncovered() {
		return this.getRight();
	}
	
}
