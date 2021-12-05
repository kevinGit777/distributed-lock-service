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

public class Application_Distributed_Lock_Service{

	NodeID myID;
	String configFile;
	int request_delay;
	int execution_time;
	int num_requests;
	Dlock lock;
	Random rnd;
	public Application_Distributed_Lock_Service(NodeID identifier, String configFile, int avg_inter_request_delay, int avg_cs_execution_time, int num_cs_requests) {
		this.myID = identifier;
		this.configFile = configFile;
		this.request_delay = avg_inter_request_delay;
		this.execution_time = avg_cs_execution_time;
		this.num_requests = num_cs_requests;
		this.lock = new Dlock(this.myID, this.configFile);
		this.rnd = new Random();
	}

	public void run() {
		for (int i = 0; i < num_requests; i++){
			lock.lock(i);
			execute_cs();
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
	}

    private void execute_cs()
    {
		try 
			{
				System.out.println("Node |" + myID + "| started excuting CS.");
				Thread.sleep(rand_exp_dist_prob_time(execution_time));
				System.out.println("Node |" + myID + "| finished excuting CS.");
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

}
