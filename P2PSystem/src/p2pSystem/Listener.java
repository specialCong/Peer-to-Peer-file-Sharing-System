package p2pSystem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class Listener implements Runnable{

	private Socket socket;
	public DataOutputStream dos;
	public DataInputStream dis;
	public Listener(){
    }
	
	@Override
	public void run() {
		try {
			String senderName;
			Client.serversocket = new ServerSocket(Integer.parseInt(Client.self.peerPort));
			while(true){
				socket = Client.serversocket.accept();
				dis = new DataInputStream(socket.getInputStream());
				senderName = dis.readUTF();
				//System.out.println("I receive from " + senderName);
				
				// Then, find the right slot in the static socket array.
				for(int i = 0 ; i < Client.neighborsNo ; i ++ ) {
					if (Client.neighbors[i].peerName.equals(senderName)) {
						Client.socket[i] = socket;
					}
				}
				
				
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
}
