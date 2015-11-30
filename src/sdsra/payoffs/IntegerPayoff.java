package sdsra.payoffs;

import sdsra.Payoff;

public class IntegerPayoff extends Payoff {
	
	private int value;
	
	public IntegerPayoff(int v) {
		value = v;
	}
	
	@Override
	public double getDouble() {
		return (double)value;
	}
	
	@Override
	public String toString() {
		return Integer.toString(value);
	}
	
}
