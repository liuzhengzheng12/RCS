package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SendMap { 
	private Map<String, RouteTable> m = new HashMap<String, RouteTable>();
	
	public synchronized Boolean isEmpty() {
		return m.isEmpty();
	}
	
	public synchronized void clear() {
		m.clear();
	}
	
	public synchronized void put(String as_id, RouteTable table) {
		m.put(as_id, table);
	}
	
	public synchronized Set<Entry<String, RouteTable>> getEntrySet() {
		return m.entrySet();
	}
}
