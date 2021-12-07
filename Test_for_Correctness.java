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
import java.sql.Time;

public class Test_for_Correctness
{
	public static void main(String[] args)
	{
		if (args.length != 1)
		{
			System.out.println("ERROR! Config file needed.");
			System.exit(0);
		}
		
		//Fetch list of all files
		List<String> files = new ArrayList<String>();
		File current_dir = new File(System.getProperty("user.dir"));

		for (String file_name : current_dir.list()) 
		{
			if (file_name.endsWith(args[0]) && !file_name.equals(args[0]))
			{
				files.add(file_name);
			}
        }

		//Read each time interval of each file
		List<String> critical_sections = new ArrayList<String>();
		for (String file : files) 
		{
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String line = reader.readLine();
				while (line != null) {
					critical_sections.add(line);
					line = reader.readLine();
				}
				reader.close();
			} catch (Exception exc) {
				System.out.println("ERROR! " + exc);
			}
        }
		
		//Compare all intevals
		boolean overlap = false;
		int overlap_count = 0;
		Collections.sort(critical_sections);

		for (String str : critical_sections) 
		{
			System.out.println(str);
        }

		for (int i = 1; i < critical_sections.size(); i++) {
			String ti1 = critical_sections.get(i-1);
			String ti2 = critical_sections.get(i);
			long start_time = Long.parseLong(ti2.split(",")[0]);
			long end_time = Long.parseLong(ti1.split(",")[1]);
			if (start_time < end_time)
			{
				overlap = true;
				overlap_count++;
			}
			System.out.println("Comparing " + (i-1) + " with " + i + " = " + overlap + "(count = " + overlap_count + ")");
		}

		//Determine correctness
		if (!overlap)
		{
			System.out.println("SUCCESS! No Critical Sections overlapped in their execution.");
		}
		else
		{
			System.out.println("FAILURE! " + overlap_count + " Critical Sections overlapped in their execution.");
		}
	}

}
