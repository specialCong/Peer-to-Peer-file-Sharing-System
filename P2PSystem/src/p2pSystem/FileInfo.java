package p2pSystem;

public class FileInfo {
	public String fileName;
	public Peer originalServer;
	public int version;
	public int fileStatus;
	public int TTR;

	public FileInfo() {

	}

	public FileInfo(String s, Peer p, int t) {
		fileName = s;
		originalServer = new Peer();
		originalServer = p;
		version = 0;
		fileStatus = 0;
		TTR = t;
	}

	public FileInfo(FileInfo f) {
		this.fileName = f.fileName;
		this.originalServer = f.originalServer;
		this.version = f.version;
		this.fileStatus = f.fileStatus;
		this.TTR = f.TTR;
	}

	public void displayFileInfo() {
		System.out.println("File information: " + fileName + " - "
				+ originalServer.peerName + " - " + version + " - "
				+ fileStatus + " TTR " + TTR);
	}

	public void versionIncrement() {
		version++;
	}
}
