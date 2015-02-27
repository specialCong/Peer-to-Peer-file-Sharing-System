package p2pSystem;

public class HitMessage {
	public MessageID m;
	public boolean flag;
	public Peer target;

	public HitMessage() {

	}

	public HitMessage(MessageID me, boolean fl, Peer ta) {
		m = me;
		flag = fl;
		target = ta;
	}

	public void printHitMessage() {
		System.out.println("Hit message source " + m.peerID.peerName + " ");
		if(flag)
			System.out.println("file found");
		else
			System.out.println("file not found");
		System.out.println("from peer : " + target.peerName);
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Peer p = new Peer("p1", "127.0.0.1", "8101");
		MessageID mid1 = new MessageID(p);
		MessageID mid2 = new MessageID(p);
		//mid2.sequenceNumber=0;
		Message m1 = new Message(mid1, 1, "tianyang");
		Message m2 = new Message(mid2, 1, "tianyang");
		//m1.printMessage();
		//m2.printMessage();
		if( m1.isEqual(m2)) {
			System.out.println("yes");
		}
		
		HitMessage h = new HitMessage(mid1, true, p);
		
		h.printHitMessage();
		
		
		
	}

}
