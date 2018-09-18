package com.example.elevator;

/**
 * Class that holds pre-calculated statistics for reporting purposes
 * 
 * @author brian
 *
 */
public class ElevatorStatistics {

	private long averageWaitTime;
	private long averageTripTime;

	public ElevatorStatistics(long averageWaitTime, long averageTripTime) {
		this.averageTripTime = averageTripTime;
		this.averageWaitTime = averageWaitTime;
	}

	public long getAverageWaitTime() {
		return averageWaitTime;
	}

	public long getAverageTripTime() {
		return averageTripTime;
	}

}
