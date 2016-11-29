package evaluation;

import java.util.HashMap;
import java.util.Map;

import sdsra.AttackerType;
import sdsra.MixedStrategy;
import sdsra.Payoff;
import sdsra.PureStrategy;
import sdsra.SecurityGame;
import sdsra.Target;
import sdsra.pairs.PlayerPair;
import sdsra.pairs.TargetPair;
import sdsra.payoffs.IntegerPayoff;
import sdsra.solution_concepts.acmre.ACMRES;
import data_structures.AdvancedSet;
import data_structures.ProbabilityDistribution;

public class Scenario {
	
	public static void acmre(double defenderThreshold, double terroristThreshold, double armedRobberThreshold, double pickpocketThreshold) {
		try {
			Target sm = new Target("SM");
			Target fce = new Target("FCE");
			Target h = new Target("H");
			AdvancedSet<Target> targets = new AdvancedSet<Target>(sm, fce, h);
			
			AttackerType terrorist = new AttackerType("terrorist");
			AttackerType armedRobber = new AttackerType("armed robber");
			AttackerType pickpocket = new AttackerType("pickpocket");
			ProbabilityDistribution<AttackerType> attackerTypeProbabilities = new ProbabilityDistribution<AttackerType>();
			attackerTypeProbabilities.setProbability(terrorist, 0.2);
			attackerTypeProbabilities.setProbability(armedRobber, 0.3);
			attackerTypeProbabilities.setProbability(pickpocket, 0.5);
			
			SecurityGame securityGame = new SecurityGame(targets, attackerTypeProbabilities);
			
			securityGame.setPayoffs(terrorist, sm, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(7), 
									new IntegerPayoff(-7)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-8), 
									new IntegerPayoff(9)
							)
					)
			);
			
			securityGame.setPayoffs(terrorist, fce, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(4), 
									new IntegerPayoff(-2)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-3), 
									new IntegerPayoff(3)
							)
					)
			);
			
			securityGame.setPayoffs(terrorist, h, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(6), 
									new IntegerPayoff(-6)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-6), 
									new IntegerPayoff(5)
							)
					)
			);
			
			securityGame.setPayoffs(armedRobber, sm, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(5), 
									new IntegerPayoff(-5)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-4), 
									new IntegerPayoff(5)
							)
					)
			);
			
			securityGame.setPayoffs(armedRobber, fce, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(7), 
									new IntegerPayoff(-8)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-6), 
									new IntegerPayoff(7)
							)
					)
			);
			
			securityGame.setPayoffs(armedRobber, h, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(6), 
									new IntegerPayoff(-5)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-5), 
									new IntegerPayoff(5)
							)
					)
			);
			
			securityGame.setPayoffs(pickpocket, sm, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(5), 
									new IntegerPayoff(-8)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-5), 
									new IntegerPayoff(7)
							)
					)
			);
			
			securityGame.setPayoffs(pickpocket, fce, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(5), 
									new IntegerPayoff(-7)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-4), 
									new IntegerPayoff(6)
							)
					)
			);
			
			securityGame.setPayoffs(pickpocket, h, 
					new TargetPair<PlayerPair<Payoff>>(
							new PlayerPair<Payoff>(
									new IntegerPayoff(7), 
									new IntegerPayoff(-7)
							), 
							new PlayerPair<Payoff>(
									new IntegerPayoff(-6), 
									new IntegerPayoff(6)
							)
					)
			);
			
			System.out.println(securityGame);
			
			Map<AttackerType, Double> attackerThresholds = new HashMap<AttackerType, Double>();
			attackerThresholds.put(terrorist, terroristThreshold);
			attackerThresholds.put(armedRobber, armedRobberThreshold);
			attackerThresholds.put(pickpocket, pickpocketThreshold);
			
			ACMRES acrmes = new ACMRES(securityGame.getBayesianStackelbergGame());
			acrmes.solve(defenderThreshold, attackerThresholds);
			
			PureStrategy defendersOptimalStrategy = acrmes.getDefenderOptimalPureStrategy();
			Map<AttackerType, MixedStrategy> attackersOptimalStrategy = acrmes.getAttackerOptimalMixedStrategies();
			
			System.out.println("defender's optimal strategy: " + defendersOptimalStrategy);
			System.out.println("attacker's optimal strategy: " + attackersOptimalStrategy);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		acmre(0.5, 0.8, 0.5, 0.2);
	}
	
}
