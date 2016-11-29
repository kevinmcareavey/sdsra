package evaluation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sdsra.AttackerType;
import sdsra.BayesianStackelbergGame;
import sdsra.PureStrategy;
import sdsra.SecurityGame;
import sdsra.solution_concepts.acmre.ACMRES;
import utilities.Utilities;

public class Experiments {
	
	public static long run(int numTargets, int numAttackerTypes) throws Exception {
		SecurityGame securityGame = Randomizer.randomSecurityGame(numTargets, numAttackerTypes);
		
		ACMRES acmres = new ACMRES(securityGame.getBayesianStackelbergGame());
		Map<AttackerType, Double> attackerSigmas = Randomizer.randomSigmas(securityGame.getAttackerTypes());
		
		long startTime = System.currentTimeMillis();
		acmres.solve(Randomizer.randomDouble(0, 1), attackerSigmas);
		
		return System.currentTimeMillis() - startTime;
	}
	
	public static void scalability() {
		try {
			int samples = 1000;
			int attackerTypes, targets;
			
			attackerTypes = 1;
			targets = 2;
			while(targets * targets * attackerTypes <= 100000) {
				long totalTime = 0;
				for(int i = 0; i < samples; i++) {
					totalTime += run(targets, attackerTypes);
				}
				double averageTime = (double)totalTime / (double)samples;
				System.out.println(targets + "," + attackerTypes + "," + (targets * targets * attackerTypes) + "," + samples + "," + averageTime);
				targets++;
			}
			
			attackerTypes = 1;
			targets = 2;
			while(targets * targets * attackerTypes <= 1200) {
				long totalTime = 0;
				for(int i = 0; i < samples; i++) {
					totalTime += run(targets, attackerTypes);
				}
				double averageTime = (double)totalTime / (double)samples;
				System.out.println(targets + "," + attackerTypes + "," + (targets * targets * attackerTypes) + "," + samples + "," + averageTime);
				attackerTypes++;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void quality() {
		try {
			SecurityGame securityGame = Randomizer.randomSecurityGame(50, 1);
			BayesianStackelbergGame bsg = securityGame.getBayesianStackelbergGame();
			
			int denominator = 20;
			List<Double> defenderSigmaList = new ArrayList<Double>();
			for(int i = 0; i <= denominator; i++) {
				double defenderSigma = ((double)1 / (double)denominator) * (double)i;
				defenderSigmaList.add(defenderSigma);
			}
			List<Map<AttackerType, Double>> attackerSigmasList = new ArrayList<Map<AttackerType, Double>>();
			for(int j = 0; j <= denominator; j++) {
				Map<AttackerType, Double> attackerSigmas = new HashMap<AttackerType, Double>();
				for(AttackerType attackerType : securityGame.getAttackerTypes()) {
					double attackerTypeSigma = ((double)1 / (double)denominator) * (double)j;
					attackerSigmas.put(attackerType, attackerTypeSigma);
				}
				attackerSigmasList.add(attackerSigmas);
			}
			
			for(Map<AttackerType, Double> attackerSigmas : attackerSigmasList) {
				System.out.print(";" + Utilities.format(attackerSigmas));
			}
			for(double defenderSigma : defenderSigmaList) {
				System.out.print("\n" + Utilities.format(defenderSigma));
				for(Map<AttackerType, Double> attackerSigmas : attackerSigmasList) {
					ACMRES acmres = new ACMRES(bsg);
					acmres.solve(defenderSigma, attackerSigmas);
					PureStrategy minimaxRegretStrategy = acmres.getDefenderOptimalPureStrategy();
					double minimaxRegretEU = bsg.getDefenderEU(minimaxRegretStrategy, acmres.getAttackerOptimalMixedStrategies());
					
					System.out.print(";" + Utilities.format(minimaxRegretEU));
					System.out.print(";" + minimaxRegretStrategy);
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		scalability();
		quality();
	}
	
}
