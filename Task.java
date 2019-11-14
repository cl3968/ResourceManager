/**
 * The Task class stores the input of the operations per task and other variables to 
 * help with the resource manager. 
 * 
 * @author Christina Liu 
 *
 */

import java.util.ArrayList;

public class Task {
	ArrayList <String> input = new ArrayList<String>();
	
	int index, id, finishTime, computeTime, numBlocked ;
	boolean abort, terminate, block;
	
	
	//constructor
	public Task(int i) {
		
		//task number
		id=i+1;
		
		//like an iterator
		index = 0; 
		
		//computational time
		computeTime=0; 	
		
		//blocked number
		numBlocked=0;
		
		//finished time
		finishTime=0;
		
		//if task was aborted or not
		abort=false;
		
		//if task was terminated or not
		terminate = false;
		
		//if task is blocked or not blocked
		block = false;
	}
	
	/**
	 * Returns the integer that corresponds to a certain operation
	 * @returns the integer that corresponds to an operation 
	 */
	//getting the operation of the task
	public int getOperation() {
		if (input.get(index).contains("initiate"))
			return 1;
		else if (input.get(index).contains("request"))
			return 2;
		else if (input.get(index).contains("release"))
			return 3;
		else if (input.get(index).contains("compute"))
			return 4;
		else if (input.get(index).contains("terminate"))
			return 5;
		else 
			return 0;
	}
	
	/**
	 * Returns the integer that corresponds to first number after the operation name
	 * @returns task number
	 */
	public int getfirst() {
		String split[] = input.get(index).split("\\s+");
		return Integer.parseInt(split[1]);
	}
	
	/**
	 * Returns the integer that corresponds to second number after the operation name
	 * @returns the resource type for all operations except compute, which returns the number of cycles  
	 */
	public int getsecond() {
		String split[] = input.get(index).split("\\s+");
		return Integer.parseInt(split[2]);
	}
	
	/**
	 * Returns the integer that corresponds to third number after the operation name
	 * @returns the initial claim for initiate, the number requested for request and the number released for release
	 */
	public int getthird() {
		String split[] = input.get(index).split("\\s+");
		return Integer.parseInt(split[3]);
	}
	
	/**
	 * Returns a boolean 
	 * @returns boolean true if input has a next String and false if not 
	 */
	//does input have next
	public Boolean hasNext() {
		//last input
		if (index == input.size()-1) {
			return false;
		}
		else
			return true;
	}
	
	/**
	 * Returns the next String in input
	 * @returns the next String in input according to the index
	 */
	//get next input 
	public String getNext() {
		
		return input.get(index);
	}

	/**
	 * Returns a boolean
	 * @returns true if the input is finished and false if it is not finished
	 */
	//is the task finished 
	public Boolean finished() {
		if (computeTime == 0 && getNext().contains("terminate")) {
			return true;
		}
		else
			return false;
	}
	
	/**
	 * Aborts a task by setting the abort variable to true and decrementing the index
	 * and setting the blocked time to zero.
	 */
	//abort this task
	public void abortThis() {
		abort = true;
		index = input.size()-1; //decrease size
		numBlocked=0;
	}
	
	/**
	 * Resets a task by doing the same thing as a constructor, setting everything the same as constructor 
	 * 
	 */
	//resetting the task from the beginning 
	public void reset(int i) {
		id=i+1;
		index = 0; 	
		computeTime=0; 	
		numBlocked=0;
		finishTime=0;
		abort=false;
		terminate = false;
		block = false;
	}
}
