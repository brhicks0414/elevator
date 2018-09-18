# Elevator Manager

Simulate the execution of an elevator system. The System follows a simple algorithm:

* If there are passengers in the elevator continue in the current direction dropping off passengers
* If a floor is reached where there is a request for a ride going in the same direction, stop and pickup that passenger
* If there are no passengers, go in the direction of the next request
* If there are no passengers or requests, stop at the current floor until there is another request

The 