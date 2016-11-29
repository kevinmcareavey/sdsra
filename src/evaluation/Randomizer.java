package evaluation;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import sdsra.AttackerType;
import sdsra.Payoff;
import sdsra.SecurityGame;
import sdsra.Target;
import sdsra.pairs.PlayerPair;
import sdsra.pairs.TargetPair;
import sdsra.payoffs.IntegerPayoff;
import data_structures.AdvancedSet;
import data_structures.ProbabilityDistribution;

public class Randomizer {
	
	private static Random rand = new Random();
	
	public static AdvancedSet<Target> getTargetSet(int numTargets) {
		AdvancedSet<Target> targets = new AdvancedSet<Target>();
		for(int i = 0; i < numTargets; i++) {
			targets.add(new Target("t" + i));
		}
		return targets;
	}
	
	public static AdvancedSet<AttackerType> getAttackerTypeSet(int numTypes) {
		AdvancedSet<AttackerType> types = new AdvancedSet<AttackerType>();
		for(int i = 0; i < numTypes; i++) {
			types.add(new AttackerType("a" + i));
		}
		return types;
	}
	
	public static <T> ProbabilityDistribution<T> randomProbabilityDistribution(AdvancedSet<T> set) throws Exception {
		ProbabilityDistribution<T> attackerTypes = new ProbabilityDistribution<T>();
		double remaining = 1;
		int i = 0;
		int count = set.size();
		for(T element : set) {
			if(count == 1) {
				attackerTypes.setProbability(element, 1.0);
			} else if(i < count - 1) {
				double random = randomDouble(0, remaining);
				remaining -= random;
				attackerTypes.setProbability(element, random);
			} else {
				attackerTypes.setProbability(element, remaining);
			}
			i++;
		}
		return attackerTypes;
	}
	
	public static double randomDouble(double min, double max) {
		double randomNum = min + (max - min) * rand.nextDouble();
		return randomNum;
	}
	
	public static int randomInteger(int min, int max) {
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
	
	public static SecurityGame randomSecurityGame(int numTargets, int numTypes) throws Exception {
		AdvancedSet<Target> targets = getTargetSet(numTargets);
		AdvancedSet<AttackerType> attackerTypes = getAttackerTypeSet(numTypes);
		ProbabilityDistribution<AttackerType> attackerTypeProbabilities = randomProbabilityDistribution(attackerTypes);
		
		int minLose = -10;
		int maxLose = 0;
		int minWin = 0;
		int maxWin = 10;
		
		SecurityGame securityGame = new SecurityGame(targets, attackerTypes, attackerTypeProbabilities);
		for(AttackerType type : attackerTypes) {
			for(Target target : targets) {
				securityGame.setPayoffs(
						type, 
						target, 
						new TargetPair<PlayerPair<Payoff>>(
								new PlayerPair<Payoff>(
										new IntegerPayoff(randomInteger(minWin, maxWin)), 
										new IntegerPayoff(randomInteger(minLose, maxLose))
								), 
								new PlayerPair<Payoff>(
										new IntegerPayoff(randomInteger(minLose, maxLose)), 
										new IntegerPayoff(randomInteger(minWin, maxWin))
								)
						)
				);
			}
		}
		
		return securityGame;
	}
	
	public static Map<AttackerType, Double> randomSigmas(AdvancedSet<AttackerType> attackerTypes) {
		Map<AttackerType, Double> attackerSigmas = new HashMap<AttackerType, Double>();
		for(AttackerType attackerType : attackerTypes) {
			attackerSigmas.put(attackerType, randomDouble(0, 1));
		}
		return attackerSigmas;
	}
	
}
