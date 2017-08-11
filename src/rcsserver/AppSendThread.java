package rcsserver;

import java.io.IOException;
import java.io.ObjectOutputStream;

import util.LinkQueue;
import util.RouteTable;

public class AppSendThread implements Runnable {
	private ObjectOutputStream oos;
	//本地路由表
	private RouteTable localTable = new RouteTable();
	//本地次优路由表
	private RouteTable localSubTable = new RouteTable();
	//向RCSApp发送链路消息的队列
	private LinkQueue lq = new LinkQueue();
	
	public AppSendThread(ObjectOutputStream oos, RouteTable localTable, RouteTable localSubTable, LinkQueue lq) {
		this.oos = oos;
		this.localTable = localTable;
		this.localSubTable = localSubTable;
		this.lq = lq;
	}
	
	//不断发送lq队列中的链路状态信息
	@Override
	public void run() {
		while(true) {
			try {
				if (!lq.isEmpty()) {
					synchronized(lq) {
						oos.writeObject(lq.poll());
						oos.writeObject(lq.poll());
						oos.writeObject(lq.poll());
					}
				} else {
					Thread.sleep(1000);
				}
			} catch(IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
