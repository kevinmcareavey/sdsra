package sdsra.pairs;

import data_structures.Pair;

public class PlayerPair<T> extends Pair<T> {
	
	public PlayerPair(T d, T a) {
		super(d, a);
	}
	
	public T getDefender() {
		return this.getLeft();
	}
	
	public T getAttacker() {
		return this.getRight();
	}
	
}
