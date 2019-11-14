/**
 * The Main class is responsible for reading in the input file and validating it. It is also 
 * responsible for calling the two methods and feeding in the data from the input file into these classes.
 * 
 * @author Christina Liu 
 *
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws FileNotFoundException{
		//verify that the command line argument exists 
			if (args.length == 0 ) {
				System.err.println("Usage Error: the program expects file name as an argument.\n");
				System.exit(1);
			}

			File input = new File(args[0]);
			
			//verify that command line argument contains a name of an existing file  
			if (!input.exists()){
				System.err.println("Error: the file "+input.getAbsolutePath()+" does not exist.\n");
				System.exit(1);
			}
			
			if (!input.canRead()){
				System.err.println("Error: the file "+input.getAbsolutePath()+
												" cannot be opened for reading.\n");
				System.exit(1);
			}
			
			//open the file for reading 
			Scanner readInput = null; 
			
			try {
				readInput = new Scanner (input ) ;
			} catch (FileNotFoundException e) {
				System.err.println("Error: the file "+input.getAbsolutePath()+
												" cannot be opened for reading.\n");
				System.exit(1);
			}
			
			//store input first 
			
			String line = readInput.nextLine();
			String split[] = line.split("\\s+");
			
			int T = Integer.parseInt(split[0]); //number of tasks
			int R = Integer.parseInt(split[1]); //number of resource types
			
			Optimistic Optimistic = new Optimistic(T,R); //optimistic method
			Banker Banker = new Banker(T,R); //banker method

			//num of avaliable resources 
			for (int i =0; i<split.length-2;i++) {
				Optimistic.avaliable[i]=Integer.parseInt(split[i+2]); 
				Banker.avaliable[i]=Integer.parseInt(split[i+2]);
			}
			
			//read input lines into tasks
			Task[] Tasks = new Task[T];
			for(int j=0;j< T; j++) {
				Tasks[j]=new Task(j);
			}
			
			//reads in each line as a new task 
			while(readInput.hasNext()) {
				String in = readInput.nextLine();
				if (!in.contentEquals("")) {
					String separate[] = in.split("\\s+");
					int tasknum = Integer.parseInt(separate[1]);
					Tasks[tasknum-1].input.add(in);
				}
			}
				
			//run fifo method on the tasks
			Optimistic.main(Tasks);
			
			//reset tasks for Banker class
			for (int t=0; t < T; t++) {
				Tasks[t].reset(t);
			}
			
			//run banker method on the tasks
			Banker.main(Tasks);
			
			
}
}