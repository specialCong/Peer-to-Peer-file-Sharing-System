package p2pSystem;

public class InvalidationMessage {
	public MessageID msgID;
	public FileInfo fileInformation;
	public int TTL;
	
	
	
	
	public InvalidationMessage() {
	}

	public InvalidationMessage(MessageID msg, FileInfo file, int t) {
		this.msgID = msg;
		this.fileInformation = new FileInfo(file);
		this.TTL = t;
	}

	public void displayInvalidationMessage() {
		System.out.println("Invalidation Message Information:");
		System.out.println("Original Server: " + msgID.peerID.peerName
				+ " Message ID: " + msgID.sequenceNumber + " File Name: "
				+ fileInformation.fileName + " Version Number: "
				+ fileInformation.version + " TTL: " + TTL);
	}
	
	public void ttlDecrement() {
		TTL--;
	}

	public boolean continueForward() {
		if(TTL == 0)
			return false;
		else
			return true;
	}
	
}
