package sdsra.solution_concepts.sse;

import java.util.HashMap;
import java.util.Map;

import sdsra.AttackerType;
import sdsra.BayesianStackelbergGame;
import sdsra.MixedStrategy;
import sdsra.Payoff;
import sdsra.PureStrategy;
import sdsra.pairs.PlayerPair;
import sdsra.pairs.player_pairs.PureStrategyProfile;
import sdsra.solution_concepts.SSE;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * created by matyama
 */
public class DOBSS extends SSE {
	
	private final int M_VALUE = 100000;

	// parameters

	/**
	 * p[l]
	 */
	private double[] attackerTypeProbabilities;

	/**
	 * R[l][i][j]
	 */
	private double[][][] R;

	/**
	 * C[l][i][j]
	 */
	private double[][][] C;

	private int L;
	private int X;
	private int Q;
	
	// references
	AttackerType[] attackerTypesArray;
	private PureStrategy[] defenderPureStrategiesArray;
	private PureStrategy[] attackerPureStrategiesArray;

	// results
	private double[] z;
	private double[] x;
	private double[] q;
	private double[] a;

	// evaluation
	private double reward;
	@SuppressWarnings("unused")
	private double cost;
	
public DOBSS(BayesianStackelbergGame bayesianStackelbergGame) {
		
		try {
			L = bayesianStackelbergGame.getAttackerTypeProbabilities().size(); // Type of attacker
			X = bayesianStackelbergGame.getPureStrategies().getDefender().size(); // Defender strategy
			Q = bayesianStackelbergGame.getPureStrategies().getAttacker().size(); // Attacker strategy
			
			assert L > 0 : "L must be positive integer";
			assert X > 0 : "X must be positive integer";
			assert Q > 0 : "Q must be positive integer";
			
			attackerTypeProbabilities = new double[L];
			R = new double[L][X][Q];
			C = new double[L][X][Q];
			
			attackerTypesArray = new AttackerType[L];
			defenderPureStrategiesArray = new PureStrategy[X];
			attackerPureStrategiesArray = new PureStrategy[Q];
			
			int index = 0;
			for(Map.Entry<AttackerType, Double> entry : bayesianStackelbergGame.getAttackerTypeProbabilities().entrySet()) {
				attackerTypesArray[index] = entry.getKey();
				attackerTypeProbabilities[index] = entry.getValue();
				index++;
			}
			
			index = 0;
			for(PureStrategy pureStrategy : bayesianStackelbergGame.getPureStrategies().getDefender()) {
				defenderPureStrategiesArray[index] = pureStrategy;
				index++;
			}
			
			index = 0;
			for(PureStrategy pureStrategy : bayesianStackelbergGame.getPureStrategies().getAttacker()) {
				attackerPureStrategiesArray[index] = pureStrategy;
				index++;
			}
			
			// R[l][i][j] and C[l][i][j]
			for(int l = 0; l < L; l++) {
				for(int i = 0; i < X; i++) {
					for(int j = 0; j < Q; j++) {
						PureStrategyProfile psp = new PureStrategyProfile(defenderPureStrategiesArray[i], attackerPureStrategiesArray[j]);
						PlayerPair<Payoff> payoff = bayesianStackelbergGame.getPayoffs(attackerTypesArray[l], psp);
						R[l][i][j] = payoff.getDefender().getDouble();
						C[l][i][j] = payoff.getAttacker().getDouble();
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * method solving a DOBSS game
	 * @throws Exception 
	 */
	public void solve() throws Exception {

		IloCplex milp = null;

		try {

			milp = new IloCplex();

			milp.setOut(null); // disable console output

			IloNumVar[] zvar = milp.numVarArray(L * X * Q, 0, 1, getZNames());

			IloNumVar[] qvar = milp.boolVarArray(L * Q, getQNames());

			IloNumVar[] avar = milp.numVarArray(L, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, getANames());

			constructMILP(milp, zvar, qvar, avar);

			milp.solve();

			if(IloCplex.Status.Optimal.equals(milp.getStatus())) {
				// do nothing
			} else if(IloCplex.Status.Feasible.equals(milp.getStatus())) {
				throw new Exception("solution is feasible, not optimal");
			} else if(IloCplex.Status.Infeasible.equals(milp.getStatus())) {
				throw new Exception("solution is infeasible");
			} else {
				throw new Exception("solution error");
			}

			z = milp.getValues(zvar);
			x = extractStrategy(z);
			q = milp.getValues(qvar);
			a = milp.getValues(avar);

			reward = milp.getBestObjValue();
			cost = dot(a, attackerTypeProbabilities);

		} catch (IloException e) {
			e.printStackTrace();
		} finally {
			if(milp != null) {
				milp.end();
			}
		}

	}
	
	public MixedStrategy getDefenderMixedStrategy() throws Exception {
		MixedStrategy defenderMixedStrategy = new MixedStrategy();
		for(int i = 0; i < X; i++) {
			defenderMixedStrategy.setProbability(defenderPureStrategiesArray[i], x[i]);
		}
		return defenderMixedStrategy;
	}
	
	public Map<AttackerType, PureStrategy> getAttackerPureStrategies() {
		Map<AttackerType, PureStrategy> attackerPureStrategies = new HashMap<AttackerType, PureStrategy>();
		for(int l = 0; l < L; l++) {
			for(int j = 0; j < Q; j++) {
				if(q[(l*Q)+j] > 0.5) {
					attackerPureStrategies.put(attackerTypesArray[l], attackerPureStrategiesArray[j]);
				}
			}
		}
		return attackerPureStrategies;
	}
	
	public double getDefenderMaxEU() {
		return reward;
	}

	private String[] getQNames() {
		String[] names = new String[L*Q];
		for(int l = 0; l < L; l++) {
			for(int j = 0; j < Q; j++) {
				names[l*Q+j] = "q" + (L > 1 ? "[" + l + "]" : "") + "[" + j + "]";
			}
		}
		return names;
	}

	private String[] getZNames() {
		String[] names = new String[L*X*Q];
		for(int l = 0; l < L; l++) {
			for(int i = 0; i < X; i++) {
				for(int j = 0; j < Q; j++) {
					names[l*X*Q+i*Q+j] = "z" + (L > 1 ? "[" + l + "]" : "") + "[" + i + "]" + "[" + j + "]";
				}
			}
		}
		return names;
	}

	private String[] getANames() {
		String[] names = new String[L];
		for(int l = 0; l < L; l++) {
			names[l] = "a" + (L > 1 ? "[" + l + "]" : "");
		}
		return names;
	}

	private void constructMILP(IloCplex milp, IloNumVar[] z, IloNumVar[] q, IloNumVar[] a) throws IloException {

		IloNumExpr[][] sums3 = new IloNumExpr[Q][L];
		IloNumExpr[][] sums5 = new IloNumExpr[Q][L];
		IloNumExpr sum256, sum5;

		double[][] msum = new double[L][Q];

		IloLinearNumExpr obj = milp.linearNumExpr();

		for(int l = 0; l < L; l++) {
			for(int i = 0; i < X; i++) {

				for(int j = 0; j < Q; j++) { // (ad 3)
					if(sums3[j][l] == null) {
						sums3[j][l] = z[l*X*Q+i*Q+j];
					} else {
						sums3[j][l] = milp.sum(sums3[j][l], z[l*X*Q+i*Q+j]);
					}
				}

				sum256 = milp.sum(z, l*X*Q+i*Q, Q);

				for(int j = 0; j < Q; j++) {
					msum[l][j] += C[l][i][j]; // (ad M)

					obj.addTerm(attackerTypeProbabilities[l]*R[l][i][j], z[l*X*Q+i*Q+j]); // (ad 0)

					if(sums5[j][l] == null) { // (ad 5)
						sums5[j][l] = milp.prod(C[l][i][j], sum256);
					} else {
						sums5[j][l] = milp.sum(sums5[j][l], milp.prod(C[l][i][j], sum256));
					}
				}

				milp.addLe(sum256, 1); // (2)

				milp.addEq(sum256, milp.sum(z, i*Q, Q)); // (6)
			}

			milp.addEq(milp.sum(z, l*X*Q, X*Q), 1); // (1)

			milp.addEq(milp.sum(q, l*Q, Q), 1); // (4)

		}

		milp.addMaximize(obj); // (0)

		IloNumExpr M = milp.constant(M_VALUE);

		for(int l = 0; l < L; l++) {
			for(int j = 0; j < Q; j++) {
				milp.addLe(q[l*Q+j], sums3[j][l]); // (3a)
				milp.addLe(sums3[j][l], 1); // (3b)
				sum5 = milp.sum(a[l],milp.negative(sums5[j][l]));
				milp.addLe(0, sum5); // (5a)
				milp.addLe(sum5, milp.prod(milp.sum(1, milp.negative(q[l*Q+j])), M)); // (5b)
			}
		}

	}

	/**
	 * extracts strategy x from variable z
	 * @param zval - resulting values for variable z
	 * @return - x (where x_i = sum_j{z^1_ij}, i.e. x[i] = sum(zval[0][i][j], j)
	 */
	private double[] extractStrategy(double[] zval) {
		double[] xval = new double[X];
		for(int i = 0; i < X; i++) {
			for(int j = 0; j < Q; j++) {
				xval[i] += zval[i*Q+j]; // l = 0
			}
		}
		return xval;
	}
	
	/**
     * dot product of two vectors (i.e. weighted sum or expected number if one represents probabilities)
     * @param x - a vector
     * @param y - a vector
     * @return sum_i{x[i]*y[i]}
     */
	private static double dot(double[] x, double[] y) {
		if(x.length != y.length) {
			return Double.NaN;
		}
		double res = 0d;
		for(int i = 0; i < x.length; i++) {
			res += x[i]*y[i];
		}
		return res;
	}

}
