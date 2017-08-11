package rcsserver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

import util.RouteTable;
import util.SendMap;

public class RemoteSendThread implements Runnable {
	private ObjectOutputStream oos;
	//发送队列
	private SendMap q = new SendMap();
	
	public RemoteSendThread(ObjectOutputStream oos, SendMap q) {
		this.oos = oos;
		this.q = q;
	}
	
	@Override
	public void run() {
		while(true) {
			try {
				if (!q.isEmpty()) {
					synchronized(q) {
						for (Map.Entry<String, RouteTable> entry: q.getEntrySet()) {
							oos.writeObject(entry.getValue());
						}
					}
					q.clear();
				}
				else {
					Thread.sleep(1000);
				} 
			} catch(IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
