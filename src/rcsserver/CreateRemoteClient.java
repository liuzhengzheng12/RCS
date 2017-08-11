package rcsserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CreateRemoteClient implements Runnable {
	//Local As作为client
	private Map<String, String> CAs2Ip = new HashMap<String, String>();
	//RCS与各个域中通信输入接口
	private Map<String, ObjectInputStream> As2In = new HashMap<String, ObjectInputStream>();
	//RCS与各个域中通信输出接口
	private Map<String, ObjectOutputStream> As2Out = new HashMap<String, ObjectOutputStream>();
	
	public CreateRemoteClient(Map<String, String> CAs2Ip, Map<String, ObjectInputStream> As2In, Map<String, ObjectOutputStream> As2Out) {
		this.CAs2Ip = CAs2Ip;
		this.As2In = As2In;
		this.As2Out = As2Out;
	}
	
	@Override
	public void run() {
		for (Map.Entry<String, String> entry: CAs2Ip.entrySet()) {
			String as_id = entry.getKey();
			String ip = entry.getValue();
			//每隔1秒尝试建立与RemoteIp的连接
			 boolean flag = false;
			 while(!flag) {
				 try {
					flag = true;
					@SuppressWarnings("resource")
					Socket socket = new Socket(InetAddress.getByName(ip), 10001);
					ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					As2In.put(as_id, ois);
					As2Out.put(as_id, oos);
				} catch (IOException e) {
					System.out.printf("Trying to connect %s\n", ip);
					flag = false;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
					}
				}
			 }
			 System.out.printf("Connected to %s\n", ip);
		}
	}

}
