package rcsserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.floodlightcontroller.egp.routing.RoutingIndex;
import net.floodlightcontroller.egp.routing.RoutingPriorityQueue;
import util.LinkQueue;
import util.RouteTable;
import util.SendMap;

public class LocalServerRecvThread implements Runnable {
	//本地AS号
	private String local_id;
	private ObjectInputStream ois;
	//本地路由表
	private RouteTable localTable = new RouteTable();
	//本地次优路由表
	private RouteTable localSubTable = new RouteTable();
	//各个端口的发送队列
	private Map<String, SendMap> m = new HashMap<String, SendMap>();
	//向RCSApp发送链路消息的队列
	private LinkQueue lq = new LinkQueue();
	
	public LocalServerRecvThread(ObjectInputStream ois, String local_id, RouteTable localTable, RouteTable localSubTable, Map<String, SendMap> m, LinkQueue lq) {
		this.ois = ois;
		this.local_id = local_id;
		this.localTable = localTable;
		this.localSubTable = localSubTable;
		this.m = m;
		this.lq = lq;
	}
	
	private String convertToString(String id, List<String> list) {
		String str = id;
		Integer end = list.size()-1;
		for (int i = 0; i < end; i++) {
			str += (" "+list.get(i));
		}
		
		return str;
	}
	
	//60秒更新一次本地路由信息
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		while(true) {
			try {
				String cmd = (String)(ois.readObject());
				if (cmd.equals("Route")) {
					Map<RoutingIndex, RoutingPriorityQueue> routes = (Map<RoutingIndex, RoutingPriorityQueue>)(ois.readObject());
					localTable.clear();
					localSubTable.clear();
					localTable.setAsid(local_id);
					localSubTable.setAsid(local_id);
					for (Map.Entry<RoutingIndex, RoutingPriorityQueue> entry: routes.entrySet()) {
						RoutingPriorityQueue queue = entry.getValue();
						String dstIp = entry.getKey().getDstIp();
						String now_path = convertToString(local_id, queue.getPoll().getPath());
						String back_path = null;
						if (queue.getTop() != null) {
							back_path = convertToString(local_id, queue.getPoll().getPath());
						}
						localTable.setPath(dstIp, now_path);
						localSubTable.setPath(dstIp, back_path);
						System.out.printf("local_id %s , dstIp %s : %s  %s\n", local_id, dstIp, now_path, back_path);
					}
					synchronized(m) {
						for (SendMap q: m.values()) {
							q.put(local_id, localSubTable.clone());
						}
					}
				} else {
					String localswid = (String)ois.readObject();
					String localport = (String)ois.readObject();
					System.out.printf("%s: %s\n", cmd, localswid+" "+localport);
					synchronized(lq) {
						lq.add(cmd);
						lq.add(localswid);
						lq.add(localport);
					}
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}
}
