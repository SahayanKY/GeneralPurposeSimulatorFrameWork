package parabolicmovement;

import simulation.frame.DataInputFrame;

public class MainParabolicMovement {

	public static void main(String args[]) {
		ParabolicMovementSimulator simulator = new ParabolicMovementSimulator();
		simulator.createParameters();

		DataInputFrame frame = new DataInputFrame(simulator,340,490);
	}
}
