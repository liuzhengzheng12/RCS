package rcsserver;

import java.io.IOException;
import java.io.ObjectOutputStream;

import util.FlowQueue;

public class LocalServerSendThread implements Runnable {
	private ObjectOutputStream oos;
	//向本地控制器发送流表下发和删除消息队列
	private FlowQueue fq = new FlowQueue();
	
	public LocalServerSendThread(ObjectOutputStream oos, FlowQueue fq) {
		this.oos = oos;
		this.fq = fq;
	}
	
	//向本地控制器发送流表的下发和删除信息
	@Override
	public void run() {
		while(true) {
			try {
				if (!fq.isEmpty()) {
					synchronized(fq) {
						oos.writeObject(fq.poll());
						oos.writeObject(fq.poll());
						oos.writeObject(fq.poll());
						oos.writeObject(fq.poll());
						oos.writeObject(fq.poll());
					}
				} else {
					Thread.sleep(10000);
				}
			} catch(IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
