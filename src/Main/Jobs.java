package Main;

/**
 * 
 * @author Cole Christie
 * 
 *         The Jobs class is a struct that holds data relvant to simple job
 *         queue management
 * 
 */
public class Jobs {
	private String work;
	private String issuedTo;
	private long timeIssued;

	/**
	 * Default constructor Initializes values to empty set
	 */
	public Jobs() {
		work = "";
		issuedTo = "";
		timeIssued = 0;
	}

	/**
	 * Constructor that sets work to passed string
	 * 
	 * @param PassedWork
	 */
	public Jobs(String PassedWork) {
		work = PassedWork;
		issuedTo = "";
		timeIssued = 0;
	}

	/**
	 * Constructor that sets work to passed string, also sets who was issued the
	 * job and when it was issued
	 * 
	 * @param PassedWork
	 * @param PassedIssuedTo
	 */
	public Jobs(String PassedWork, String PassedIssuedTo) {
		work = PassedWork;
		issuedTo = PassedIssuedTo;
		timeIssued = System.nanoTime();
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