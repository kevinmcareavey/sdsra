package evaluation;

import data_structures.AdvancedSet;
import data_structures.ProbabilityDistribution;
import sdsra.AttackerType;
import sdsra.BayesianStackelbergGame;
import sdsra.Payoff;
import sdsra.PureStrategy;
import sdsra.pairs.PlayerPair;
import sdsra.pairs.player_pairs.PureStrategyProfile;
import sdsra.payoffs.IntegerPayoff;
import sdsra.solution_concepts.sse.DOBSS;

public class Example {
	
	public static void main(String[] args) {
		try {
			PureStrategy a = new PureStrategy("A");
			PureStrategy b = new PureStrategy("B");
			PureStrategy x = new PureStrategy("X");
			PureStrategy y = new PureStrategy("Y");
			AttackerType t1 = new AttackerType("T1");
			AttackerType t2 = new AttackerType("T2");
			AdvancedSet<AttackerType> attackerTypes = new AdvancedSet<AttackerType>(t1, t2);
			
			int min = -4;
			int max = 4;
			
			ProbabilityDistribution<AttackerType> completeAttackerTypes = new ProbabilityDistribution<AttackerType>();
			completeAttackerTypes.setProbability(t1, 0.75);
			completeAttackerTypes.setProbability(t2, 0.25);
			BayesianStackelbergGame complete = new BayesianStackelbergGame(new PlayerPair<AdvancedSet<PureStrategy>>(new AdvancedSet<PureStrategy>(a, b), new AdvancedSet<PureStrategy>(x, y)), attackerTypes, completeAttackerTypes);
			complete.setPayoffs(t1, new PureStrategyProfile(a, x), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			complete.setPayoffs(t1, new PureStrategyProfile(a, y), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			complete.setPayoffs(t1, new PureStrategyProfile(b, x), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			complete.setPayoffs(t1, new PureStrategyProfile(b, y), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			
			complete.setPayoffs(t2, new PureStrategyProfile(a, x), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			complete.setPayoffs(t2, new PureStrategyProfile(a, y), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			complete.setPayoffs(t2, new PureStrategyProfile(b, x), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			complete.setPayoffs(t2, new PureStrategyProfile(b, y), new PlayerPair<Payoff>(new IntegerPayoff(Randomizer.randomInteger(min, max)), new IntegerPayoff(Randomizer.randomInteger(min, max))));
			
			System.out.println(complete);
			System.out.println();
			System.out.println("Attacker types: " + completeAttackerTypes);
			
			DOBSS completeDobss = new DOBSS(complete);
			completeDobss.solve();
			
			System.out.println("Defender's strategy: " + completeDobss.getDefenderMixedStrategy());
			System.out.println("Attacker's strategy: " + completeDobss.getAttackerPureStrategies());
			System.out.println("Defender's EU: " + complete.getDefenderEU(completeDobss.getDefenderMixedStrategy(), completeDobss.getAttackerPureStrategies()));
			System.out.println();
			
			ProbabilityDistribution<AttackerType> partialAttackerTypes = new ProbabilityDistribution<AttackerType>();
			partialAttackerTypes.setProbability(t1, 0.25);
			partialAttackerTypes.setProbability(t2, 0.75);
			BayesianStackelbergGame partial = complete;
			complete.setAttackerTypeProbabilities(partialAttackerTypes);
			System.out.println("Attacker types: " + partialAttackerTypes);
			
			DOBSS partialDobss = new DOBSS(partial);
			partialDobss.solve();
			
			System.out.println("Defender's strategy: " + partialDobss.getDefenderMixedStrategy());
			System.out.println("Attacker's strategy: " + partialDobss.getAttackerPureStrategies());
			System.out.println("Defender's EU: " + complete.getDefenderEU(completeDobss.getDefenderMixedStrategy(), partialDobss.getAttackerPureStrategies()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
