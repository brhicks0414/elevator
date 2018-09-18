package com.example.elevator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that receives and tracks requests for elevators. There should be just
 * one RequestManager per bank of elevators. This class does not care how many
 * elevators service requests. It simply tracks that a request through its
 * lifecycle, as follows:
 * <li>Request Received (passenger makes request for an elevator)
 * <li>Request Serviced (passenger is picked up by an elevator)
 * <li>Request Completed (passenger is dropped off by an elevator at the
 * destination floor)
 * 
 * @author brian
 *
 */
public class RequestManager {

	private List<Request> unservicedRequests = new ArrayList<>();

	private List<Request> completedRequests = new ArrayList<>();

	private Map<String, Stats> requestStats = new HashMap<>();

	/**
	 * Return the current list of unserviced requests.
	 * 
	 * @return
	 */
	public List<Request> getUnservicedRequests() {
		return new ArrayList<>(unservicedRequests);
	}

	/**
	 * Add a request to the system (a passenger makes a request for an elevator)
	 * 
	 * @param r
	 *            a non-null request
	 */
	public void addRequest(Request r) {
		System.out.println("Request added [" + r.getFloor() + "; " + r.getDirection() + "]");
		unservicedRequests.add(r);
		requestStats.put(r.getId(), new Stats(r.getId()));
	}

	/**
	 * Return the request for the given floor, if there is one.
	 * 
	 * @param currentFloor
	 *            the floor to check
	 * @return the request, or null, if there is no unserviced request for the floor
	 */
	public Request getRequest(int currentFloor) {
		for (Request request : unservicedRequests) {
			if (request.getFloor() == currentFloor) {
				return request;
			}
		}
		return null;
	}

	/**
	 * Is there are requests that can be serviced by an elevator going in the
	 * specified direction
	 * 
	 * @param direction
	 *            a non-null direction
	 * @return true if the direction is IDLE and there are any requests; true if the
	 *         direction is either UP or DOWN and there exists one or more requests
	 *         for the specified direction; false otherwise
	 */
	public boolean hasRequestsFor(Direction direction) {
		if (direction == Direction.IDLE) {
			if (!unservicedRequests.isEmpty()) {
				return true;
			}
		} else {
			for (Request request : unservicedRequests) {
				if (request.getDirection() == direction) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Handle a request once it has been serviced.
	 * 
	 * @param r
	 *            a non-null request
	 */
	public void requestServiced(Request r) {
		unservicedRequests.remove(r);
		requestStats.get(r.getId()).pickup();
	}

	/**
	 * Handle a request once it has been completed
	 * 
	 * @param r
	 */
	public void requestCompleted(Request r) {
		requestStats.get(r.getId()).stop(System.currentTimeMillis());
		completedRequests.add(r);
		System.out.println("Trip Time: " + requestStats.get(r.getId()).getTripTime() + "s");
		System.out.println("Wait Time: " + requestStats.get(r.getId()).getWaitTime() + "s");
		ElevatorStatistics stats = getCurrentStats();
		System.out.println("Current Elevator Statistics" + "\n  Average Wait Time: " + stats.getAverageWaitTime()
				+ "s\n  Average Trip Time: " + stats.getAverageTripTime() + "s");
	}

	/**
	 * 
	 * @return the current stats for all requests handled by this manager
	 */
	public ElevatorStatistics getCurrentStats() {
		int numRequests = completedRequests.size();
		long totalWaitTime = 0;
		long totalTripTime = 0;
		for (Request r : completedRequests) {
			Stats stats = requestStats.get(r.getId());
			totalWaitTime += stats.getWaitTime();
			totalTripTime += stats.getTripTime();
		}

		long averageWaitTime = totalWaitTime / numRequests;
		long averageTripTime = totalTripTime / numRequests;
		return new ElevatorStatistics(averageWaitTime, averageTripTime);

	}

	private class Stats {
		private String id;

		private long pickupTime;

		public Stats(String id) {
			this.id = id;
			this.start = System.currentTimeMillis();
		}

		public String getId() {
			return id;
		}

		public long getPickupTime() {
			return this.pickupTime;
		}

		public void pickup() {
			this.pickupTime = System.currentTimeMillis();
		}

		public long getRequestTime() {
			return start;
		}

		public long getDropOffTime() {
			return stop;
		}

		public long getTripTime() {
			if (stop != -1) {
				return (stop - pickupTime) / 1000;
			} else {
				return 0;
			}
		}

		public long getWaitTime() {
			return (pickupTime - start) / 1000;
		}

		private void stop(long time) {
			this.stop = time;
		}

		private long start;
		private long stop = -1;

	}

	public static void main(String[] args) {
		RequestManager manager = new RequestManager();
		new Elevator("A", manager).run();
		new Elevator("B", manager).run();
		synchronized (manager) {
			manager.addRequest(new Request(5, Direction.UP, 10));
			manager.addRequest(new Request(7, Direction.UP, 9));
			manager.addRequest(new Request(6, Direction.DOWN, 1));
			manager.addRequest(new Request(10, Direction.DOWN, 1));
		}

	}

}
