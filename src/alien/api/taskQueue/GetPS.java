package alien.api.taskQueue;

import java.util.ArrayList;
import java.util.List;

import alien.api.Request;
import alien.taskQueue.Job;
import alien.taskQueue.TaskQueueUtils;

/**
 * Get a JDL object
 * 
 * @author ron
 * @since Jun 05, 2011
 */
public class GetPS extends Request {



	/**
	 * 
	 */
	private static final long serialVersionUID = -1486633687303580187L;

	/**
	 * 
	 */
	private List<Job> jobs;

	private final List<String> states;
	
	private final List<String> users;
	
	private final List<String> sites;
	
	private final List<String> nodes;
	
	private final List<String> mjobs;
	
	private final List<String> jobid;
	
	private int  limit = 0;
	
	/**
	 * @param loadJDL 
	 * @param running 
	 */
	public GetPS(final List<String> states,final List<String> users,final List<String> sites,
			final List<String> nodes,final List<String> mjobs,final List<String> jobid, final int limit){
		this.states = states;
		this.users = users;
		this.sites = sites;
		this.nodes = nodes;
		this.mjobs = mjobs;
		this.jobid = jobid;
		this.limit = limit;
	}
	
	@Override
	public void run() {
		this.jobs = TaskQueueUtils.getPS(states, users, sites, nodes, mjobs, jobid, limit);
	}
	
	/**
	 * @return a JDL
	 */
	public List<Job> returnPS(){
		return this.jobs;
	}
	
	@Override
	public String toString() {
		return "Asked for PS :  reply is: "+this.jobs;
	}
}