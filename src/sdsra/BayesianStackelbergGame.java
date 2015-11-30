package sdsra;

import java.util.HashMap;
import java.util.Map;

import sdsra.pairs.PlayerPair;
import sdsra.pairs.player_pairs.PureStrategyProfile;
import data_structures.AdvancedSet;
import data_structures.ProbabilityDistribution;

public class BayesianStackelbergGame {
	
	private PlayerPair<AdvancedSet<PureStrategy>> pureStrategies;
	private AdvancedSet<AttackerType> attackerTypes;
	private ProbabilityDistribution<AttackerType> attackerTypeProbabilities;
	private Map<AttackerType, StackelbergGame> attackerTypeGames;
	
	public BayesianStackelbergGame(PlayerPair<AdvancedSet<PureStrategy>> ps, AdvancedSet<AttackerType> a, ProbabilityDistribution<AttackerType> pd) {
		pureStrategies = ps;
		attackerTypes = a;
		attackerTypeProbabilities = pd;
		attackerTypeGames = new HashMap<AttackerType, StackelbergGame>();
		for(AttackerType type : attackerTypes) {
			attackerTypeGames.put(type, new StackelbergGame(pureStrategies));
		}
	}
	
	public PlayerPair<AdvancedSet<PureStrategy>> getPureStrategies() {
		return pureStrategies;
	}
	
	public void setPayoffs(AttackerType at, PureStrategyProfile psp, PlayerPair<Payoff> p) throws Exception {
		if(attackerTypes.contains(at)) {
			attackerTypeGames.get(at).setPayoffs(psp, p);
		} else {
			throw new Exception("invalid attacker type");
		}
	}
	
	public ProbabilityDistribution<AttackerType> getAttackerTypeProbabilities() {
		return attackerTypeProbabilities;
	}
	
	public PlayerPair<Payoff> getPayoffs(AttackerType at, PureStrategyProfile psp) {
		return attackerTypeGames.get(at).getPayoffs(psp);
	}
	
	@Override
	public String toString() {
		String output = "";
		String delim = "";
		for(Map.Entry<AttackerType, StackelbergGame> entry : attackerTypeGames.entrySet()) {
			AttackerType attackerType = entry.getKey();
			output += delim + "P(" + attackerType + ")=" + attackerTypeProbabilities.getProbability(attackerType) + "\n" + entry.getValue();
			delim = "\n";
		}
		return output;
	}
	
}
