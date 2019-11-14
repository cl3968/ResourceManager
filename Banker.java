/**
 * The Banker class is responsible for doing resource allocation using the banker’s 
 * algorithm of Dijkstra.
 * 
 * @author Christina Liu 
 *
 */

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Banker {
	int T, R, cycle; //number of tasks, number of resources, and number of cycles 
	
	int[] avaliable; //avaliability matrix
	int[][] allocate; //allocation matrix
	
	int[][] max; //added a maximum number of resources matrix
	int[][] need; //and a necessary number of resources matrix
	
	int request; //number of requests
	int[][] numRequest; //requests matrix
	
	boolean allfinished; //see if the Banker method is finished
	
	ArrayList<Task> readyQueue = new ArrayList<Task>(); //for all the ready processes
	ArrayList<Task> blockQueue = new ArrayList<Task>(); //for all the blocked processes
	ArrayList<Task> waitQueue = new ArrayList<Task>(); //for all the processes waiting 
	
	//constructor that initializes all the global variables
	public Banker(int t, int r){
		T=t;R=r;
		cycle =0;
		
		avaliable = new int[R];
		allocate = new int[T][R];
		
		max=new int[T][R];
		need = new int[T][R];
		
		numRequest=new int[T][R];
		allfinished=false;
		
	}
	
	/**
	 * Performs each operation in the Task object array every cycle, checking 
	 * whether or not the state is safe or not and then allocates.  
	 * @param an array of Task objects 
	 */
	//executes methods until blocked
	public void main(Task[] Tasks) {
		
		//executing main method until all tasks are finished 
		while(!allfinished) {

			waitQueue.clear(); //clear waiting processes 
			int[] release=new int[R];

			for (int i = 0; i < release.length; i++) {
				release[i]=0; //initialize array
			}
			cycle ++;
			int [][] releasedN = new int[T][R]; //how many released resources are needed per cycle
			int [][] releasedA = new int[T][R]; //how many released resources are allocated per cycle
			for(int i=0; i <T; i++) { 
				for(int j=0; j<R;j++) {
					releasedN[i][j]=0; //initialize array
					releasedA[i][j]=0; //initialize array
				}
			}
			
			//making sure the blocked task is ready to allocate
			while(!blockQueue.isEmpty()) {
				Task task = blockQueue.get(0);
				blockQueue.remove(0); //remove from block queue 

				boolean isAllocate = toAllocate(Tasks, task);

				if (isAllocate) {
					readyQueue.add(task); //add blocked task to ready queue
				}
				else {
					waitQueue.add(task); //the blocked task is now waiting
				}
			}
			blockQueue.addAll(waitQueue); //add waiting processes to blocked queue 
			
			//actually reading in tasks
			for (int i =0; i <Tasks.length; i++) {
				Task t = Tasks[i];
				if (!blockQueue.contains(t) && !readyQueue.contains(t)) { //checking tasks that arent blocked
					if (t.computeTime == 0) { //checking if tasks are delayed
						
						//now let's check all the operations
						if (t.getOperation() == 1) { //if initiate, banker has to make sure that there is enough avaliable resources
							int taskNumber = t.getfirst();
							int resourceType = t.getsecond();
							int initialClaim = t.getthird();
							if (initialClaim > avaliable[resourceType-1]) {
								Tasks[taskNumber-1].abortThis(); //if not enough avaliable resource then abort the task
							}
							else {
								max[taskNumber-1][resourceType-1]=initialClaim; //else add the initial claim to max
								need[taskNumber-1][resourceType-1]=initialClaim; //and add to need as well
								t.index++; //next operation
							}
						}
						else if (t.getOperation() == 2) { //if request banker has to make sure it can make that request or not 
							
							boolean isAllocated=toAllocate(Tasks, t);
							
							if (!isAllocated) {
								blockQueue.add(t); //if it not able to allocate, put task in block queue
							}
						}
						else if (t.getOperation() == 3) { //if release banker has to update arrays and set finishing time
						
							
							int taskNumber = t.getfirst(); 
							int resourceType = t.getsecond();
							int numberReleased = t.getthird(); //number of resources released
							release[resourceType-1]+=numberReleased; //add to release array
							releasedA[taskNumber-1][resourceType-1] =numberReleased; //add to release allocation
							releasedN[taskNumber-1][resourceType-1] = numberReleased; //also add to release need
							t.index++; //next operation
							if(t.finished()) {
								t.finishTime = cycle; //set finishing time
							}
						}
						else if (t.getOperation() == 4) { //if compute the process is computing for several cycles, so there are no requests or releases 
							int taskNumber = t.getfirst()-1;
							int numofCycles = t.getsecond()-1;
							Tasks[taskNumber].computeTime = numofCycles; //update computing time
							Tasks[taskNumber].index++; //go to next operation
							if(t.finished() && t.computeTime==0) {
								t.finishTime=cycle; //set finishing time to cycle number 
							}
						}
					}
					//else terminate does nothing 
					else { //if tasks are waiting 
						t.computeTime--;
						if(t.computeTime==0 && t.finished()) {
							t.finishTime=cycle; //update finishing time
						}
					}
				}
			}
			for (int a=0; a <R; a++) { //collect resources that were released this cycle 
				avaliable[a] += release[a];
			}
			for(int c=0;c<T;c++) { //modify changed matrix for allocated resources and needed resources
				for (int d=0; d<R;d++) {
					allocate[c][d] -= releasedA[c][d];
					need[c][d] += releasedN[c][d];
				}
			}
			
			
			Task[] blockToReady = readyQueue.toArray(new Task[0]);
			for (int b=0; b < blockToReady.length; b++) {
				readyQueue.remove(blockToReady[b]); //removing task from ready queue
			}
			
			allfinished= isFinished(Tasks); //checking if tasks are finished and therefore stopping the while loop
		}
		print(Tasks); //printing the output 
	}
	
	/**
	 * Checks if the array of Task objects is safe or not   
	 * @param an array of Task objects 
	 * @return true if the state is safe and false if the state is unsafe
	 */
	//is this a safe state?
	public Boolean isSafe(Task[] Tasks) {
		
		int[] intermediate = new int[R];
		boolean[] finish = new boolean[T]; //stores booleans for the Tasks array, if it is finished or not
		for (int i =0; i < Tasks.length; i++) {
			if (Tasks[i].abort || Tasks[i].finished()) { //if task is aborted or finished
				finish[i]=true;	//initializing finish array 
			}
			else {
				finish[i]=false;
			}
		}
		for (int i=0; i <R; i++) {
			intermediate[i]=avaliable[i]; //initialize array to be avaliable 
		}
		//look for processes that are not finished and the request exceeds intermediate/avaliable
		for (int i =0; i < T; i++) {
			boolean canFinish = true; //can this finish normally 

			for(int j=0;j<R;j++){
				if(need[i][j]>intermediate[j]){ //if there is more needed than avaliable 
					canFinish = false; //then tasks cannot finish normally 
					break;
				}
			}
			if(finish[i]==false && canFinish){ //if process is not finished yet and can finish normally 
				for(int j=0;j<R;j++){
					intermediate[j] = intermediate[j] + allocate[i][j]; //add allocated process to avaliable process
				}
				finish[i] = true; //change finished to true 
				i = -1; //traverse unfinished process
			}
		}
		
		for (int j =0; j < T; j++) { //traverse through finish matrix to see if finished and last index (so to see if last Task is finished)
			if (finish[j]==true && j ==T-1) { //if the tasks are safe states
				return true; //then return true
			}
		}
	
		return false; //is danger states, return false
		
	}
	
	/**
	 * Prints all the tasks and the corresponding total time taken for each class, 
	 * the total time blocked for each task, and the percentage of time blocked and finishing time
	 * and if the task was aborted or not. (same method as optimistic)
	 * @param an array of Task objects 
	 */
	public void print(Task[] task) {
		System.out.println("BANKER'S");
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
	 * Checks if Task objects are finished or not (same method as optimistic) 
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
	 * @param an array of Task objects and a Task object
	 * @return true if the task can be allocated and allocates the task and false if the task cannot be allocated
	 */
	public boolean toAllocate(Task[] Tasks, Task task) {
		
		int taskNumber = task.getfirst()-1;
		int resourceType = task.getsecond()-1;
		int numRequested = task.getthird();
		for (int i = 0; i < T; i++) {
			for (int j=0; j < R; j++) {
				numRequest[taskNumber][resourceType]=0; //set all requested resources to 0
			}
		}
		request = taskNumber;
		numRequest[taskNumber][resourceType]=numRequested;
		
		//then try to allocate 
		for (int i=0; i < R; i++) {
			if(need[request][i]-numRequest[request][i]<0) { //if the allocated resource exceeds the maximum amount of claim
				avaliable[i]=avaliable[i]+numRequest[request][i]; //then banker would have to release the resources
				allocate[request][i] = allocate[request][i] - numRequest[request][i];
				need[request][i] = need[request][i] + numRequest[request][i];
				task.abortThis(); //and abort the task
				return true;
				
			}
			else { //but if allocation does not exceed the maximum claim then it is able to allocate
				avaliable[i] = avaliable[i] - numRequest[request][i];
				allocate[request][i] =allocate [request][i] + numRequest[request][i];
				need[request][i] =need [request][i] - numRequest[request][i];
			}
		}
		if(isSafe(Tasks)) { //check if the array of Task objects is safe 
			task.index++; //next operation
			for (int i =0; i < R; i++) {
				numRequest[request][i] = 0; //set the request to 0 and continue
			}
			return true;
		}
		else { //if it is not safe then 
			for (int i =0; i < R; i++) { //start releasing all the resources requested in this cycle 
				avaliable[i] = avaliable[i] + numRequest[request][i];
				allocate[request][i] = allocate[request][i] - numRequest[request][i];
				need[request][i] = need[request][i]+ numRequest[request][i];
				numRequest[request][i]=0; //and then set the request to 0
			}
			
			task.numBlocked++; //add to the waiting time of the task
			return false;
		}
		
	}
}

