package rcsserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

import util.RouteTable;
import util.SendMap;

public class RemoteRecvThread implements Runnable {
	//出端口号
	private String localport;
	private ObjectInputStream ois;
	//各个端口的发送队列
	private Map<String, SendMap> m = new HashMap<String, SendMap>();
	
	public RemoteRecvThread(String localport, ObjectInputStream ois, Map<String, SendMap> m) {
		this.localport = localport;
		this.ois = ois;
		this.m = m;
	}
	
	//对接收到的RouteTable进行洪泛
	@Override
	public void run() {
		while(true) {
			try {
				RouteTable Subtable = (RouteTable)ois.readObject();
				System.out.printf("from %s receive:\n", localport);
				Subtable.printTable();
				System.out.println();
				synchronized(m) {
					for (String outport: m.keySet()) {
						if (!outport.equals(localport)) {
							m.get(outport).put(Subtable.getAsid(), Subtable.clone());
						}
					}
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}
	}

}
