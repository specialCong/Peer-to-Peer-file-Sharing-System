package p2pSystem;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Date;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

public class Client {

	public static Peer self;
	public static Peer[] neighbors;
	public static int neighborsNo;
	public static String[] fileList;

	public DataOutputStream dos;
	public DataInputStream dis;
	public static ServerSocket serversocket;
	public static ServerSocket downloadServerSocket;
	public static ServerSocket pollServerSocket;
	public static Socket socket[];
	public Gson gson;

	public static Message[] messageArray;
	public static Peer[] upstreamArray;
	public static int messageNumber;
	public static DataInputStream neighborInput[];
	public static DataOutputStream neighborOutput[];

	// used for programming assignment 3
	public static ArrayList<FileInfo> ownFileList;
	public static ArrayList<FileInfo> downloadFileList;
	public static ArrayList<InvalidationMessage> invalMsgList;
	public static ArrayList<Peer> invalMsgUpstreamPeer;
	public static ArrayList<FileInfoMonitor> monitorList;

	public static ArrayList<FileInfoMonitor> tempList;
	public static int originalTTR;

	public final int MAX_TTL = 5;
	public static int consistencyMethod;

	public Client() {
		initializeClient();
	}

	public String getPort() {
		return self.peerPort;
	}

	public String getIP() {
		return self.peerIP;
	}

	public void initializeClient() {

		messageArray = new Message[500];
		upstreamArray = new Peer[500];
		messageNumber = 0;
		gson = new Gson();
		String configurePath = "E:/Eclipse/workspace/P2PSystem/configure";
		String configureConsistency = "E:/Eclipse/workspace/P2PSystem/consistencyMethod";
		readConfigure(configurePath);
		readConsistencyMethod(configureConsistency);
		String filePath = "E:/Eclipse/workspace/P2PSystem/demo" + self.peerName
				+ "/original";
		getFileList(filePath);
		new Thread(new Listener()).start();
		new Thread(new DownloadListener()).start();
		new Thread(new PollListener()).start();
		Client.socket = new Socket[Client.neighborsNo];
		Client.neighborInput = new DataInputStream[Client.neighborsNo];
		Client.neighborOutput = new DataOutputStream[Client.neighborsNo];
		initFileMonitor(filePath);
		monitorList = new ArrayList<FileInfoMonitor>();
		tempList = new ArrayList<FileInfoMonitor>();
	}

	public void initFileMonitor(String filePath) {
		try {
			// filePath = filePath + "/p1.1";
			FileSystemManager fsManager = VFS.getManager();
			FileObject listendir = fsManager.resolveFile(filePath);
			DefaultFileMonitor fm = new DefaultFileMonitor(new FileListener() {
				private synchronized void updateRegister() {
					// System.out.println("changed");
				}

				@Override
				public void fileCreated(FileChangeEvent fce) throws Exception {
					this.updateRegister();
					// System.out.println(fce.getFile() + " created");
				}

				@Override
				public void fileDeleted(FileChangeEvent fce) throws Exception {
					this.updateRegister();
					// System.out.println(fce.getFile() + " deleted");
				}

				@Override
				public void fileChanged(FileChangeEvent fce) throws Exception {
					this.updateRegister();

					String fileName = fce.getFile().getName().toString();
					String file = fileName.substring(fileName.lastIndexOf("/") + 1);
					// in case of temp file's interruption
					if (file.charAt(0) != '.') {
						System.out.println(file + " is modified.");
						int index = searchFileIndex(file);
						FileInfo fi = Client.ownFileList.get(index);
						fi.versionIncrement();
						if (Client.consistencyMethod == 0)
							forwardInvalidationMessage(fi);

					}
				}
			});
			fm.setRecursive(false);
			fm.addFile(listendir);
			fm.start();
		} catch (FileSystemException ex) {
			Logger.getLogger(Client.class.getName())
					.log(Level.SEVERE, null, ex);
		}

	}

