package com.example.elevator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Class that models an elevator.
 * 
 * @author brian
 *
 */
public class Elevator {
	private static final long DESTINATION_WAIT_TIME = 1000;
	private static final long TIME_BETWEEN_FLOORS = 1000;
	private RequestManager requestManager;
	private String name;

	/**
	 * Initialize the elevator
	 * 
	 * @param name
	 *            the name of the elevator (should uniquely identify an elevator)
	 * @param requestManager
	 *            a non-null request manager
	 */
	public Elevator(String name, RequestManager requestManager) {
		this.requestManager = requestManager;
		this.name = name;
	}

	public void run() {
		Thread executor = new Thread(new Executor());
		executor.start();
	}

	private boolean stopRequested = false;

	public void stop() {
		stopRequested = true;
	}

	private List<Request> destinations = new ArrayList<>();

	private int currentFloor = 1;

	private Direction direction = Direction.IDLE;

	public void addDestination(Request request) {
		destinations.add(request);
		Collections.sort(destinations, new Comparator<Request>() {

			@Override
			public int compare(Request o1, Request o2) {
				return (o1.getDestinationFloor() - o2.getDestinationFloor());
			}
		});
	}

	public int getCurrentFloor() {
		return this.currentFloor;
	}

	public List<Request> getDestinations() {
		return Collections.unmodifiableList(destinations);
	}

	public Direction getDirection() {
		return this.direction;
	}

	public void setStatus(Direction direction) {
		this.direction = direction;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Thread used to manage an elevator. This is where the algorithm lives.
	 * 
	 * @author brian
	 *
	 */
	private class Executor implements Runnable {

		private boolean handlePassengersAtFloor() {
			// is this floor a destination?
			List<Request> toRemove = new ArrayList<>();
			boolean result = false;
			for (Request r : destinations) {
				if (r.getDestinationFloor() == currentFloor) {
					// drop off passenger at this floor
					System.out.println("[" + name + "] Dropping off passenger on floor " + currentFloor);
					requestManager.requestCompleted(r);
					toRemove.add(r);
					result = true;
				}
			}
			destinations.removeAll(toRemove);

			return result;
		}

		private boolean pickupPassengersIfNecessary() {
			// Check for requests in my direction at this floor
			boolean result = false;
			Request r = requestManager.getRequest(currentFloor);
			if (r != null && (destinations.isEmpty() || (r.getDirection() == direction))) {
				// service the request
				System.out.println("[" + name + "] Picking up passenger on floor " + currentFloor);
				addDestination(r);
				requestManager.requestServiced(r);
				result = true;
				direction = r.getDirection();

			}
			return result;
		}

		private int getNextFloor() {
			int nextFloor = currentFloor;
			if (destinations.isEmpty()) {
				List<Request> requests = requestManager.getUnservicedRequests();
				if (!requests.isEmpty()) {
					int requestFloor = requests.get(0).getFloor();
					if (requestFloor < currentFloor) {
						nextFloor--;
					} else {
						nextFloor++;
					}
				}
			} else {
				if (direction == Direction.UP) {
					nextFloor++;
				}
				if (direction == Direction.DOWN) {
					nextFloor--;
				}
			}
			return nextFloor;
		}

		@Override
		public void run() {
			while (!stopRequested) {
				if (direction != Direction.IDLE) {
					System.out.println("[" + name + "] At floor " + currentFloor + "; Status=" + direction);
				}
				boolean stoppedAtThisFloor = false;

				int nextFloor;
				synchronized (requestManager) {
					// drop off any passengers destined for the current floor
					boolean passengersDroppedOff = handlePassengersAtFloor();

					// pick up any passengers waiting at the current floor
					boolean passengersPickedUp = pickupPassengersIfNecessary();

					// the elevator stops at the current floor if it drops off or picks up
					// passengers
					stoppedAtThisFloor = passengersDroppedOff || passengersPickedUp;

					// which floor should i go to next?
					nextFloor = getNextFloor();
				}
				// determine direction to next floor
				if (nextFloor == currentFloor) {
					direction = Direction.IDLE;
				} else if (nextFloor < currentFloor) {
					direction = Direction.DOWN;
				} else if (nextFloor > currentFloor) {
					direction = Direction.UP;
				}

				// simulate the time required to drop off and/or pick up passengers
				waitIfNecessary(stoppedAtThisFloor);
				currentFloor = nextFloor;
			}

		}

		private void waitIfNecessary(boolean waitAtThisFloor) {
			if (waitAtThisFloor) {
				try {
					synchronized (this) {
						this.wait(DESTINATION_WAIT_TIME);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			if (direction != Direction.IDLE) {
				try {
					synchronized (this) {
						this.wait(TIME_BETWEEN_FLOORS);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

		}

	}
}
