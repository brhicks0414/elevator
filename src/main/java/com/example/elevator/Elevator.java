package com.example.elevator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class Elevator {
	private static final long DESTINATION_WAIT_TIME = 1000;
	private static final long TIME_BETWEEN_FLOORS = 1000;
	private RequestManager requestManager;
	private String name;

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

	private Direction status = Direction.IDLE;

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

	public Direction getStatus() {
		return this.status;
	}

	public void setStatus(Direction direction) {
		this.status = direction;
	}

	public String getName() {
		return this.name;
	}

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
			if (r != null && (destinations.isEmpty() || (r.getDirection() == status))) {
				// service the request
				System.out.println("[" + name + "] Picking up passenger on floor " + currentFloor);
				addDestination(r);
				requestManager.requestServiced(r);
				result = true;
				status = r.getDirection();

			}
			return result;
		}

		private int getNextFloor() {
			int nextFloor = currentFloor;
			if (destinations.isEmpty()) {
				List<Request> requests = requestManager.getCurrentRequests();
				if (!requests.isEmpty()) {
					int requestFloor = requests.get(0).getFloor();
					if (requestFloor < currentFloor) {
						nextFloor--;
					} else {
						nextFloor++;
					}
				}
			} else {
				if (status == Direction.UP) {
					nextFloor++;
				}
				if (status == Direction.DOWN) {
					nextFloor--;
				}
			}
			return nextFloor;
		}

		@Override
		public void run() {
			while (!stopRequested) {
				if (status != Direction.IDLE) {
					System.out.println("[" + name + "] At floor " + currentFloor + "; Status=" + status);
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
					status = Direction.IDLE;
				} else if (nextFloor < currentFloor) {
					status = Direction.DOWN;
				} else if (nextFloor > currentFloor) {
					status = Direction.UP;
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
			if (status != Direction.IDLE) {
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
