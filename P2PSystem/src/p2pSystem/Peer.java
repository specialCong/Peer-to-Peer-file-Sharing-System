package p2pSystem;

public class Peer {

	public String peerName;
	public String peerIP;
	public String peerPort;

	public Peer() {
		
	}
	
	
	public Peer(String peername, String peerip, String peerport) {
		peerName = peername;
		peerIP = peerip;
		peerPort = peerport;
	}

	public Peer(Peer p) {
		this.peerName = p.peerName;
		this.peerIP = p.peerIP;
		this.peerPort = p.peerPort;
	}
	
	
	public void printPeer() {
		System.out.println("Peer information: " + peerName + " " + peerIP + " " + peerPort);
	}
}
