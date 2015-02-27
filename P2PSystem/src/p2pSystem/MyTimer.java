package p2pSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.TimerTask;

import com.google.gson.Gson;

public class MyTimer extends TimerTask {
	public FileInfo fileInfo;
	public int i;

	public MyTimer(FileInfo fi) {
		fileInfo = new FileInfo();
		fileInfo = fi;
		i = 0;
	}

	public void run() {

		String ip = fileInfo.originalServer.peerIP;
		String port = fileInfo.originalServer.peerPort;
		try {
			Socket s = new Socket(ip, Integer.parseInt(port) + 2000);
			DataInputStream dis = new DataInputStream(s.getInputStream());
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());

			// 1. send file info
			Gson gson = new Gson();
			String sendBuffer = gson.toJson(fileInfo);
			dos.writeUTF(sendBuffer);
			
			
			// 2. send peer name
			
			dos.writeUTF(Client.self.peerName);
			
			// 3. get the result
			if (dis.read() != 0 && i == 0) {
				System.out.println(fileInfo.fileName + " is out of date.");
				i++;
				int index = Client.searchDownloadFileIndex(fileInfo.fileName);
				Client.downloadFileList.get(index).fileStatus =1;
				// update the file content
				// Client.updateFileContent(fileInfo.fileName,
				// fileInfo.originalServer.peerName);
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}