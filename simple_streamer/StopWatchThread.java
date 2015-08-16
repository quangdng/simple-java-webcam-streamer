package simple_streamer;

/**
 * @author quangdng
 */

import java.io.IOException;

import org.json.simple.JSONObject;

/*
 * This class is responsible to watch for stop request from clients while server
 * is streaming image data continuously to those clients. It will wake server
 * up when a stop stream request is received.
 */

public class StopWatchThread extends Thread {

	private ServerThread serverThread = null;

	/**
	 * Constructor
	 */
	public StopWatchThread(ServerThread serverThread) {
		this.serverThread = serverThread;
	}

	public void run() {
		// JSON parser
		FromJSON parser = new FromJSON();
		String clientRequest = null;
		JSONObject request = null;

		// Look for stopstream request and wake ServerThread up
		try {
			boolean keepChecking = true;
			while (keepChecking) {
				if (serverThread.reader.ready()) {
					clientRequest = serverThread.reader.readLine();
					request = parser.parse(clientRequest);
					if (request.get("request").equals("stopstream")) {
						// Wake server up
						serverThread.interrupt();
						keepChecking = false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
