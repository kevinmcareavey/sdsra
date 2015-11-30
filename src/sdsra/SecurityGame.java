package sdsra;

import java.util.HashMap;
import java.util.Map;

import sdsra.pairs.PlayerPair;
import sdsra.pairs.TargetPair;
import sdsra.pairs.player_pairs.PureStrategyProfile;
import data_structures.AdvancedSet;
import data_structures.ProbabilityDistribution;

public class SecurityGame {
	
	private AdvancedSet<Target> targets;
	private AdvancedSet<AttackerType> attackerTypes;
	private BayesianStackelbergGame game;
	private Map<AttackerType, Map<Target, TargetPair<PlayerPair<Payoff>>>> payoffs;
	
	public SecurityGame(AdvancedSet<Target> t, AdvancedSet<AttackerType> a, ProbabilityDistribution<AttackerType> pd) {
		targets = t;
		attackerTypes = a;
		game = new BayesianStackelbergGame(this.getPureStrategies(), attackerTypes, pd);
		payoffs = new HashMap<AttackerType, Map<Target, TargetPair<PlayerPair<Payoff>>>>();
		for(AttackerType type : attackerTypes) {
			payoffs.put(type, new HashMap<Target, TargetPair<PlayerPair<Payoff>>>());
		}
	}
	
	public SecurityGame(AdvancedSet<Target> t, ProbabilityDistribution<AttackerType> pd) {
		this(t, pd.getFrame(), pd);
	}
	
	public PlayerPair<AdvancedSet<PureStrategy>> getPureStrategies() {
		AdvancedSet<PureStrategy> pureStrategies = new AdvancedSet<PureStrategy>();
		for(Target target : targets) {
			pureStrategies.add(target.getPureStrategy());
		}
		return new PlayerPair<AdvancedSet<PureStrategy>>(pureStrategies, pureStrategies);
	}
	
	public void setPayoffs(AttackerType at, Target t, TargetPair<PlayerPair<Payoff>> p) throws Exception {
		if(attackerTypes.contains(at)) {
			if(targets.contains(t)) {
				payoffs.get(at).put(t, p);
				for(Target target : targets) {
					PureStrategyProfile psp = new PureStrategyProfile(target.getPureStrategy(), t.getPureStrategy());
					if(psp.getLeft().equals(psp.getRight())) {
						game.setPayoffs(at, psp, p.getCovered());
					} else {
						game.setPayoffs(at, psp, p.getUncovered());
					}
				}
			} else {
				throw new Exception("invalid target");
			}
		} else {
			throw new Exception("invalid attacker type");
		}
	}
	
	public BayesianStackelbergGame getBayesianStackelbergGame() {
		return game;
	}
	
	public ProbabilityDistribution<AttackerType> getAttackerTypeProbabilities() {
		return game.getAttackerTypeProbabilities();
	}
	
	public AdvancedSet<Target> getTargets() {
		return targets;
	}
	
	public AdvancedSet<AttackerType> getAttackerTypes() {
		return attackerTypes;
	}
	
	@Override
	public String toString() {
		String output = "";
		String delimOuter = "";
		for(Map.Entry<AttackerType, Map<Target, TargetPair<PlayerPair<Payoff>>>> entryOuter : payoffs.entrySet()) {
			AttackerType attackerType = entryOuter.getKey();
			output += delimOuter + "P(" + attackerType + ")=" + game.getAttackerTypeProbabilities().getProbability(attackerType) + "\n";
			String delimInner = "";
			for(Map.Entry<Target, TargetPair<PlayerPair<Payoff>>> entryInner : entryOuter.getValue().entrySet()) {
				output += delimInner + entryInner.getKey() + ":=" + entryInner.getValue();
				delimInner = "\n";
			}
			delimOuter = "\n";
		}
		return output;
	}
	
}
