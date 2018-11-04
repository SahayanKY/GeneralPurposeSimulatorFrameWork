package simulation.solver;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class LUTest {

	@RunWith(Theories.class)
	public static class SolveTest{
		@DataPoints
		public static double[][][] As = {
				{
					{1,0,0},
					{0,1,0},
					{0,0,1}
				},
				{
					{0,2,1},
					{4,5,2},
					{1,3,0}
				}
		};

		@DataPoints
		public static LinearEquationSolver[] solvers = {
			new LU()
		};

		@DataPoints
		public static double[][] Bs = {
				{2,5,2},
				{3,4,7},
				{1,0,3}
		};

		@Test
		@Theory
		/*
		 * 係数行列や右辺項ベクトルを変化させない解き方でのテストメソッド
		 * */
		public void testSolver(LinearEquationSolver solver, double[][] A,double[] b) {
			if(A.length != b.length) {
				return;
			}
			solver.changeArray(false);
			double[] x = solver.solve(A, b);
			for(int i=0;i<A.length;i++) {
				double assertb=0;
				for(int j=0;j<A.length;j++) {

				}
			}
		}

		/*
		 * 1回目の計算
		 * ・Aとbの行、列数が一致しない場合のハンドリング
		 * ・LUを保存するかどうかに依らない計算結果
		 * ・Aとbの改変設定との一致
		 *
		 * 2回目の計算(前回LU保存)
		 * ・前回のLUを利用して計算したときの結果
		 * ・前回のLUを破棄し、新しい計算をしたときの結果
		 *
		 * 2回目の計算(前回LU非保存)
		 * ・
		 * */

	}
}
