package sdsra;

import java.util.HashMap;
import java.util.Map;

import sdsra.pairs.PlayerPair;
import sdsra.pairs.player_pairs.PureStrategyProfile;
import utilities.Utilities;
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
	
	public void setAttackerTypeProbabilities(ProbabilityDistribution<AttackerType> atp) {
		attackerTypeProbabilities = atp;
	}
	
	public ProbabilityDistribution<AttackerType> getAttackerTypeProbabilities() {
		return attackerTypeProbabilities;
	}
	
	public PlayerPair<Payoff> getPayoffs(AttackerType at, PureStrategyProfile psp) {
		return attackerTypeGames.get(at).getPayoffs(psp);
	}
	
	public double getDefenderEU(PureStrategy defenderStrategy, Map<AttackerType, MixedStrategy> attackerStrategies) {
		double sum = 0;
		for(Map.Entry<AttackerType, MixedStrategy> entryOuter : attackerStrategies.entrySet()) {
			AttackerType attackerType = entryOuter.getKey();
			double attackerTypeProbability = attackerTypeProbabilities.getProbability(attackerType);
			MixedStrategy attackerTypeMixedStrategy = entryOuter.getValue();
			for(Map.Entry<PureStrategy, Double> entryInner : attackerTypeMixedStrategy.entrySet()) {
				PureStrategy attackerTypePureStrategy = entryInner.getKey();
				double attackerTypePureStrategyProbability = entryInner.getValue();
				PureStrategyProfile psp = new PureStrategyProfile(defenderStrategy, attackerTypePureStrategy);
				double payoff = this.getPayoffs(attackerType, psp).getDefender().getDouble();
				sum += attackerTypeProbability * attackerTypePureStrategyProbability * payoff;
			}
		}
		return sum;
	}
	
	public double getDefenderMinEU(PureStrategy defenderStrategy) {
		double minEU = 0;
		for(Map.Entry<AttackerType, StackelbergGame> entryOuter : attackerTypeGames.entrySet()) {
			AttackerType attackerType = entryOuter.getKey();
			double probability = attackerTypeProbabilities.getProbability(attackerType);
			StackelbergGame stackelbergGame = entryOuter.getValue();
			double minPayoff = Double.POSITIVE_INFINITY;
			for(PureStrategy attackerStrategy : this.getPureStrategies().getAttacker()) {
				PureStrategyProfile psp = new PureStrategyProfile(defenderStrategy, attackerStrategy);
				double payoff = stackelbergGame.getPayoffs(psp).getDefender().getDouble();
				if(payoff < minPayoff) {
					minPayoff = payoff;
				}
			}
			minEU += probability * minPayoff;
		}
		return minEU;
	}
	
	public double getDefenderMaxEU(PureStrategy defenderStrategy) {
		double maxEU = 0;
		for(Map.Entry<AttackerType, StackelbergGame> entryOuter : attackerTypeGames.entrySet()) {
			AttackerType attackerType = entryOuter.getKey();
			double probability = attackerTypeProbabilities.getProbability(attackerType);
			StackelbergGame stackelbergGame = entryOuter.getValue();
			double maxPayoff = Double.NEGATIVE_INFINITY;
			for(PureStrategy attackerStrategy : this.getPureStrategies().getAttacker()) {
				PureStrategyProfile psp = new PureStrategyProfile(defenderStrategy, attackerStrategy);
				double payoff = stackelbergGame.getPayoffs(psp).getDefender().getDouble();
				if(payoff > maxPayoff) {
					maxPayoff = payoff;
				}
			}
			maxEU += probability * maxPayoff;
		}
		return maxEU;
	}
	
	public double getAttackerMinEU(AttackerType attackerType, PureStrategy attackerStrategy) {
		double minEU = Double.POSITIVE_INFINITY;
		StackelbergGame stackelbergGame = attackerTypeGames.get(attackerType);
		for(PureStrategy defenderStrategy : this.getPureStrategies().getDefender()) {
			PureStrategyProfile psp = new PureStrategyProfile(defenderStrategy, attackerStrategy);
			double payoff = stackelbergGame.getPayoffs(psp).getAttacker().getDouble();
			if(payoff < minEU) {
				minEU = payoff;
			}
		}
		return minEU;
	}
	
	public double getAttackerMaxEU(AttackerType attackerType, PureStrategy attackerStrategy) {
		double maxEU = Double.NEGATIVE_INFINITY;
		StackelbergGame stackelbergGame = attackerTypeGames.get(attackerType);
		for(PureStrategy defenderStrategy : this.getPureStrategies().getDefender()) {
			PureStrategyProfile psp = new PureStrategyProfile(defenderStrategy, attackerStrategy);
			double payoff = stackelbergGame.getPayoffs(psp).getAttacker().getDouble();
			if(payoff > maxEU) {
				maxEU = payoff;
			}
		}
		return maxEU;
	}
	
	public double getDefenderEU(MixedStrategy defenderStrategy, Map<AttackerType, PureStrategy> attackerStrategy) {
		double sum = 0;
		for(Map.Entry<PureStrategy, Double> defenderStrategyEntry : defenderStrategy.entrySet()) {
			PureStrategy defenderPureStrategy = defenderStrategyEntry.getKey();
			double defenderPureStrategyProbability = defenderStrategyEntry.getValue();
			for(Map.Entry<AttackerType, PureStrategy> attackerStrategyEntry : attackerStrategy.entrySet()) {
				AttackerType attackerType = attackerStrategyEntry.getKey();
				PureStrategy attackerPureStrategy = attackerStrategyEntry.getValue();
				sum += attackerTypeProbabilities.getProbability(attackerType) * defenderPureStrategyProbability 
						* this.getPayoffs(attackerType, new PureStrategyProfile(defenderPureStrategy, attackerPureStrategy)).getDefender().getDouble();
			}
		}
		return sum;
	}
	
	@Override
	public String toString() {
		String output = "";
		String delim = "";
		for(Map.Entry<AttackerType, StackelbergGame> entry : attackerTypeGames.entrySet()) {
			AttackerType attackerType = entry.getKey();
			output += delim + "P(" + attackerType + ")=" + Utilities.format(attackerTypeProbabilities.getProbability(attackerType)) + "\n" + entry.getValue();
			delim = "\n";
		}
		return output;
	}
	
}
