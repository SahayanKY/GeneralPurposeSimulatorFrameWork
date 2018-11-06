package simulation.solver;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.function.IntConsumer;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class LUTest {
	static class SolverConstructor{
		Class<? extends LinearEquationSolver> solverclass;
		Object[] parameter;
		Class<?>[] parameterType;
		SolverConstructor(Class<? extends LinearEquationSolver> solverclass, Class<?>[] parameterType, Object[] parameter){
			this.solverclass = solverclass;
			this.parameter = parameter;
			this.parameterType = parameterType;
		}
	}

	@RunWith(Theories.class)
	public static class RepeatSolveTest{
		@DataPoints
		public static double[][][][] As = {
				{
					//3回連続で解く時の係数行列
					{
						{1,0,0},
						{0,1,0},
						{0,0,1}
					},
					{
						{0,2,1},
						{4,5,2},
						{1,3,0}
					},
					{
						{2,5,6},
						{7,2,5},
						{10,-4,-3}
					}
				}
		};

		@DataPoints
		public static SolverConstructor[] solvers = {
				new SolverConstructor(
						LU.class,
						new Class<?>[] {
							boolean.class
						},
						new Object[] {
							true
						}),
				new SolverConstructor(
						LU.class,
						new Class<?>[] {
							boolean.class
						},
						new Object[] {
							false
						})
		};

		@DataPoints
		public static double[][][] Bs = {
				{
					//3回連続で解く
					{2,5,2},
					{3,4,7},
					{1,0,3}
				}
		};

		@DataPoints
		//3回解くに当たっての分岐
		//trueなら係数行列はnull
		public static boolean[][] bools = {
				{true,true,true},
				{true,true,false},
				{true,false,true},
				{true,false,false},
				{false,true,true},
				{false,true,false},
				{false,false,true},
				{false,false,false}
		};

		@Test
		@Theory
		/**
		 * 係数行列や右辺項ベクトルを変化させない解き方でのテストメソッド
		 * */
		public void testSolve_NotChangeArrayAndSeveralTime(SolverConstructor solverconstructor, double[][][] As,double[][] bs,boolean[] AisNull) {
			try {
				LinearEquationSolver solver
					= solverconstructor.solverclass
							.getConstructor(solverconstructor.parameterType)
							.newInstance(solverconstructor.parameter);

				solver.changeArray(false);
				//配列が本当に変化していないのかを1行分だけ調べる
				double[][] Atest = new double[As.length][As[0].length];
				double[] Btest = new double[bs.length];
				//As.length==bs.length

				assertEquals(As.length,bs.length);

				for(int i=0;i<As.length;i++) {
					for(int j=0;j<As[0].length;j++) {
						Atest[i][j] = As[i][0][j];
					}
					Btest[i] = bs[i][0];
				}

				//連続で解く
				repeatSolve(solver,As,bs,AisNull);

				//配列の変化が実際に起こっていないかをチェック
				for(int i=0;i<As.length;i++) {
					for(int j=0;j<As[0].length;j++) {
						assertEquals(Atest[i][j],As[i][0][j],0.0);
					}
					assertEquals(Btest[i],bs[i][0],0.0);
				}

			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}

		@Test
		@Theory
		/**
		 * 配列を改変してもいい解き方で繰り返し解いても問題はないか
		 * */
		public void testSolve_ChangeArrayAndSeveralTime(SolverConstructor solverconstructor, double[][][] As,double[][] bs,boolean[] AisNull) {
			try {
				LinearEquationSolver solver
					= solverconstructor.solverclass
							.getConstructor(solverconstructor.parameterType)
							.newInstance(solverconstructor.parameter);

				solver.changeArray(true);

				assertEquals(As.length,bs.length);
				assertEquals(As[0].length,bs[0].length);
				assertEquals(As[0].length,As[0][0].length);

				//配列を新しく用意しておく（次のテストケースのときに変化している恐れがあるため）
				double[][][] _As = new double[As.length][As[0].length][As[0][0].length];
				double[][] _bs = new double[bs.length][bs[0].length];
				for(int i=0;i<As.length;i++) {
					for(int j=0;j<As[0].length;j++) {
						for(int k=0;k<As[0][0].length;k++) {
							_As[i][j][k] = As[i][j][k];
						}
						_bs[i][j] = bs[i][j];
					}
				}

				//連続で解く
				repeatSolve(solver,_As,_bs,AisNull);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}


		/**
		 * 繰り返し方程式を解かせても問題ないかをテストします。
		 * ソルバーが配列を変化させない設定だった場合、引数で指定された配列を直接テスト計算に用います。
		 * 一方、ソルバーが配列を変化させる設定の場合、引数で指定された配列とはまた違う配列を用意し、
		 * それを用いて計算します。すなわち、いずれにしても引数で指定された配列は変化しないことが
		 * 期待されます。
		 * */
		private void repeatSolve(LinearEquationSolver solver, double[][][] As, double[][] bs, boolean[] AisNull) {
			IntConsumer assertSolution = (i) -> {
				double[][] A;
				double[] b;
				if(solver.isToChangeArray()) {
					//solverが配列を変える設定だった場合、
					A = new double[As[0].length][As[0][0].length];
					b = new double[bs[0].length];
					for(int j=0;j<A.length;j++) {
						for(int k=0;k<A[0].length;k++) {
							A[j][k] = As[i][j][k];
						}
						b[j] = bs[i][j];
					}
				}else {
					//solverが配列を変えない設定だった場合、元々テストメソッドで指定された配列を計算に用いる
					A = As[i];
					b = bs[i];
				}
				double[] x = solver.solve(A,b);
				assertSolutionIsCorrect(As[i],x,bs[i]);
			};

			if(solver instanceof LU && ((LU) solver).isToReuseLUResult()) {
				for(int i=0;i<As.length;i++) {
					//今までの試行ではAは全てnull指定だったのか
					boolean allnull = true;
					//null指定ではない一番最後の試行回はいつだったのか
					int lastNotNullI=0;
					for(int j=i-1;j>=0;j--) {
						if(!AisNull[j]) {
							allnull = false;
							lastNotNullI = j;
							break;
						}
					}

					if(AisNull[i]) {
						//この試行回ではAがnullだった場合

						if(allnull) {
							//それまでもnullだった場合、例外処理
							final int k=i;
							Assertions.assertThrows(
									IllegalArgumentException.class,
									()->solver.solve(null, bs[k])
							);
						}else {
							//bは今回のものを用い、前回のLU分解結果を利用して解いた結果が
							//前回指定した係数行列に対して合っているのかをアサーション
							double[] b;
							if(solver.isToChangeArray()) {
								b = new double[bs[i].length];
								for(int j=0;j<b.length;j++) {
									b[j] = bs[i][j];
								}
							}else {
								b = bs[i];
							}
							double[] x = solver.solve(null, b);

							//一番最後に係数行列を指定した回の係数行列を用いてアサーション
							assertSolutionIsCorrect(As[lastNotNullI],x,bs[i]);
						}
					}else {
						assertSolution.accept(i);
					}

				}
			}else {
				//LU分解結果を用いない場合
				for(int i=0;i<As.length;i++) {
					if(AisNull[i]) {
						final int k=i;
						Assertions.assertThrows(
								IllegalArgumentException.class,
								()->solver.solve(null, bs[k])
						);
					}else{
						assertSolution.accept(i);
					}
				}
			}
		}


	}

	/*
	 * 指定された解ベクトルが正しい値かどうかをテストします。
	 * @param a 方程式の係数行列
	 * @param x 方程式の解ベクトル
	 * @param b 方程式の右辺項ベクトル
	 * */
	public static void assertSolutionIsCorrect(double[][] a, double[] x, double[] b) {
		assertEquals(a.length,b.length);
		assertEquals(a.length,x.length);
		for(int i=0;i<a.length;i++) {
			assertEquals(a[i].length,x.length);
			//左辺の行列の計算
			double aij_xj = 0;
			for(int j=0;j<x.length;j++) {
				aij_xj += a[i][j]*x[j];
			}

			//右辺と一致するかどうか
			if(aij_xj == 0.0 || b[i] == 0.0) {
				assertEquals(aij_xj, b[i], Math.pow(10, -8));
			}else {
				assertEquals(aij_xj, b[i], b[i]*Math.pow(10,-8));
			}
		}
	}
}
