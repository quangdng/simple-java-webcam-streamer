package simple_streamer;

/**
 * @author quangdng
 */

import java.util.Scanner;

/*
 * This class is responsible to wait until user press Enter key
 * to terminate the program if running in remote mode.
 */

public class ProperExitThread extends Thread {

	private RemoteThread remote = null;
	private Scanner in;

	public ProperExitThread(RemoteThread remote) {
		this.remote = remote;
	}

	public void run() {
		while (!remote.doExit) {
			in = new Scanner(System.in);
			System.out.println("Press enter to exit: ");
			if (in.nextLine().equals("")) {
				remote.doExit = true;
			}
		}
	}
}
