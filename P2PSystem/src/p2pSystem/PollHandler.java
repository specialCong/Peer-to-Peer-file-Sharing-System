package p2pSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.google.gson.Gson;

public class PollHandler implements Runnable {

	private Socket socket;
	public Gson gson;
	public DataInputStream dis;
	public DataOutputStream dos;

	public PollHandler(Socket socket) {
		gson = new Gson();
		this.socket = socket;
	}

	@Override
	public void run() {
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			int commandIndex;
			int index;
			FileInfo fi = new FileInfo();
			do {
				commandIndex = Integer.parseInt(dis.readUTF());
				switch (commandIndex) {
				case 13:
					fi = gson.fromJson(dis.readUTF(), fi.getClass());
					index = Client.searchFileIndex(fi.fileName);
					FileInfo targetFile = Client.ownFileList.get(index);

					if (targetFile.version != fi.version) {
						fi.fileStatus = 1;
						String sendBuffer = gson.toJson(fi);
						dos.writeUTF("14");
						dos.writeUTF(sendBuffer);
					} else {
						fi.fileStatus = 0;
						fi.TTR = 5;
						String sendBuffer = gson.toJson(fi);
						dos.writeUTF("15");
						dos.writeUTF(sendBuffer);
					}
					// socket.close();
					break;
				case 14:
					fi = gson.fromJson(dis.readUTF(), fi.getClass());
					index = Client.searchDownloadFileIndex(fi.fileName);
					FileInfo tFile = Client.downloadFileList.get(index);
					tFile.fileStatus = fi.fileStatus;
					System.out.println("File " + tFile.fileName
							+ " is out of date");
					// socket.close();
					break;
				case 15:
					fi = gson.fromJson(dis.readUTF(), fi.getClass());
					index = Client.searchDownloadFileIndex(fi.fileName);
					FileInfo tf = Client.downloadFileList.get(index);
					tf.fileStatus = fi.fileStatus;
					tf.fileStatus = fi.TTR;
					System.out.println("File " + tf.fileName
							+ " is the newest version");
					// socket.close();
					break;
				}
			} while (commandIndex != 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
