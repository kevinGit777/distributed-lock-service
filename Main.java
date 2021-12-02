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

public class Main
{
	public static void main(String[] args)
	{
		if (args.length != 5){
			System.out.println("ERROR! Argument missing. Arguments passed: " + args.length + "/5");
			System.exit(0);
		}

		//Read Arguments from command line
		NodeID id = new NodeID(Integer.parseInt(args[0]));
		String configFile = args[1];
		int avg_inter_request_delay = Integer.parseInt(args[2]);
		int avg_cs_execution_time = Integer.parseInt(args[3]);
		int num_cs_requests = Integer.parseInt(args[4]);
		
		//Launch application and wait for it to terminate
		Application_Distributed_Lock_Service myApp = new Application_Distributed_Lock_Service(id, configFile, avg_inter_request_delay, avg_cs_execution_time, num_cs_requests);
		myApp.run();	
	}
}
