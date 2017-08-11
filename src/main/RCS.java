package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import rcsserver.AppRecvThread;
import rcsserver.AppSendThread;
import rcsserver.CreateRemoteClient;
import rcsserver.CreateRemoteServer;
import rcsserver.LocalServerRecvThread;
import rcsserver.LocalServerSendThread;
import rcsserver.RemoteRecvThread;
import rcsserver.RemoteSendThread;
import util.FlowQueue;
import util.LinkQueue;
import util.RouteTable;
import util.SendMap;

public class RCS {
	//本地AS号
	private String local_id;
	//本地IP地址
	private String local_ip;
	//本地交换机号
	private String local_swid;
	//本地AS连接的AS个数
	private Integer edge_size;
	//AS号与IP地址的映射
	private Map<String, String> As2Ip = new HashMap<String, String>();
	//Local As作为server
	private Map<String, String> SAs2Ip = new HashMap<String, String>();
	//Local As作为client
	private Map<String, String> CAs2Ip = new HashMap<String, String>();
	//本地路由表
	private RouteTable localTable = new RouteTable();
	//本地次优路由表
	private RouteTable localSubTable = new RouteTable();
	//remote AS号及对应的出端口映射
	private Map<String, String> As2Port = new HashMap<String, String>();
	//RCS与各个域中通信输入接口
	private Map<String, ObjectInputStream> As2In = new HashMap<String, ObjectInputStream>();
	//RCS与各个域中通信输出接口
	private Map<String, ObjectOutputStream> As2Out = new HashMap<String, ObjectOutputStream>();
	//各个端口的发送队列
	private Map<String, SendMap> m = new HashMap<String, SendMap>();
	//向RCSApp发送链路消息的队列
	private LinkQueue lq = new LinkQueue();
	//向本地控制器发送流表下发和删除消息队列
	private FlowQueue fq = new FlowQueue();
	//创建本地socket
	private void createLocalComm() {
		try {
			ServerSocket serverSocket = new ServerSocket(10000);
			Socket socket = serverSocket.accept();
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			
			//接收AS号,IP地址，交换机号
			local_id = (String)ois.readObject();
			local_ip = (String)ois.readObject();
			local_swid = (String)ois.readObject();
			System.out.printf("%s %s %s\n", local_id, local_ip, local_swid);
			
			//接受边和对应的出端口
			edge_size = (Integer)ois.readObject();
			Integer edge_num = edge_size;
			while((edge_num--) != 0) {
				String remoteid = (String)ois.readObject();
				String outport = (String)ois.readObject();
				String cs = (String)ois.readObject();
				String ip = (String)ois.readObject();
				As2Port.put(remoteid, outport);
				As2Ip.put(remoteid, ip);
				SendMap q = new SendMap();
				m.put(outport, q);
				if (cs.equals("c")) {
					CAs2Ip.put(remoteid, ip);
				}
				else {
					SAs2Ip.put(remoteid, ip);
				}
				System.out.printf("%s %s: %s\n", remoteid, outport, ip);
			}
			new Thread(new LocalServerRecvThread(ois, local_id, localTable, localSubTable, m, lq)).start(); //启动本地路由接收线程
			new Thread(new LocalServerSendThread(oos, fq)).start(); //启动本地发送线程，执行流表下发删除操作
			serverSocket.close();
		} catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//创建远程socket
	private void createRemoteComm() {
		Thread a = new Thread(new CreateRemoteClient(CAs2Ip, As2In, As2Out));
		Thread b = new Thread(new CreateRemoteServer(SAs2Ip, As2In, As2Out));
		a.start();
		b.start();
		try {
			a.join();
			b.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//创建与RCSApp交互的socket
	private void createAppComm(String server_ip, Integer server_port) {
		Socket socket = null;
		ObjectInputStream ois = null;
		ObjectOutputStream oos = null;
		//每隔1秒尝试建立与AppServer的连接
		 boolean flag = false;
		 while(!flag) {
			 try {
				flag = true;
				socket = new Socket(InetAddress.getByName(server_ip), server_port);
				ois = new ObjectInputStream(socket.getInputStream());
				oos = new ObjectOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				System.out.println("Trying to connect the RCSApp");
				flag = false;
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
				}
			}
		 }
		 System.out.println("Connected to the RCSApp");
		 //向App发送初始化信息
		 try {
			 //发送AS号,IP地址,交换机号
			 oos.writeObject(local_id);
			 oos.writeObject(local_ip);
			 oos.writeObject(local_swid);
				
			//发送边和对应的出端口号
			//传送边的数量
			oos.writeObject(edge_size);
			for (Map.Entry<String, String> entry: As2Port.entrySet()) {
					oos.writeObject(entry.getKey());
					oos.writeObject(entry.getValue());
			}
			oos.writeObject(localTable);
			oos.writeObject(localSubTable);
			} catch (IOException e) {
			}
		 new Thread(new AppRecvThread(ois, fq)).start(); //启动App接收线程
		 new Thread(new AppSendThread(oos, localTable, localSubTable, lq)).start(); //启动App发送线程
	}
	
	public void init() {
		createLocalComm();
		createRemoteComm();
		createAppComm("192.168.10.17", 9000);
	}
	
	public void work() {
		//为各个remote AS创建接收线程
		for (Map.Entry<String, ObjectInputStream> entry: As2In.entrySet()) {
			new Thread(new RemoteRecvThread(As2Port.get(entry.getKey()), entry.getValue(), m)).start();
		}
		
		//为各个remote AS创建发送线程
		for (Map.Entry<String, ObjectOutputStream> entry: As2Out.entrySet()) {
			String port = As2Port.get(entry.getKey());
			new Thread(new RemoteSendThread(entry.getValue(), m.get(port))).start();
		}
		
	}
	   
	public static void main(String[] args) {
		RCS rcs = new RCS();
		rcs.init();
		rcs.work();
	}
}
