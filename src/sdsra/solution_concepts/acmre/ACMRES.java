package sdsra.solution_concepts.acmre;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.HashMap;
import java.util.Map;

import sdsra.AttackerType;
import sdsra.BayesianStackelbergGame;
import sdsra.MixedStrategy;
import sdsra.Payoff;
import sdsra.PureStrategy;
import sdsra.SecurityGame;
import sdsra.pairs.PlayerPair;
import sdsra.pairs.player_pairs.PureStrategyProfile;
import sdsra.solution_concepts.ACMRE;

public class ACMRES extends ACMRE {
	
	private final int M = 10000;
	
	// Inputs.
	private double[] p; // p[l]
	private double[][][] R; // R[l][i][j]
	private double[][][] C; // C[l][i][j]
	
	private int L; // Number of attacker types.
	private int X; // Number of defender pure strategies.
	private int Q; // Number of attacker pure strategies.
	
	private AttackerType[] attackerTypesArray;
	private PureStrategy[] defenderPureStrategiesArray;
	private PureStrategy[] attackerPureStrategiesArray;
	
	// Outputs.
	private double[][] q;
	private double[] x;
	
	public ACMRES(SecurityGame securityGame) {
		this(securityGame.getBayesianStackelbergGame());
	}
	
	public ACMRES(BayesianStackelbergGame bayesianStackelbergGame) {
		L = bayesianStackelbergGame.getAttackerTypeProbabilities().size();
		X = bayesianStackelbergGame.getPureStrategies().getDefender().size();
		Q = bayesianStackelbergGame.getPureStrategies().getAttacker().size();
		
		p = new double[L];
		R = new double[L][X][Q];
		C = new double[L][X][Q];
		
		attackerTypesArray = new AttackerType[L];
		defenderPureStrategiesArray = new PureStrategy[X];
		attackerPureStrategiesArray = new PureStrategy[Q];
		
		int index = 0;
		for(Map.Entry<AttackerType, Double> entry : bayesianStackelbergGame.getAttackerTypeProbabilities().entrySet()) {
			attackerTypesArray[index] = entry.getKey();
			p[index] = entry.getValue();
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
	}
	
	private double[] attacker(int l, double sigma) {
		double[] qvals = new double[Q];
		IloCplex milp = null;
		try {
			milp = new IloCplex();
			milp.setOut(null); // disable console output
			
			IloNumVar[] q = milp.numVarArray(Q, 0, 1); // (9)
			milp.addEq(milp.sum(q, 0, Q), 1); // (8)
			
			IloNumVar[] qprime = milp.numVarArray(Q, 0, 1); // (7)
			milp.addEq(milp.sum(qprime, 0, Q), 1); // (6)
			
			IloNumVar Vl2 = milp.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			double min = Double.POSITIVE_INFINITY;
			for(int i = 0; i < X; i++) {
				for(int j = 0; j < Q; j++) {
					if(C[l][i][j] < min) {
						min = C[l][i][j];
					}
				}
			}
			milp.addEq(Vl2, min); // (5)
			
			IloNumVar Bl2 = milp.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			for(int i = 0; i < X; i++) {
				IloNumExpr sum = null;
				for(int j = 0; j < Q; j++) {
					IloNumExpr prod = milp.prod(C[l][i][j], qprime[j]);
					if(sum == null) {
						sum = prod;
					} else {
						sum = milp.sum(sum, prod);
					}
				}
				milp.addEq(Bl2, sum); // (4)
			}
			
			for(int i = 0; i < X; i++) {
				IloNumExpr left = null;
				for(int j = 0; j < Q; j++) {
					IloNumExpr prod = milp.prod(C[l][i][j], q[j]);
					if(left == null) {
						left = prod;
					} else {
						left = milp.sum(left, prod);
					}
				}
				milp.addGe(left, milp.sum(milp.prod((1 - sigma), Bl2), milp.prod(sigma, Vl2))); // (3)
			}
			
			IloNumVar obj = milp.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			milp.addMinimize(obj); // (1)
			
			for(int i = 0; i < X; i++) {
				IloNumExpr sum = null;
				double max = Double.NEGATIVE_INFINITY;
				for(int j = 0; j < Q; j++) {
					if(C[l][i][j] > max) {
						max = C[l][i][j];
					}
				}
				for(int j = 0; j < Q; j++) {
					IloNumExpr prod = milp.prod(C[l][i][j], q[j]);
					if(sum == null) {
						sum = prod;
					} else {
						sum = milp.sum(sum, prod);
					}
				}
				milp.addGe(obj, milp.diff(max, sum)); // (2)
			}
			
			milp.solve();
			if(milp.getStatus().equals(IloCplex.Status.Optimal)) {
//				System.err.println(attackerTypesArray[l]);
//				System.err.println("objective=" + milp.getObjValue());
			} else if(milp.getStatus().equals(IloCplex.Status.Feasible)) {
				throw new IloException("solution is feasible, not optimal");
			} else if(milp.getStatus().equals(IloCplex.Status.Infeasible)) {
				throw new IloException("solution is infeasible");
			} else {
				throw new IloException("solution error");
			}
			
			qvals = milp.getValues(q);
//			for(int j = 0; j < Q; j++) {
//				System.err.println("p(" + attackerPureStrategiesArray[j] + ")=" + qvals[j]);
//			}
//			System.err.println();
		} catch (IloException e) {
			e.printStackTrace();
		} finally {
			if(milp != null) {
				milp.end();
			}
		}
		return qvals;
	}
	
//	public double[] defender(double sigma, double[][] q) {
//		double[] xvals = new double[X];
//		IloCplex milp = null;
//		try {
//			milp = new IloCplex();
//			milp.setOut(null); // disable console output
//			
//			IloNumVar[] x = milp.boolVarArray(X); // (6)
//			milp.addEq(milp.sum(x, 0, X), 1); // (5)
//			
//			IloNumVar V1 = milp.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//			milp.addEq(V1, this.minimin()); // (4)
//			
//			IloNumVar B1 = milp.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
//			milp.addEq(B1, this.maximin()); // (3)
//			
//			for(int j = 0; j < Q; j++) {
//				IloNumExpr left = null;
//				for(int l = 0; l < L; l++) {
//					for(int i = 0; i < X; i++) {
//						IloNumExpr prod = milp.prod(p[l] * R[l][i][j], x[i]);
//						if(left == null) {
//							left = prod;
//						} else {
//							left = milp.sum(left, prod);
//						}
//					}
//				}
//				milp.addGe(left, milp.sum(milp.prod((1 - sigma), B1), milp.prod(sigma, V1))); // (2)
//			}
//			
//			IloNumExpr obj = null;
//			for(int j = 0; j < Q; j++) {
//				for(int l = 0; l < L; l++) {
//					for(int i = 0; i < X; i++) {
//						IloNumExpr prod = milp.prod(p[l] * R[l][i][j] * q[l][j], x[i]);
//						if(obj == null) {
//							obj = prod;
//						} else {
//							obj = milp.sum(obj, prod);
//						}
//					}
//				}
//			}
//			milp.addMaximize(obj); // (1)
//			
//			milp.solve();
//			if(milp.getStatus().equals(IloCplex.Status.Optimal)) {
//				System.err.println(milp.getObjValue());
//			} else if(milp.getStatus().equals(IloCplex.Status.Feasible)) {
//				throw new IloException("solution is feasible, not optimal");
//			} else if(milp.getStatus().equals(IloCplex.Status.Infeasible)) {
//				throw new IloException("solution is infeasible");
//			} else {
//				throw new IloException("solution error");
//			}
//			
//			xvals = milp.getValues(x);
//			for(int i = 0; i < X; i++) {
//				System.err.println("p(" + attackerPureStrategiesArray[i] + ")=" + xvals[i]);
//			}
//		} catch (IloException e) {
//			e.printStackTrace();
//		} finally {
//			if(milp != null) {
//				milp.end();
//			}
//		}
//		return xvals;
//	}
//	
//	public double maximin() {
//		double val = 0;
//		IloCplex milp = null;
//		try {
//			milp = new IloCplex();
//			milp.setOut(null); // disable console output
//			
//			IloNumVar[] y = milp.boolVarArray(X); // (8)
//			milp.addEq(milp.sum(y, 0, X), 1); // (7)
//			
//			IloNumExpr obj = null;
//			for(int l = 0; l < L; l++) {
//				for(int i = 0; i < X; i++) {
//					double min = Double.POSITIVE_INFINITY;
//					for(int j = 0; j < Q; j++) {
//						if(R[l][i][j] < min) {
//							min = R[l][i][j];
//						}
//					}
//					IloNumExpr prod = milp.prod(p[l] * min, y[i]);
//					if(obj == null) {
//						obj = prod;
//					} else {
//						obj = milp.sum(obj, prod);
//					}
//				}
//			}
//			milp.addMaximize(obj);
//			
//			milp.solve();
//			if(milp.getStatus().equals(IloCplex.Status.Optimal)) {
//				System.err.println(milp.getObjValue());
//			} else if(milp.getStatus().equals(IloCplex.Status.Feasible)) {
//				throw new IloException("solution is feasible, not optimal");
//			} else if(milp.getStatus().equals(IloCplex.Status.Infeasible)) {
//				throw new IloException("solution is infeasible");
//			} else {
//				throw new IloException("solution error");
//			}
//			
//			val = milp.getObjValue();
//			for(int i = 0; i < X; i++) {
//				System.err.println("p(" + attackerPureStrategiesArray[i] + ")=" + milp.getValues(y)[i]);
//			}
//		} catch (IloException e) {
//			e.printStackTrace();
//		} finally {
//			if(milp != null) {
//				milp.end();
//			}
//		}
//		return val;
//	}
//	
//	public double minimin() {
//		double val = 0;
//		IloCplex milp = null;
//		try {
//			milp = new IloCplex();
//			milp.setOut(null); // disable console output
//			
//			IloNumVar[] z = milp.boolVarArray(X); // (8)
//			milp.addEq(milp.sum(z, 0, X), 1); // (7)
//			
//			IloNumExpr obj = null;
//			for(int l = 0; l < L; l++) {
//				for(int i = 0; i < X; i++) {
//					double min = Double.POSITIVE_INFINITY;
//					for(int j = 0; j < Q; j++) {
//						if(R[l][i][j] < min) {
//							min = R[l][i][j];
//						}
//					}
//					IloNumExpr prod = milp.prod(p[l] * min, z[i]);
//					if(obj == null) {
//						obj = prod;
//					} else {
//						obj = milp.sum(obj, prod);
//					}
//				}
//			}
//			milp.addMinimize(obj);
//			
//			milp.solve();
//			if(milp.getStatus().equals(IloCplex.Status.Optimal)) {
//				System.err.println(milp.getObjValue());
//			} else if(milp.getStatus().equals(IloCplex.Status.Feasible)) {
//				throw new IloException("solution is feasible, not optimal");
//			} else if(milp.getStatus().equals(IloCplex.Status.Infeasible)) {
//				throw new IloException("solution is infeasible");
//			} else {
//				throw new IloException("solution error");
//			}
//			
//			val = milp.getObjValue();
//			for(int i = 0; i < X; i++) {
//				System.err.println("p(" + attackerPureStrategiesArray[i] + ")=" + milp.getValues(z)[i]);
//			}
//		} catch (IloException e) {
//			e.printStackTrace();
//		} finally {
//			if(milp != null) {
//				milp.end();
//			}
//		}
//		return val;
//	}
	
	private double[] defender(double sigma, double[][] q) {
		double[] xvals = new double[X];
		IloCplex milp = null;
		try {
			milp = new IloCplex();
			milp.setOut(null); // disable console output
			
			IloNumVar[] xprime = milp.boolVarArray(X); // (8)
			milp.addEq(milp.sum(xprime, 0, X), 1); // (7)
			
			IloNumVar[] x = milp.boolVarArray(X); // (6)
			milp.addEq(milp.sum(x, 0, X), 1); // (5)
			
			IloNumVar V1 = milp.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			for(int i = 0; i < X; i++) {
				double sum = 0;
				for(int l = 0; l < L; l++) {
					double min = Double.POSITIVE_INFINITY;
					for(int j = 0; j < Q; j++) {
						if(R[l][i][j] < min) {
							min = R[l][i][j];
						}
					}
					sum += p[l] * min;
				}
				milp.addLe(V1, sum); // (4)
			}
			
			IloNumVar B1 = milp.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			for(int i = 0; i < X; i++) {
				double sum = 0; // try using CPLEX object
				for(int l = 0; l < L; l++) {
					double min = Double.POSITIVE_INFINITY;
					for(int j = 0; j < Q; j++) {
						if(R[l][i][j] < min) {
							min = R[l][i][j];
						}
					}
					sum += p[l] * min;
				}
				IloNumExpr middle = milp.diff(B1, sum);
				milp.addGe(middle, 0);
				milp.addLe(middle, milp.prod(M, milp.diff(1, xprime[i]))); // (3)
			}
			
			for(int j = 0; j < Q; j++) {
				IloNumExpr left = null;
				for(int l = 0; l < L; l++) {
					for(int i = 0; i < X; i++) {
						IloNumExpr prod = milp.prod(p[l] * R[l][i][j], x[i]);
						if(left == null) {
							left = prod;
						} else {
							left = milp.sum(left, prod);
						}
					}
				}
				milp.addGe(left, milp.sum(milp.prod((1 - sigma), B1), milp.prod(sigma, V1))); // (2)
			}
			
			IloNumExpr obj = null;
			for(int j = 0; j < Q; j++) {
				for(int l = 0; l < L; l++) {
					for(int i = 0; i < X; i++) {
						IloNumExpr prod = milp.prod(p[l] * R[l][i][j] * q[l][j], x[i]);
						if(obj == null) {
							obj = prod;
						} else {
							obj = milp.sum(obj, prod);
						}
					}
				}
			}
			milp.addMaximize(obj); // (1)
			
			milp.solve();
			if(milp.getStatus().equals(IloCplex.Status.Optimal)) {
//				System.err.println("defender");
//				System.err.println("objective=" + milp.getObjValue());
			} else if(milp.getStatus().equals(IloCplex.Status.Feasible)) {
				throw new IloException("solution is feasible, not optimal");
			} else if(milp.getStatus().equals(IloCplex.Status.Infeasible)) {
				throw new IloException("solution is infeasible");
			} else {
				throw new IloException("solution error");
			}
			
			xvals = milp.getValues(x);
//			for(int i = 0; i < X; i++) {
//				System.err.println("p(" + attackerPureStrategiesArray[i] + ")=" + xvals[i]);
//			}
		} catch (IloException e) {
			e.printStackTrace();
		} finally {
			if(milp != null) {
				milp.end();
			}
		}
		return xvals;
	}
	
	public void solve(double defenderSigma, Map<AttackerType, Double> attackerSigmas) {
		q = new double[L][Q];
		for(int l = 0; l < L; l++) {
			q[l] = this.attacker(l, attackerSigmas.get(attackerTypesArray[l]));
		}
		x = this.defender(defenderSigma, q);
	}
	
	public PureStrategy getDefenderOptimalPureStrategy() throws Exception {
		for(int i = 0; i < X; i++) {
			if(x[i] > 0.5) {
				return defenderPureStrategiesArray[i];
			}
		}
		throw new Exception("invalid pure strategy");
	}
	
	public Map<AttackerType, MixedStrategy> getAttackerOptimalMixedStrategies() throws Exception {
		Map<AttackerType, MixedStrategy> attackerMixedStrategies = new HashMap<AttackerType, MixedStrategy>();
		for(int l = 0; l < L; l++) {
			MixedStrategy attackerMixedStrategy = new MixedStrategy();
			for(int j = 0; j < Q; j++) {
				attackerMixedStrategy.setProbability(attackerPureStrategiesArray[j], q[l][j]);
			}
			attackerMixedStrategies.put(attackerTypesArray[l], attackerMixedStrategy);
		}
		return attackerMixedStrategies;
	}
	
}
