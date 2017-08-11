package rcsserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class CreateRemoteServer implements Runnable {
	//local As作为server
	private Map<String, String> SAs2Ip = new HashMap<String, String>();
	//RCS与各个域中通信输入接口
	private Map<String, ObjectInputStream> As2In = new HashMap<String, ObjectInputStream>();
	//RCS与各个域中通信输出接口
	private Map<String, ObjectOutputStream> As2Out = new HashMap<String, ObjectOutputStream>();
	
	public CreateRemoteServer(Map<String, String> SAs2Ip, Map<String, ObjectInputStream> As2In, Map<String, ObjectOutputStream> As2Out) {
		this.SAs2Ip = SAs2Ip;
		this.As2In = As2In;
		this.As2Out = As2Out;
	}
	
	@Override
	public void run() {
		Map<String, String> Ip2SAs = new HashMap<String, String>();
		for (Map.Entry<String, String> entry: SAs2Ip.entrySet()) {
			Ip2SAs.put(entry.getValue(), entry.getKey());
		}
		try {
			ServerSocket serverSocket = new ServerSocket(10001);
			int num = SAs2Ip.size();
			while((num--) != 0) {
				Socket socket = serverSocket.accept();
				System.out.printf("%s connected\n", socket.getInetAddress().getHostAddress());
				ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				As2In.put(Ip2SAs.get(socket.getInetAddress().getHostAddress()), ois);
				As2Out.put(Ip2SAs.get(socket.getInetAddress().getHostAddress()), oos);
			}
			serverSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
