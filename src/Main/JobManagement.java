package Main;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Simple job management class. Supports reading jobs (one per line) from a
 * file. Also supports issuing those jobs to clients and a simple expiration
 * system.
 * 
 * @author Cole Christie
 * 
 */
public class JobManagement {
	private ArrayList<Jobs> jobqueue;
	private ArrayList<Jobs> jobsent;
	private ArrayList<Jobs> jobcomplete;
	private Charset ENCODING = StandardCharsets.UTF_8;
	private int IDcounter;

	/**
	 * Default constructor.
	 */
	public JobManagement() {
		jobqueue = new ArrayList<Jobs>();
		jobsent = new ArrayList<Jobs>();
		jobcomplete = new ArrayList<Jobs>();
		IDcounter = 1;
	}

	/**
	 * Returns the number of jobs yet to be assigned
	 * 
	 * @return
	 */
	public int UnassignedCount() {
		return jobqueue.size();
	}

	/**
	 * Returns the number of jobs that have been assigned, but not yet
	 * acknowledged as complete
	 * 
	 * @return
	 */
	public int AssignedCount() {
		return jobsent.size();
	}

	/**
	 * Reads a list of jobs (one per line) from a file
	 * 
	 * @param filepath
	 * @throws IOException
	 */
	public int Load(String filepath) throws IOException {
		int AddeCounter = 0;
		Path path = Paths.get(filepath);
		try (Scanner scanner = new Scanner(path, ENCODING.name())) {
			while (scanner.hasNextLine()) {
				// Read each line into the array list
				String line = scanner.nextLine();
				if (line != null && !line.isEmpty()) {
					Jobs jobUnit = new Jobs(line);
					jobqueue.add(jobUnit);
					AddeCounter++;
				}
			}
		}
		return AddeCounter;
	}

	/**
	 * Sets up to receive the completed work
	 */
	public int SetupResultStorage(String JobComplete) {
		Jobs jobUnit = new Jobs(JobComplete);
		jobcomplete.add(jobUnit);
		jobUnit.SetJobID(IDcounter);
		int toReturn = IDcounter;
		IDcounter++;
		return toReturn;
	}

	/**
	 * Saves a line of either ERROR or OUTPUT to a specific job ID
	 */
	public void StoreResutls(int JobID, String ToStore, String Type) {
		// Look for, and load, the job that has that JobID
		int fetch = JobSearch(JobID, jobcomplete);
		Jobs jobUnit = jobcomplete.get(fetch);

		// If we can find that job...
		if (fetch >= 0) {
			if (Type.equalsIgnoreCase("ERROR")) {
				jobUnit.AddToErrorList(ToStore);
			} else if (Type.equalsIgnoreCase("OUTPUT")) {
				jobUnit.AddToOutputList(ToStore);
			}
		}
	}

	/**
	 * Populates the job queue with 10 sample jobs for windows based systems
	 */
	public void SampleWindows() {
		for (int loop = 0; loop < 10; loop++) {
			Jobs jobUnit = new Jobs("time /T", "Windows");
			jobqueue.add(jobUnit);
		}
	}

	/**
	 * Populates the job queue with 10 sample jobs for linux/unix based systems
	 */
	public void SampleLinux() {
		for (int loop = 0; loop < 10; loop++) {
			Jobs jobUnit = new Jobs("date", "Linux");
			jobqueue.add(jobUnit);
		}
	}

	/**
	 * Populates the job queue with 10 sample jobs for ANY OS
	 */
	public void Sample() {
		for (int loop = 0; loop < 10; loop++) {
			Jobs jobUnit = new Jobs("java -version");
			jobqueue.add(jobUnit);
		}
	}

	// TODO Setup job sign off code (server acknowledges job is complete)
	
	/**
	 * Assigns a job to a client and returns the string contain what that job is
	 * (what work needs to be done)
	 * 
	 * @param clientsName
	 * @return
	 */
	public String Assign(String clientsName, String OS, int SecLev) {
		int size = jobqueue.size();
		if (size > 0) {
			int fetch = 0;
			if ((OS.toLowerCase().contains("windows")) || (OS.toLowerCase().contains("linux"))
					|| (OS.toLowerCase().contains("mac"))) {
				fetch = JobSearch(OS, SecLev, jobqueue);

				// If fetch is now -1 than no jobs exist for that OS type
				if (fetch == -1) {
					return "";
				}
			} else {
				fetch = JobSearch("any", SecLev, jobqueue);

				// If fetch is now -1 than no jobs exist for generic clients
				if (fetch == -1) {
					return "";
				}
			}
			// If we are here then we have a valid index to work from
			Jobs jobUnit = jobqueue.get(fetch); // Pull the data at that index
			jobqueue.remove(fetch); // Remove it from the queue
			jobUnit.SetIssued(clientsName); // Add who it was issued to
			jobUnit.SetTimeIssued(); // Update the time issued to now
			jobsent.add(jobUnit); // Add that data into the issued list
			String jobWork = jobUnit.GetWork();
			return jobWork; // Return the extracted data
		} else {
			return "";
		}
	}

	/**
	 * This is passed the OS to search for and returns the index into the array
	 * list of the first job that matches that OS or 0 if no jobs exist for that
	 * OS
	 * 
	 * @param OS
	 * @return
	 */
	private int JobSearch(String OS, int SecLev, ArrayList<Jobs> SearchThrough) {
		int size = SearchThrough.size();
		int scan = 0;

		// Simplify searches (remove anything extra that was passed
		if (OS.toLowerCase().contains("windows")) {
			OS = "windows";
		} else if (OS.toLowerCase().contains("linux")) {
			OS = "linux";
		} else if (OS.toLowerCase().contains("mac")) {
			OS = "linux";
		}

		while (scan < size) {
			Jobs looking = SearchThrough.get(scan);
			// Look for matching OS or jobs that can be ran under any OS
			if ((looking.GetOSspecific().toLowerCase().contains(OS.toLowerCase()))
					|| (looking.GetOSspecific().toLowerCase().contains("any"))) {
				// Make sure the security level is acceptable
				if (looking.GetSecurityLevel() <= SecLev) {
					return scan;
				}
			}
			scan++;
		}
		return -1;
	}

	/**
	 * This is passed the JobID to search for and returns the index into the
	 * array
	 * 
	 * @param JobID
	 * @return
	 */
	private int JobSearch(int JobID, ArrayList<Jobs> SearchThrough) {
		int size = SearchThrough.size();
		int scan = 0;

		if (JobID < 0) {
			return -1;
		}

		while (scan < size) {
			Jobs looking = SearchThrough.get(scan);
			// Look for matching OS or jobs that can be ran under any OS
			if (looking.GetJobIT() == JobID) {
				return scan;
			}
			scan++;
		}
		return -1;
	}

	/**
	 * Clears the queue of jobs that have NOT already been sent to clients
	 */
	public void ClearUnsentQueue() {
		jobqueue.clear();
	}

	/**
	 * Clears the queue of jobs that HAVE already been sent to clients
	 */
	public void ClearSentQueue() {
		jobsent.clear();
	}
}