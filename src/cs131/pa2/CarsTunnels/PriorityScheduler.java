package cs131.pa2.CarsTunnels;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cs131.pa2.Abstract.Tunnel;
import cs131.pa2.Abstract.Vehicle;
import cs131.pa2.Abstract.Log.Log;

public class PriorityScheduler extends Tunnel{
	Collection<Tunnel> available_tunnels;
	Collection<Tunnel> full_tunnels;
	Map<Vehicle, Tunnel> cars_to_tunnels;
	PriorityQueue<Vehicle> waitSet;
	
	final Lock enter_lock;
	// conditions used along with condition variables (semaphores) 
	final Condition noAvailableTunnels;
	final Condition notHighestPriorityVehicle;
	
	final Lock exit_lock;
	
	public PriorityScheduler(String name, Collection<Tunnel> tunnels, Log log) {
		super(name, log);
		this.available_tunnels = tunnels;
		this.full_tunnels = new HashSet<Tunnel>();
		this.cars_to_tunnels = new HashMap<Vehicle, Tunnel>();
		// initialize priority queue with comparator in order for queue to sort vehicles based on priority
		waitSet = new PriorityQueue<Vehicle>(100, new VehicleComparator()); 
		enter_lock = new ReentrantLock();
		exit_lock = new ReentrantLock();
		noAvailableTunnels = enter_lock.newCondition();
		notHighestPriorityVehicle = enter_lock.newCondition();
	}

	@Override
	public boolean tryToEnterInner(Vehicle vehicle) {
		enter_lock.lock();
		// critical section
		while(!isHighestPriority(vehicle)) { // not highest priority vehicle so thread waits
			waitSet.add(vehicle);
			try {
				notHighestPriorityVehicle.await();
			} catch (InterruptedException e) {}
		}
		
		while(!tryAvailableTunnels(vehicle)) { // no higher priority vehicle, but no tunnels available for entry
			// thread waits
			try {
				waitSet.add(vehicle);
				noAvailableTunnels.await();
			} catch (InterruptedException e) {}
		}
		
		waitSet.remove(vehicle); // vehicle has entered, so is no longer waiting
		
		enter_lock.unlock();
		return true;
		
	}

	@Override
	public void exitTunnelInner(Vehicle vehicle) {
		enter_lock.lock();
		BasicTunnel temp_tunnel = (BasicTunnel) cars_to_tunnels.get(vehicle);
		temp_tunnel.exitTunnelInner(vehicle);
		cars_to_tunnels.remove(vehicle); // vehicle no longer in this tunnel
		if(full_tunnels.contains(temp_tunnel)) { // if this tunnel was full before the exit
			available_tunnels.add(temp_tunnel); // make tunnel available for trial entry
			full_tunnels.remove(temp_tunnel); // remove tunnel from full tunnels list
			noAvailableTunnels.signalAll();
		}
		if(vehicle.equals(waitSet.peek())) {
			notHighestPriorityVehicle.signalAll();
		}
		enter_lock.unlock();
	}
	
	private class VehicleComparator implements Comparator<Vehicle> {
		// override compare method of Comparator
		public int compare(Vehicle v1, Vehicle v2) {
			if(v1.getPriority() > v2.getPriority()) {
				return -1;
			}else if(v1.getPriority() < v2.getPriority()) {
				return 1;
			}
			return 0;
		}
	}
	
	/*
	 * this method is the critical section of the code
	 */
	private boolean tryAvailableTunnels(Vehicle vehicle) {
		if(available_tunnels.isEmpty()) {
			return false;
		}
		BasicTunnel temp = null;
		for(Tunnel tunnel : available_tunnels) {
			if(tunnel.tryToEnterInner(vehicle)) {
				cars_to_tunnels.put(vehicle, tunnel);
				if(((BasicTunnel) tunnel).atCapacity()) { //if the tunnel is now full, break for loop, remove tunnel from available tunnels
					full_tunnels.add(tunnel);
					temp = (BasicTunnel) tunnel;
					break;
				}
				return true;
			}
		}
		if(temp != null) {
			available_tunnels.remove(temp);
			return true;
		}
		return false;
	}
	private boolean isHighestPriority(Vehicle vehicle) {
		return waitSet.isEmpty() || waitSet.peek().getPriority() < vehicle.getPriority();
	}
}
