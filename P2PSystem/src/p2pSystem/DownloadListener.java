package p2pSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import com.google.gson.Gson;

public class DownloadListener implements Runnable {

	private Socket socket;
	public DataOutputStream dos;
	public DataInputStream dis;
	public Gson gson;

	public DownloadListener() {
	}

	@Override
	public void run() {
		try {
			String senderName;
			Client.downloadServerSocket = new ServerSocket(
					Integer.parseInt(Client.self.peerPort) + 1000);
			while (true) {

				// 1. get file name
				socket = Client.downloadServerSocket.accept();
				Date dt = new Date();
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				senderName = dis.readUTF();

				System.out.println("Download file is : " + senderName);

				// get file content.

				String filePath = "/Users/yangkklt/cs550demo/"
						+ Client.self.peerName + "/original/" + senderName;
				FileInputStream in = new FileInputStream(filePath);
				File fileTemp = new File("/Users/yangkklt/tempfile");
				FileOutputStream out = new FileOutputStream(fileTemp);
				int c;
				byte buffer[] = new byte[10240];
				// read the file to a buffer
				int textLength = 0;
				while ((c = in.read(buffer)) != -1) {
					for (int i = 0; i < c; i++) {
						out.write(buffer[i]);
						textLength = i;
					}
				}

				String str = new String(buffer, "UTF-8");
				str = str.substring(0, textLength);

				// 2. send the file content
				dos.writeUTF(str);

				// 3. send file info
				int fileIndex = Client.searchFileIndex(senderName);
				FileInfo fi = Client.ownFileList.get(fileIndex);
				gson = new Gson();
				String sendFileInfo = gson.toJson(fi);
				dos.writeUTF(sendFileInfo);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
