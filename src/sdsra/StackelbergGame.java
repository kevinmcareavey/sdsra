package sdsra;

import java.util.HashMap;
import java.util.Map;

import sdsra.pairs.PlayerPair;
import sdsra.pairs.player_pairs.PureStrategyProfile;
import data_structures.AdvancedSet;

public class StackelbergGame {
	
	private PlayerPair<AdvancedSet<PureStrategy>> pureStrategies;
	private Map<PureStrategyProfile, PlayerPair<Payoff>> payoffs;
	
	public StackelbergGame(PlayerPair<AdvancedSet<PureStrategy>> ps) {
		pureStrategies = ps;
		payoffs = new HashMap<PureStrategyProfile, PlayerPair<Payoff>>();
	}
	
	public void setPayoffs(PureStrategyProfile psp, PlayerPair<Payoff> p) throws Exception {
		if(pureStrategies.getLeft().contains(psp.getLeft()) && pureStrategies.getRight().contains(psp.getRight())) {
			payoffs.put(psp, p);
		} else {
			throw new Exception("invalid pure strategy profile");
		}
	}
	
	public PlayerPair<Payoff> getPayoffs(PureStrategyProfile psp) {
		return payoffs.get(psp);
	}
	
	@Override
	public String toString() {
		String output = "";
		String delim = "";
		for(Map.Entry<PureStrategyProfile, PlayerPair<Payoff>> entry : payoffs.entrySet()) {
			output += delim + entry.getKey() + ":=" + entry.getValue();
			delim = "\n";
		}
		return output;
	}
	
}
