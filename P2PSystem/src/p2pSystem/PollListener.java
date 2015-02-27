package p2pSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import com.google.gson.Gson;

public class PollListener implements Runnable {

	private Socket socket;
	public DataOutputStream dos;
	public DataInputStream dis;

	public PollListener() {
	}

	@Override
	public void run() {

		try {
			Client.pollServerSocket = new ServerSocket(
					Integer.parseInt(Client.self.peerPort) + 2000);
			while (true) {
				socket = Client.pollServerSocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				dos = new DataOutputStream(socket.getOutputStream());
				Gson gson = new Gson();

				// 1. get sender file information
				FileInfo tempFileInfo = new FileInfo();

				tempFileInfo = gson.fromJson(dis.readUTF(),
						tempFileInfo.getClass());

				Date dt = new Date();
				System.out.println(Client.self.peerName
						+ " gets poll request at " + dt.toString()
						+ " for file " + tempFileInfo.fileName + " from " + dis.readUTF());
				// 2. get corresponding file information
				int index = -1;
				index = Client.searchFileIndex(tempFileInfo.fileName);

				// 3. determine whether out of date
				int isOutOfDate = 1;
				if (Client.ownFileList.get(index).version == tempFileInfo.version)
					isOutOfDate = 0;

				dos.write(isOutOfDate);
				// 4. send back

			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
