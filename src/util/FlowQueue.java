package util;

import java.util.LinkedList;
import java.util.Queue;

public class FlowQueue {
	private Queue<String> q = new LinkedList<String>();
	
	public Boolean isEmpty() {
		return q.isEmpty();
	}
	
	public void add(String s) {
		q.add(s);
	}
	
	public String poll() {
		return q.poll();
	}
}
