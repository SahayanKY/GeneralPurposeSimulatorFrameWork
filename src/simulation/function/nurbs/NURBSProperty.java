package simulation.function.nurbs;

public class NURBSProperty {
	protected final double[][] knot;
	//パラメータの数
	protected final int parameterNum;

	public NURBSProperty(double[][] knot, int parameterNum){
		this.knot = knot;
		this.parameterNum = parameterNum;
	}
}