	// read configuration to determine use push or pull
	// 0 for push
	// 1 for pull
	public void readConsistencyMethod(String fileName) {
		BufferedReader reader = null;
		try {

			File file = new File(fileName);
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			line = reader.readLine();
			Client.consistencyMethod = Integer.parseInt(line);
			if(Client.consistencyMethod == 0) {
				System.out.println("Consistency method is push. ");			
			} else {
				System.out.println("consistency method is pull.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException el) {

				}
			}
		}

	}

	// forward the invalidation message to all neighbours
	public void forwardInvalidationMessage(FileInfo fi) {
		InvalidationMessage invalMessage = new InvalidationMessage(
				new MessageID(Client.self), fi, MAX_TTL);

		invalMessage.displayInvalidationMessage();
		// send this message
		gson = new Gson();
		try {
			for (int i = 0; i < Client.neighborsNo; i++) {
				Client.neighborOutput[i].writeUTF("11"); // send message Gson
				String sendBuffer = gson.toJson(invalMessage);
				Client.neighborOutput[i].writeUTF(sendBuffer); // send upstream
																// information
				sendBuffer = gson.toJson(Client.self);
				Date dt = new Date();
				System.out.println(Client.self.peerName
						+ " forwards invalidation message of " + fi.fileName
						+ " to " + Client.neighbors[i].peerName + " at "
						+ dt.toString());
				Client.neighborOutput[i].writeUTF(sendBuffer);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public boolean hasConnected() {
		for (int i = 0; i < Client.neighborsNo; i++) {
			if (Client.neighbors[i] == null)
				return false;
		}
		return true;
	}

	public static void readConfigure(String filename) {

		BufferedReader reader = null;
		try {
			neighbors = new Peer[10];
			System.out.println("Please input the name of the peer : ");
			Scanner input = new Scanner(System.in);

			String inputString = input.nextLine();

			File file = new File(filename);
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			String[] str = null;
			while ((line = reader.readLine()) != null) {
				str = line.split("\t");
				if (str[0].equals(inputString)) {

					// set this peer's id
					self = new Peer(str[0], str[1], str[2]);
					Client.originalTTR = Integer.parseInt(str[3]);
					neighborsNo = str.length - 4;

					// get its neighbors' attributes
					reader = new BufferedReader(new FileReader(file));
					int neighbourIndex = 0;
					while ((line = reader.readLine()) != null) {
						String[] neighAttr = line.split("\t");
						for (int i = 4; i < neighborsNo + 4; i++) {
							if (str[i].equals(neighAttr[0])) {
								neighbors[neighbourIndex++] = new Peer(
										neighAttr[0], neighAttr[1],
										neighAttr[2]);
							}
						}
					}
				}
			}
			System.out.println("I have " + Client.neighborsNo + " neighbours");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException el) {

				}
			}
		}
	}

	// read itself's file list.
	public void getFileList(String peerPath) {

		File file = new File(peerPath);
		if (file.exists()) {
			// System.out.println("");
		} else {
			file.mkdirs();
		}

		fileList = new String[file.list().length];
		fileList = file.list();
		ownFileList = new ArrayList<FileInfo>();
		downloadFileList = new ArrayList<FileInfo>();

		invalMsgList = new ArrayList<InvalidationMessage>();
		invalMsgUpstreamPeer = new ArrayList<Peer>();

		for (int i = 0; i < fileList.length; i++) {
			ownFileList
					.add(new FileInfo(fileList[i], self, Client.originalTTR));
		}

	}

	public void connect() {

		try {
			for (int i = 0; i < Client.neighborsNo; i++) {
				if (Client.socket[i] == null) {
					Client.socket[i] = new Socket(Client.neighbors[i].peerIP,
							Integer.parseInt(Client.neighbors[i].peerPort));

					dos = new DataOutputStream(
							Client.socket[i].getOutputStream());
					dos.writeUTF(Client.self.peerName);
				} else {
					// System.out.println(" Notice : "
					// + Client.neighbors[i].peerName + "has occupied.");
				}
			}

			while (this.hasConnected() == false) {
			}

			for (int i = 0; i < Client.neighborsNo; i++) {
				Client.neighborInput[i] = new DataInputStream(
						Client.socket[i].getInputStream());
				Client.neighborOutput[i] = new DataOutputStream(
						Client.socket[i].getOutputStream());
			}

			for (int i = 0; i < Client.neighborsNo; i++) {
				new Thread(new Handler(i)).start();
			}
			// System.out.println("Network established");

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void disconnect(Socket[] socket) {
		for (int i = 0; i < neighborsNo; i++) {
			try {
				socket[i].close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// check if the client already has this message
	// true for already has
	// false for hasn't yet.
	public static boolean checkMessageArray(Message m) {
		for (int i = 0; i < Client.messageNumber; i++) {
			if (Client.messageArray[i].isEqual(m))
				return true;
		}
		return false;
	}

	public void obtain(String fn) {
		System.out
				.println("Please select the peer name you want to download from :");
		Scanner obtainFile = new Scanner(System.in);
		String pn = obtainFile.nextLine();

		String downloadIP = searchDownloadPeerIP(pn);
		String downloadPort = searchDownloadPeerPort(pn);

		// 1. peek the socket belonging to the target peer.

		try {
			// get socket, data input/output stream for this download operation
			Socket s = new Socket(downloadIP,
					Integer.parseInt(downloadPort) + 1000);
			DataInputStream ddis = new DataInputStream(s.getInputStream());
			DataOutputStream ddos = new DataOutputStream(s.getOutputStream());

			// 1. send file name
			ddos.writeUTF(fn);

			// 2. get file content
			String fileContent = ddis.readUTF();

			FileWriter fw = new FileWriter("E:/Eclipse/workspace/P2PSystem/demo"
					+ Client.self.peerName + "/download/" + fn);
			fw.write(fileContent, 0, fileContent.length());
			fw.flush();
			fw.close();

			// 3. get file information
			FileInfo tempFileInfo = new FileInfo();
			tempFileInfo = gson.fromJson(ddis.readUTF(),
					tempFileInfo.getClass());

			// 4. store the file information to the download file list.
			Client.downloadFileList.add(tempFileInfo);

			// 5.. set up timer
			if (Client.consistencyMethod == 1) {
				FileInfoMonitor fm = new FileInfoMonitor(
						Client.downloadFileList.get(Client.downloadFileList
								.size() - 1));
				Client.monitorList.add(fm);
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// update the out-of-date file
	public static void updateFileContent(String fn, String peer) {
		String downloadIP = searchDownloadPeerIP(peer);
		String downloadPort = searchDownloadPeerPort(peer);
		Gson g = new Gson();
		// 1. peek the socket belonging to the target peer.

		try {
			// get socket, data input/output stream for this download operation
			Socket s = new Socket(downloadIP,
					Integer.parseInt(downloadPort) + 1000);
			DataInputStream ddis = new DataInputStream(s.getInputStream());
			DataOutputStream ddos = new DataOutputStream(s.getOutputStream());

			// 1. send file name
			ddos.writeUTF(fn);

			// 2. get file content
			String fileContent = ddis.readUTF();
			FileWriter fw = new FileWriter("E:/Eclipse/workspace/P2PSystem/demo"
					+ Client.self.peerName + "/download/" + fn);
			fw.write(fileContent, 0, fileContent.length());
			fw.flush();
			fw.close();

			// 3. get file information
			FileInfo tempFileInfo = new FileInfo();

			tempFileInfo = g.fromJson(ddis.readUTF(), tempFileInfo.getClass());

			// 4. store the file information to the download file list.
			for (int i = 0; i < Client.downloadFileList.size(); i++) {
				if (Client.downloadFileList.get(i).fileName.equals(fn)) {
					// System.out.println(i);
					// tempFileInfo.displayFileInfo();
					Client.downloadFileList.get(i).version = tempFileInfo.version;
					Client.downloadFileList.get(i).fileStatus = 0;
					// Client.downloadFileList.add(i, tempFileInfo);
				}
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void query(MessageID mid, int TTL, String searchFileName) {
		Message m = new Message(mid, TTL, searchFileName);

		// send this message
		try {
			for (int i = 0; i < Client.neighborsNo; i++) {

				Client.neighborOutput[i].writeUTF("2");
				// send message
				Gson gson = new Gson();
				String sendBuffer = gson.toJson(m);
				Client.neighborOutput[i].writeUTF(sendBuffer);
				// send upstream information
				sendBuffer = gson.toJson(Client.self);
				Client.neighborOutput[i].writeUTF(sendBuffer);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String searchDownloadPeerIP(String peerName) {
		BufferedReader reader = null;
		String s = null;
		try {
			File file = new File(
					"E:/Eclipse/workspace/P2PSystem/configure");
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			String[] str = null;
			while ((line = reader.readLine()) != null) {
				str = line.split("\t");
				if (str[0].equals(peerName)) {
					s = str[1];
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException el) {

				}
			}
		}
		return s;
	}

	public static String searchDownloadPeerPort(String peerName) {
		BufferedReader reader = null;
		String s = null;
		try {
			File file = new File(
					"E:/Eclipse/workspace/P2PSystem/configure");
			reader = new BufferedReader(new FileReader(file));

			String line = null;
			String[] str = null;
			while ((line = reader.readLine()) != null) {
				str = line.split("\t");
				if (str[0].equals(peerName)) {
					s = str[2];
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException el) {

				}
			}
		}
		return s;
	}

	public static int searchFileIndex(String searchFile) {
		int index = -1;
		for (int i = 0; i < Client.fileList.length; i++) {
			if (Client.fileList[i].equals(searchFile)) {
				// System.out.println("find");
				index = i;
			}
		}
		return index;
	}

	public static int searchDownloadFileIndex(String searchFile) {
		int index = -1;
		for (int i = 0; i < Client.downloadFileList.size(); i++) {
			if (Client.downloadFileList.get(i).fileName.equals(searchFile)) {
				// System.out.println("find");
				index = i;
			}
		}
		return index;
	}

	public static void main(String[] args) throws IOException {
		Client client = new Client();

		Scanner input = null;
		int commandIndex;

		do {
			// user interface
			System.out.println("Please input operation index:");
			System.out
					.println("1: Create \t 2: Query \t 3: Obtain \t 4: Refresh \t 5: Quit");
			input = new Scanner(System.in);
			commandIndex = input.nextInt();
			switch (commandIndex) {
			case 1:
				client.connect();
				break;
			case 2:

				Scanner searchResult = new Scanner(System.in);
				System.out
						.println("Please input the file name you are looking for: ");
				String searchFileName = searchResult.nextLine();

				System.out.println("Please input the TTL: ");
				int ttl = searchResult.nextInt();

				// long startTime = System.currentTimeMillis();
				MessageID mid = new MessageID(Client.self);
				client.query(mid, ttl, searchFileName);
				break;
			case 3:
				Scanner obtainFile = new Scanner(System.in);
				System.out
						.println("Please input the file name you want to download :");
				String fn = obtainFile.nextLine();
				client.obtain(fn);
				System.out.println("Download file " + fn + " successfully");
				break;
			case 4:
				for (int i = 0; i < Client.downloadFileList.size(); i++) {
					if (Client.downloadFileList.get(i).fileStatus != 0) {
						System.out
								.println(Client.downloadFileList.get(i).fileName);
						Client.updateFileContent(
								Client.downloadFileList.get(i).fileName,
								Client.downloadFileList.get(i).originalServer.peerName);
						System.out.println("Downloaded file "
								+ Client.downloadFileList.get(i).fileName
								+ " is updated.");
						if (Client.consistencyMethod == 1) {
							FileInfoMonitor fm = new FileInfoMonitor(
									Client.downloadFileList.get(i));
							Client.tempList.add(fm);
						}
					}
				}
			}
		} while (commandIndex != 5);
		disconnect(socket);
	}
}
