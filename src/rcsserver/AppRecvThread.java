package rcsserver;

import java.io.IOException;
import java.io.ObjectInputStream;

import util.FlowQueue;

public class AppRecvThread implements Runnable {
	private ObjectInputStream ois;
	//向本地控制器发送流表下发和删除消息队列
	private FlowQueue fq = new FlowQueue();
	
	public AppRecvThread(ObjectInputStream ois, FlowQueue fq) {
		this.ois = ois;
		this.fq = fq;
	}
	@Override
	public void run() {
		while(true) {
			try {
				String cmd = (String)(ois.readObject());
				String swid = (String)(ois.readObject());
				String srcip = (String)(ois.readObject());
				String dstip = (String)(ois.readObject());
				String outport = (String)(ois.readObject());
				synchronized(fq) {
					fq.add(cmd);
					fq.add(swid);
					fq.add(srcip);
					fq.add(dstip);
					fq.add(outport);
				}
				String loginfo = "cmd"+":" +
			 			 "\n---swichId: " + swid + 
			 			 "\n---srcIp: " + srcip +
			 			 "\n---dstIp: " + dstip +
			 			 "\n---outPort:" + outport +"\n";
				System.out.println(loginfo);
			} catch(IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

}
