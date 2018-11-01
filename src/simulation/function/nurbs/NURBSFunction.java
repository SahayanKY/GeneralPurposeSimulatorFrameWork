package simulation.function.nurbs;

public class NURBSFunction {
	private final double[][] ctrl;
	private final NURBSProperty property;

	public NURBSFunction(double[][] ctrl, NURBSProperty property) {
		this.ctrl = ctrl;
		this.property = property;
	}

	public double[] value(double... t){
		return null;
	}
}
