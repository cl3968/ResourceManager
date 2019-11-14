/**
 * The Optimistic class is responsible for doing resource allocation using an optimistic 
 * resource manager. The optimistic resource manager is simple: Satisfy a request if possible, if not make
 * the task wait; when a release occurs, try to satisfy pending requests in a FIFO manner.
 * 
 * @author Christina Liu 
 *
 */

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Optimistic {
	int T, R, cycle; //number of tasks, number of resources, and number of cycles 
	
	int[] avaliable; //avaliability matrix
	int[][] allocate; //allocation matrix
	
	boolean deadlock, possibleDeadlock, allfinished;  //see if the Tasks are deadlocked, possible deadlocked, and if Banker method is finished

	ArrayList<Task> readyQueue = new ArrayList<Task>(); //for all the ready processes
	ArrayList<Task> blockQueue = new ArrayList<Task>(); //for all the blocked processes
	ArrayList<Task> waitQueue = new ArrayList<Task>(); //for all the processes waiting 
	
	//constructor that initializes all the global variables
	public Optimistic(int t, int r){
		T=t;R=r;
		cycle =0;
		
		avaliable = new int[R];
		allocate = new int[T][R];
		
		deadlock=false;
		possibleDeadlock=false;
		allfinished= false;
	}
	
	/**
	 * Performs each operation in the Task object array every cycle, checking for deadlocks 
	 * @param an array of Task objects 
	 */
	//executes methods until blocked
	public void main(Task[] Tasks) {
		
		//executing main method until all tasks are finished 
		while(!allfinished) {
			//total num of operations executed in one cycle
			int operations=0;
			//num of blocked requesting operations
			int numBlockedReq=0;
			waitQueue.clear(); //clear waiting processes 
			int[] release=new int[R];
			for (int i = 0; i < release.length; i++) {
				release[i]=0; //initialize array to zero and fill in later
			}
			cycle ++;
			//if this cycle is possible deadlocked
			if(possibleDeadlock) {
				int j =0;
				for (Task t: Tasks) {
					if (!t.finished() && !t.abort) { //if task is unfinished or not aborted then it could be deadlocked
						//abort this task
						t.abortThis();
						//and then release the task
						for (int i =0; i <R; i++) {
							avaliable[i] += allocate[j][i];
						}
						blockQueue.remove(t); //also remove from block queue
						
						if (!isDeadlocked(Tasks)) {
							possibleDeadlock=false;
							break; //not deadlocked then stop checking
						}
					}
					j++;	
				}
				
			}
			//making sure the blocked task is ready to allocate
			while(!blockQueue.isEmpty()) {
				Task task = blockQueue.get(0);
				blockQueue.remove(0); //remove from block queue 
				operations++;
				boolean isAllocate = toAllocate(task);

				if (isAllocate) {
					readyQueue.add(task); //add blocked task to ready queue
				}
				else {
					numBlockedReq++;
					waitQueue.add(task); //blocked task is now waiting
				}
			}
			blockQueue.addAll(waitQueue);
			
			//actually reading in tasks
			for (int i =0; i <Tasks.length; i++) {
				Task t = Tasks[i];
				if (!blockQueue.contains(t) && !readyQueue.contains(t)) { //checking tasks that aren't blocked
					if (t.computeTime == 0) { //checking if tasks are waiting or not 
						
						//now let's check all the operations
						if (t.getOperation() == 1) { //for initiate the optimistic manager ignores the claim
							operations++;
							t.index++; //next operation
						}
						else if (t.getOperation() == 2) { //for request the optimistic manager sees if it can grant the request 
							operations++;
							boolean isAllocate=toAllocate(t);
							
							if (!isAllocate) {
								numBlockedReq++; //add to wait time 
								blockQueue.add(t) ; //add to block queue is manager cannot allocated
							}
						}
						else if (t.getOperation() == 3) { //for release the optimistic manager updates release array and allocate array 
							operations++; 
							int resourceType = t.getsecond()-1;
							int numReleased = t.getthird();
							release[resourceType]+=numReleased; //from release array 
							allocate[i][resourceType] -=numReleased; //add to allocate array
							t.index++; //next operation
							if(t.finished()) {
								t.finishTime = cycle; //updates finishing time 
							}
						}
						else if (t.getOperation() == 4) { //for compute the process is computing for several cycles, so there are no requests or releases 
							operations++;
							
							int taskNumber = t.getfirst()-1;
							int numOfCycles = t.getsecond()-1;
							Tasks[taskNumber].computeTime = numOfCycles; //update computing time
							Tasks[taskNumber].index++; //go to next operation
							if(t.finished() && t.computeTime==0) {
								t.finishTime=cycle; //set finishing time to cycle number 
							}
						}
					} //else terminate do nothing
					else { //if tasks are waiting 
						operations++;
						t.computeTime--; 
						if(t.computeTime==0 && t.finished()) {
							t.finishTime=cycle; //update finishing time 
						}
					}
				}
			}
			for (int a=0; a <R; a++) { //collect resources that were released in this cycle
				avaliable[a] += release[a];
			}
			
			Task[] blockToReady = readyQueue.toArray(new Task[0]);
			for (int b=0; b < blockToReady.length; b++) { //removing task from ready queue 
				readyQueue.remove(blockToReady[b]);
			}
			
			if(operations == numBlockedReq) { //this means deadlock and that initiates aborting tasks next cycle 
				possibleDeadlock = true;
			}
			else {
				possibleDeadlock = false;
			}
			 allfinished= isFinished(Tasks); //checking if tasks are finished and therefore stopping the while loop
				 ;
			
		}
	 	print(Tasks); //printing the output 
	}
	
	/**
	 * Checks if Task objects are deadlocked  
	 * @param an array of Task objects 
	 * @return true if the array of Task objects are deadlocked and false if the array of Task objects are not deadlocked
	 */
	//is this deadlocked?
	public Boolean isDeadlocked(Task[] Tasks) {
		for (int i =0; i < Tasks.length; i++) {
			if (!Tasks[i].abort && !Tasks[i].finished()) { //if task is aborted, terminated, or blocked then deadlock is true
				int second = Tasks[i].getsecond()-1;
				int third = Tasks[i].getthird(); 
				
				if(avaliable[second] >= third) { //if resource type is more than or equal to number requested 
					return false; //no deadlock 
				}
			}
		}
		//if tasks can't be allocated
		return true; //else there will be a deadlock because we cannot allocate
	}
	
	/**
	 * Prints all the tasks and the corresponding total time taken for each class, 
	 * the total time blocked for each task, and the percentage of time blocked and finishing time
	 * and if the task was aborted or not.   
	 * @param an array of Task objects 
	 */
	//print the output
	public void print(Task[] task) {
		System.out.println("FIFO");
		DecimalFormat decimal = new DecimalFormat("####");
		int totalTime =0; //total time for all the tasks
		int totalBlockedTime =0; //total time all the tasks spent waiting
		for (Task t: task) {
			System.out.print("Task " + t.id+"         "); //for each task print the ID
			if (t.abort) {
				System.out.print("aborted"); //checks if the task was ever aborted
			}
			else {
				System.out.print(t.finishTime + "          "); //prints out finish time for one task
				System.out.print(t.numBlocked + "          "); //prints out waiting time for one task
				float print = (float) t.numBlocked/t.finishTime; //percentage of waiting to finish time for one task
				System.out.print(decimal.format(print*100)+"%"); //print
			}
			System.out.println(); //new line
			totalTime += t.finishTime; //add the finish time to the total finish time
			totalBlockedTime += t.numBlocked; //add the waiting time ot the total waiting time
		}
		
		System.out.print("Total "+ "         "); //printing out the total
		float print =  (float) totalBlockedTime/totalTime; //calculate the fraction of waiting to finish time for ALL tasks
		System.out.print(totalTime + "          "); //the total time for ALL tasks
		System.out.print(totalBlockedTime + "          "); //total time waiting for ALL tasks
		System.out.print(decimal.format(print*100)+"%"); //print 
		System.out.println();
	}
	
	/**
	 * Checks if Task objects are finished or not  
	 * @param an array of Task objects 
	 * @return true if each Task object in the Tasks array are finished (according to the finished method in Tasks) 
	 */
	public boolean isFinished(Task[] Tasks) {
		for(Task t: Tasks) {
			if (!t.finished()) { //checks if there is any Task t in the Tasks array that is not finished
			 return false;
		}
	}
	 return true; 
	 }
	
	/**
	 * Checks if Task object can be allocated or not  
	 * @param a Task object
	 * @return true if the task can be allocated and allocates the task and false if the task cannot be allocated
	 */
	public boolean toAllocate(Task task) {
		
		int resourceType = task.getsecond()-1; 
		int numRequested = task.getthird();
		if (avaliable[resourceType] - numRequested < 0) { //in the case where there is not enough resources
			task.numBlocked++; //task is now blocked
			return false;
		}
		else { //if optimistic manager CAN allocate
			task.index++; //next operation
			avaliable[resourceType] -= numRequested;  //remove from avaliable 
			allocate[task.id-1][resourceType] += numRequested; //because now we can add it to allocate
			return true;
		}
	}
	
	
}

