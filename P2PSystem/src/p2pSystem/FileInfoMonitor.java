package p2pSystem;

import java.util.Timer;

public class FileInfoMonitor {

	public Timer timer = new Timer();
	public static FileInfo fileInfo;

	public FileInfoMonitor() {
	}

	public FileInfoMonitor(FileInfo fi) {
		FileInfo fim = new FileInfo();
		fim = fi;

		FileInfoMonitor.fileInfo = fim;
		this.timer = new Timer();
		timer.schedule(new MyTimer(fim), 5000, fi.TTR * 1000);
	}

	public void displayFileInfoMonitor() {
		// System.out.println("file monitor is : " + FileInfoMonitor.);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
