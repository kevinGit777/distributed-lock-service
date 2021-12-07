/*
 * Written by:
 * - Haifan Wu (hxw170013)
 * - Li Feng (lxf200001)
 * - Nicolas Amezquita (nxa200018)
 * 
 * For:
 * CS6378 Advanced Operating Systems (Fall 2021)
 * Professor Neeraj Mittal - UT Dallas
 * 
 * Monday, December 6th, 2021
*/

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;
import java.sql.Timestamp;

public class Application_Distributed_Lock_Service{

	NodeID myID;
	String configFile;
	int request_delay;
	int execution_time;
	int num_requests;
	Dlock lock;
	Random rnd;
	List<String> exec_times;

	public Application_Distributed_Lock_Service(NodeID identifier, String configFile, int avg_inter_request_delay, int avg_cs_execution_time, int num_cs_requests) {
		this.myID = identifier;
		this.configFile = configFile;
		this.request_delay = avg_inter_request_delay;
		this.execution_time = avg_cs_execution_time;
		this.num_requests = num_cs_requests;
		this.lock = new Dlock(this.myID, this.configFile);
		this.rnd = new Random();
		this.exec_times = new ArrayList<String>();
	}

	public void run() {
		for (int i = 0; i < num_requests; i++){
			lock.lock(System.currentTimeMillis());
			System.out.println("in cs");
			execute_cs();
			System.out.println("out cs");
			lock.unlock();

			try 
			{
				Thread.sleep(rand_exp_dist_prob_time(request_delay));
			} 
			catch(Exception exc)
			{
				System.out.println("ERROR! Request Delay: " + exc);
			}
			
		}
        lock.close();
		output_cs_exec_time(myID.getID() + "-" + configFile);
	}

    private void execute_cs()
    {
    	
		try 
			{
				long current_time = get_GMT_timestamp();
				String time_range = myID.getID()+" "+ current_time + ",";
				
				Thread.sleep(rand_exp_dist_prob_time(execution_time));
				
				current_time = get_GMT_timestamp();
				time_range += current_time;

				exec_times.add(time_range);
			} 
			catch(Exception exc)
			{
				System.out.println("ERROR! Execution Time: " + exc);
			}
    }

	private long rand_exp_dist_prob_time(int mean){
		long time = (long)Math.floor(Math.log(1-rnd.nextDouble())/(-1.0/(double)(mean*1000)));
		return time;
	}

	private long get_GMT_timestamp(){
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		return timestamp.getTime();
	}

	private void output_cs_exec_time(String file_name) {
		try {
			File output_file = new File(file_name);
			if (output_file.createNewFile()) {
				System.out.println("Log file <" + output_file.getName() + "> doesn't exist. Creating...");
			} else {
				System.out.println("Log file <" + output_file.getName() + "> already exists. Updating...");
			}

			BufferedWriter writer = new BufferedWriter(new FileWriter(output_file.getName()));
			for (int i = 0; i < exec_times.size(); i++) {
				String output_line = exec_times.get(i);
				writer.write(output_line + "\n");
			}
			writer.close();
			System.out.println("Log file <" + output_file.getName() + "> done!");
		} catch (IOException ioe) {
			System.out.println("ERROR occurred with the output file.");
			ioe.printStackTrace();
		}
	}
}
