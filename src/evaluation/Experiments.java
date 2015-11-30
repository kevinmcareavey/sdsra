package evaluation;

import java.util.HashMap;
import java.util.Map;

import sdsra.AttackerType;
import sdsra.SecurityGame;
import sdsra.solution_concepts.acmre.ACMRES;

public class Experiments {
	
	public static long run(int numTargets, int numAttackerTypes) throws Exception {
		SecurityGame securityGame = Randomizer.randomSecurityGame(numTargets, numAttackerTypes);
		
		ACMRES acmres = new ACMRES(securityGame);
		Map<AttackerType, Double> attackerSigmas = new HashMap<AttackerType, Double>();
		for(AttackerType attackerType : securityGame.getAttackerTypes()) {
			attackerSigmas.put(attackerType, Randomizer.randomDouble(0, 1));
		}
		
		long startTime = System.currentTimeMillis();
		acmres.solve(Randomizer.randomDouble(0, 1), attackerSigmas);
		
		return System.currentTimeMillis() - startTime;
	}
	
	public static void experiment() {
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
	
	public static void main(String[] args) {
		experiment();
	}
	
}
