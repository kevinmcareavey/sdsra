package sdsra.payoffs;

import sdsra.Payoff;

public class DoublePayoff extends Payoff {
	
	private double value;
	
	public DoublePayoff(double v) {
		value = v;
	}
	
	@Override
	public double getDouble() {
		return value;
	}
	
	@Override
	public String toString() {
		return Double.toString(value);
	}
	
}
