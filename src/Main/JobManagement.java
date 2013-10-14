package Main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class JobManagement {
	private Charset ENCODING = StandardCharsets.UTF_8;
	private ArrayList<String> jobqueue;
	private ArrayList<String> jobsent;
	
	public JobManagement{
		jobqueue = new ArrayList<String>();
		jobsent = new ArrayList<String>();
	}

	public void Load(String filepath) throws IOException {
		Path path = Paths.get(filepath);
	    try (Scanner scanner =  new Scanner(path, ENCODING.name())){
		  while (scanner.hasNextLine()){
		    //Read each line into the array list
		    jobqueue.add(scanner.nextLine());
		  }      
	    }
	}

	public String Issue{
		int size = jobqueue.size();		
		if (size > 0)
		{
			String fetch = jobqueue.get(0);	// Pull the data at spot 0
			jobqueue.remove(0);  //Remove it from the queue
			jobsent.add(fetch); //Add that data into the sent list			
			return fetch; //Return the extracted data
		}
	}
}