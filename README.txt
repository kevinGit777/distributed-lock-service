----- PROJECT 3 -----

CS6378 Advanced Operating Systems (Fall 2021)
Professor Neeraj Mittal - UT Dallas
Monday, December 6th, 2021

Written by:
- Haifan Wu (hxw170013)
- Li Feng (lxf200001)
- Nicolas Amezquita (nxa200018)
 
---How to run the program---
1. Extract all the files in the desired UTD machine (dcXX.utdallas.edu)
2. Compile the Java classes using the command <javac *.java>
3. Run Main using the command <java Main 'NodeID' 'config file' 'avg request delay' 'avg execution time' 'critical sections'>
	a) 'NodeID' = the unique identifier associated with the node
	b) 'config file' = the name of the configuration file
	c) 'avg request delay' = the average inter request delay (in SECONDS)
	d) 'avg execution time' = the average cs-execution time (in SECONDS)
	e) 'critical sections' = the number of critical section requests a node should generate
4. Repeat the above steps for every Node

---How to test the correctness of the program---
1. Run the program following the instructions above
2. Run Test_for_Correctness using the command <java Test_for_Correctness 'config file'>
	a) 'config file' = the name of the same configuration file used above