package cs131.pa2.CarsTunnels;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Direction;

public class BasicTunnel extends Tunnel{
	private int capacity;
	private boolean hasSled;
	private Direction direction;
	
	public BasicTunnel(String name) {
		super(name);
		this.capacity = 0;
		this.hasSled = false;
	}

	@Override
	public synchronized boolean tryToEnterInner(Vehicle vehicle) { // insert synchronized keyword
		if(vehicle instanceof Sled) { // if sled tries to enter tunnel
			if(isEmpty()) { // and tunnel is empty
				// then allow entrance, increase capacity, return true
				this.capacity++;
				this.hasSled = true;
				return true;
			}else { // otherwise block
				return false;
			}
		} else if(vehicle instanceof Car) {
			if(isEmpty()) {
				// first car in tunnel determines direction of tunnel
				this.direction = vehicle.getDirection();
			}
			if(atCapacity()) {
				// if tunnel is full, then block
				return false;
			}else if(this.direction != vehicle.getDirection()) {
				// if car is going different direction than tunnel allows, then block
				return false;
			}else if(hasSled) {
				// sleds and cars cannot share tunnels
				return false;
			}else {
				// no issues, allow entry
				this.capacity++;
				return true;
			}
		}
		return false;
	}

	@Override
	public synchronized void exitTunnelInner(Vehicle vehicle) { 
		this.capacity--;
		if(vehicle instanceof Sled) {
			this.hasSled = false;
		}
	}
	public boolean atCapacity() {
		return capacity == 3 || hasSled;
	}
	
	public boolean isEmpty() {
		return capacity == 0;
	}
}
