package com.example.elevator;

import java.util.UUID;

/**
 * Model the request for an elevator. This model assumes that the passenger
 * making the request get on the elevator and requests to go to the destination
 * floor.
 * 
 * @author brian
 *
 */
public class Request {

	private int floor;
	private Direction direction;
	private int destinationFloor;

	private String id;

	/**
	 * The floor from which the request was made
	 * 
	 * @return
	 */
	public int getFloor() {
		return floor;
	}

	/**
	 * The direction of the request (UP or DOWN)
	 * 
	 * @return
	 */
	public Direction getDirection() {
		return direction;
	}

	public Request(int floor, Direction direction, int destinationFloor) {
		super();
		if (direction == Direction.IDLE) {
			throw new IllegalArgumentException("Direction of request cannot be IDLE");
		}
		if (direction == Direction.DOWN && destinationFloor > floor) {
			throw new IllegalArgumentException("If direction is DOWN, destination must be lower than floor");
		}
		if (direction == Direction.UP && destinationFloor < floor) {
			throw new IllegalArgumentException("If direction is UP, destination must be higher than floor");
		}
		this.floor = floor;
		this.direction = direction;
		this.destinationFloor = destinationFloor;
		this.id = UUID.randomUUID().toString();
	}

	/**
	 * The destination floor
	 * 
	 * @return
	 */
	public int getDestinationFloor() {
		return this.destinationFloor;
	}

	public String getId() {
		return this.id;

	}
}
