package data_structures;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import utilities.Utilities;

public class ProbabilityDistribution<T> {
	
	private Map<T, Double> probabilities;
	
	public ProbabilityDistribution() {
		probabilities = new HashMap<T, Double>();
	}
	
	public void setProbability(T e, double p) throws Exception {
		if(p >= 0 && p <= 1) {
			if(p == 0) {
				probabilities.remove(e);
			} else {
				probabilities.put(e, p);
			}
		} else {
			throw new Exception("probability must be in the interval [0,1]");
		}
	}
	
	public double getProbability(T e) {
		double probability = 0;
		if(probabilities.containsKey(e)) {
			probability = probabilities.get(e);
		}
		return probability;
	}
	
	public int size() {
		return probabilities.size();
	}
	
	public Set<Map.Entry<T, Double>> entrySet() {
		return probabilities.entrySet();
	}
	
	public AdvancedSet<T> getFrame() {
		return new AdvancedSet<T>(probabilities.keySet());
	}
	
	@Override
	public String toString() {
		AdvancedSet<String> output = new AdvancedSet<String>();
		for(Map.Entry<T, Double> entry : this.entrySet()) {
			output.add("P(" + entry.getKey() + ")=" + Utilities.format(entry.getValue()));
		}
		return output.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((probabilities == null) ? 0 : probabilities.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		ProbabilityDistribution<T> other = (ProbabilityDistribution<T>) obj;
		if (probabilities == null) {
			if (other.probabilities != null)
				return false;
		} else if (!probabilities.equals(other.probabilities))
			return false;
		return true;
	}
	
}
