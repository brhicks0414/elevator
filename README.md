# Elevator Manager

Simulate the execution of an elevator system. The System follows a simple algorithm (more or less the classic "Elevator Algorithm"):

* If there are passengers in the elevator continue in the current direction dropping off passengers
* If a floor is reached where there is a request for a ride going in the same direction, stop and pickup that passenger
* If there are no passengers, go in the direction of the next request
* If there are no passengers or requests, stop at the current floor until there is another request

The system prints out updated statistics each time a passenger is dropped off:
* Average Trip Time: average time that a passenger spend on the elevator 
* Average Wait Time: average time that a passenger waits for a request

For an example of how the system runs, see RequestManager.main();

Next items to implement:

* Refactor Elevator to extract the algorithm so it can easily be replaced with an optimized algorithm and/or independently tested.
* Optimize the algorithm to take into account time of day: pre-position elevators during rush hours
* Currently if multiple elevators are idle and a new request is issued, all idle elevators will try to service the request. 
* Simple test harness to read scenarios (number of elevators, starting positions, requests) and report statistics