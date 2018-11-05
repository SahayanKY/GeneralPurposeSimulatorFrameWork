package simulation.solver;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class LUTest {

	@RunWith(Theories.class)
	public static class SolveTest{
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
		public void testLU(SolverConstructor solverconstructor, double[][][] As,double[][] bs,boolean[] AisNull) {
			try {
				LinearEquationSolver solver
					= solverconstructor.solverclass
							.getConstructor(solverconstructor.parameterType)
							.newInstance(solverconstructor.parameter);



				solver.changeArray(false);
				if(((LU) solver).isToReuseLUResult()) {
					for(int i=0;i<3;i++) {
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
								//一番最後に係数行列を指定した回の係数行列を用いてアサーション
								double[] x = solver.solve(null, bs[i]);
								assertSolutionIsCorrect(As[lastNotNullI],x,bs[i]);
							}
						}else {
							double[] x = solver.solve(As[i],bs[i]);
							assertSolutionIsCorrect(As[i],x,bs[i]);
						}

					}
				}else {
					//LU分解結果を用いない場合
					for(int i=0;i<3;i++) {
						if(AisNull[i]) {
							final int k=i;
							Assertions.assertThrows(
									IllegalArgumentException.class,
									()->solver.solve(null, bs[k])
							);
						}else{
							double[] x = solver.solve(As[i],bs[i]);
							assertSolutionIsCorrect(As[i],x,bs[i]);
						}
					}
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


	}

	/*
	 * 指定された解ベクトルが正しい値かどうかをテストします。
	 * @param a 方程式の係数行列
	 * @param x 方程式の解ベクトル
	 * @param b 方程式の右辺項ベクトル
	 * */
	public static void assertSolutionIsCorrect(double[][] a, double[] x, double[] b) {
		System.out.println(1.7763568394002505E-15+0.0);
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
