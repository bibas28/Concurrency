package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Direction;
import cs131.pa2.Abstract.Vehicle;

/*
 * An ambulance has reserved priority of 4
 * An ambulance has the fasted possible speed
 * An ambulance can share a tunnel with any other vehicle including a sled, except another ambulance
 * No other vehicles can make progress when an ambulance is in the tunnel
 */
public class Ambulance extends Vehicle {
	public Ambulance (String name, Direction direction) {
		super(name, direction);
		super.setPriority(4);
	}
	/*
	 * 9 is the fastest possible speed per assignment requirements
	 */
	protected int getDefaultSpeed() {
		return 9;
	}
}
