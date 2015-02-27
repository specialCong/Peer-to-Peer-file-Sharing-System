package p2pSystem;

public class MessageID {
	public Peer peerID;
	public int sequenceNumber;
	public static int globalSequenceNumber = 0;

	public MessageID(Peer p) {
		peerID = new Peer(p);
		sequenceNumber = MessageID.globalSequenceNumber;
		MessageID.globalSequenceNumber++;
	}

	public MessageID(MessageID m) {
		this.peerID.peerIP = m.peerID.peerIP;
		this.peerID.peerName = m.peerID.peerName;
		this.peerID.peerPort = m.peerID.peerPort;
		this.sequenceNumber = m.sequenceNumber;
		MessageID.globalSequenceNumber++;
		
	}
	
	public boolean isEqual(MessageID m) {
		if(this.peerID.peerName.equals(m.peerID.peerName) && this.sequenceNumber==m.sequenceNumber)
			return true;
		else
			return false;
	}
}
