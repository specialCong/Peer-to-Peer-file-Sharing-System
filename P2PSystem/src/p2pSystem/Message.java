package p2pSystem;

public class Message {
	public MessageID messageID;
	public int currentTTL;
	public int maxTTL;
	public String FileName;

	public Message(MessageID m, int t, String fn) {
		messageID = m;
		currentTTL = t;
		maxTTL = t;
		FileName = fn;
	}

	public void printMessage() {
		System.out.println("message information: ");
		System.out.println("MessageID : " + messageID.peerID.peerName + "-"
				+ messageID.peerID.peerIP + "-" + messageID.peerID.peerPort
				+ "-" + messageID.sequenceNumber);
		System.out.println("TTL : " + currentTTL + maxTTL);
		System.out.println("Search File : " + FileName);
	}

	public Message() {
		// TODO Auto-generated constructor stub
	}

	public void TTLdecrease() {
		currentTTL += -1;
	}

	public boolean isEqual(Message m) {
		String name1 = this.messageID.peerID.peerName;
		int sequence1 = this.messageID.sequenceNumber;

		String name2 = m.messageID.peerID.peerName;
		int sequence2 = m.messageID.sequenceNumber;

		if (name1.equals(name2) && sequence1 == sequence2)
			return true;
		else
			return false;

	}

	public static void main(String[] args) {
		Peer p = new Peer("p1", "127.0.0.1", "8101");
		MessageID mid1 = new MessageID(p);
		MessageID mid2 = new MessageID(p);
		// mid2.sequenceNumber=0;
		Message m1 = new Message(mid1, 1, "tianyang");
		Message m2 = new Message(mid2, 1, "tianyang");
		m1.printMessage();
		m2.printMessage();
		if (m1.isEqual(m2)) {
			System.out.println("yes");
		}

	}

}
