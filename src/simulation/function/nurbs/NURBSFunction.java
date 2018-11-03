package simulation.function.nurbs;

public class NURBSFunction {
	private final double[][] ctrl;
	private final NURBSProperty property;

	public NURBSFunction(double[][] ctrl, NURBSProperty property) {
		//次数は設定させないのか？
		//ctrlの要素数はproperty.knotとつじつまがあうのか
		this.ctrl = ctrl;
		this.property = property;
	}

	public double[] value(double... t){
		//t.lengthはproperty.parameterNumと一致するのか
		return null;
	}
}
