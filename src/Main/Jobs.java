package Main;

import java.util.ArrayList;

/**
 * 
 * @author Cole Christie
 * 
 *         The Jobs class is a simple storage structure that holds data relevant
 *         to simple job queue management
 * 
 */
public class Jobs {
	private String work;
	private String issuedTo;
	private long timeIssued;
	private int SecurityLevel;
	private String OSspecific;
	private ArrayList<String> ReturnedErrors;
	private ArrayList<String> ReturnedOutput;
	private int JobID;

	/**
	 * Default constructor - Initializes values to empty set
	 */
	public Jobs() {
		work = "";
		issuedTo = "";
		timeIssued = 0;
		SecurityLevel = 0;
		OSspecific = "any";
		ReturnedErrors = new ArrayList<String>();
		ReturnedOutput = new ArrayList<String>();
		JobID = 0;
	}

	/**
	 * Constructor that sets: work field
	 * 
	 * @param PassedWork
	 */
	public Jobs(String PassedWork) {
		work = PassedWork;
		issuedTo = "";
		timeIssued = 0;
		SecurityLevel = 0;
		OSspecific = "any";
		ReturnedErrors = new ArrayList<String>();
		ReturnedOutput = new ArrayList<String>();
		JobID = 0;
	}

	/**
	 * Constructor that sets: work field, OS required field
	 * 
	 * @param PassedWork
	 */
	public Jobs(String PassedWork, String PassedOSspecific) {
		work = PassedWork;
		issuedTo = "";
		timeIssued = 0;
		SecurityLevel = 0;
		OSspecific = PassedOSspecific;
		ReturnedErrors = new ArrayList<String>();
		ReturnedOutput = new ArrayList<String>();
		JobID = 0;
	}

	/**
	 * Constructor that sets: work field, OS required field, required security
	 * level
	 * 
	 * @param PassedWork
	 * @param PassedIssuedTo
	 */
	public Jobs(String PassedWork, String PassedOSspecific, int PassedSecurityLevel) {
		work = PassedWork;
		issuedTo = "";
		timeIssued = 0;
		SecurityLevel = PassedSecurityLevel;
		OSspecific = PassedOSspecific;
		ReturnedErrors = new ArrayList<String>();
		ReturnedOutput = new ArrayList<String>();
		JobID = 0;
	}

	/**
	 * Returns the job ID
	 * 
	 * @return
	 */
	public int GetJobID() {
		return JobID;
	}

	/**
	 * Sets the job ID
	 * 
	 * @param DesiredJobID
	 */
	public void SetJobID(int DesiredJobID) {
		JobID = DesiredJobID;
	}	
	
	/**
	 * Returns the SIZE of the output (results) cached with the job results
	 * 
	 * @return
	 */
	public int GetOutputListSize() {
		return ReturnedOutput.size();
	}
	
	/**
	 * Returns the output (results) cached with the job results
	 * 
	 * @return
	 */
	public ArrayList<String> GetOutputList() {
		return ReturnedOutput;
	}

	/**
	 * Inserts another string into the output storage cache
	 * 
	 * @param SingleString
	 */
	public void AddToOutputList(String OutputString) {
		ReturnedOutput.add(OutputString);
	}

	/**
	 * Returns the SIZE of the errors cached with the job results
	 * 
	 * @return
	 */
	public int GetErrorListSize() {
		return ReturnedErrors.size();
	}
	
	/**
	 * Returns the errors cached with the job results
	 * 
	 * @return
	 */
	public ArrayList<String> GetErrorList() {
		return ReturnedErrors;
	}

	/**
	 * Inserts another string into the error storage cache
	 * 
	 * @param SingleString
	 */
	public void AddToErrorList(String ErrorString) {
		ReturnedErrors.add(ErrorString);
	}

	/**
	 * Returns the security level
	 * 
	 * @return
	 */
	public int GetSecurityLevel() {
		return SecurityLevel;
	}

	/**
	 * Sets the work string
	 * 
	 * @param PassedWork
	 */
	public void SetSecurityLevel(int PassedSecurityLevel) {
		SecurityLevel = PassedSecurityLevel;
	}

	/**
	 * Returns the OS required to run the job (if any)
	 * 
	 * @return
	 */
	public String GetOSspecific() {
		return OSspecific;
	}

	/**
	 * Sets the OS required to run the job
	 * 
	 * @param PassedWork
	 */
	public void SetOSspecific(String PassedOSspecific) {
		OSspecific = PassedOSspecific;
	}

	/**
	 * Returns the work string
	 * 
	 * @return
	 */
	public String GetWork() {
		return work;
	}

	/**
	 * Sets the work string
	 * 
	 * @param PassedWork
	 */
	public void SetWork(String PassedWork) {
		work = PassedWork;
	}

	/**
	 * Returns who was issued this job
	 * 
	 * @return
	 */
	public String GetIussed() {
		return issuedTo;
	}

	/**
	 * Sets who was issued this job
	 * 
	 * @param PassedIssuedTo
	 */
	public void SetIssued(String PassedIssuedTo) {
		issuedTo = PassedIssuedTo;
	}

	/**
	 * Returns the time the job was issued (in nano seconds)
	 * 
	 * @return
	 */
	public long GetTimeIssued() {
		return timeIssued;
	}

	/**
	 * Sets the time the job was issued to right now
	 */
	public void SetTimeIssued() {
		timeIssued = System.nanoTime();
	}

	/**
	 * Get the number of nano seconds that have elapsed since the job was issued
	 * 
	 * @return
	 */
	public long GetElpased() {
		long currentTime = System.nanoTime();
		long elpasedTime = currentTime - timeIssued;
		if (elpasedTime < 0) {
			elpasedTime = 0;
		}
		return elpasedTime;
	}
}