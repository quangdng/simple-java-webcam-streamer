package simple_streamer;

/**
 * @author quangdng
 */

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import org.json.simple.JSONObject;

/*
 * This class is responsible to return Overloaded JSON message that
 * is used by a server to let new clients know that it is overloaded
 * and return its current serving clients and its server (if running
 * in remote mode) to those new clients for handover purpose.
 */

public class OverloadedResponseThread extends Thread {

	// Socket, reader & writer stuffs
	private PrintWriter writer = null;
	public BufferedReader reader = null;
	Socket socket = null;

	// Main thread
	private EntryPoint mainThread = null;

	// Connection flag
	private boolean justConnected = true;
	private boolean isRemote = false;
	
	
	/*
	 * Constructor
	 */
	public OverloadedResponseThread(Socket s, EntryPoint mainThread,
			boolean isRemote) {
		this.socket = s;
		this.mainThread = mainThread;
		this.isRemote = isRemote;
	}

	/*
	 * Main logic to handle new client connection and return overloaded
	 * response.
	 */
	public void run() {

		String clientRequest = null;
		JSONObject request = null;
		FromJSON parser = new FromJSON();

		/*
		 * Setup buffered reader and writer
		 */
		try {
			writer = new PrintWriter(socket.getOutputStream(), true);
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Loop control flag
		boolean completed = false;

		while (!completed) {
			
			// Send status JSON message
			if (justConnected) {
				StatusResponse status;
				if (isRemote) {
					status = new StatusResponse("remote", mainThread.clientNum,
							true, true);
				} else {
					status = new StatusResponse("local", mainThread.clientNum,
							true, true);
				}
				writer.println(status.ToJSON());
				justConnected = false;
			} else {
				try {
					clientRequest = reader.readLine();
					System.out.println("Client request: " + clientRequest);
					request = parser.parse(clientRequest);

					// Handle start stream request & overloaded response
					if (request.get("request").equals("startstream")) {

						LinkedList clientList = new LinkedList();
						LinkedHashMap serverList = new LinkedHashMap();
						
						// Add current serving client
						for (ServerThread client : mainThread.serverArray) {
							LinkedHashMap m = new LinkedHashMap();
							m.put("ip", client.clientHost);
							m.put("port", client.clientPort);
							clientList.add(m);
						}
						
						// Add server if running remotely
						if (isRemote) {
							serverList.put("ip", mainThread.remoteHost);
							serverList.put("port", mainThread.rport);
						}

						// Send overloaded response 
						OverloadResponse overloadRes = null;
						if (isRemote) {
							overloadRes = new OverloadResponse(clientList,
									serverList);
						} else {
							overloadRes = new OverloadResponse(clientList);
						}
						writer.println(overloadRes.ToJSON());
						completed = true;
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}

		try {
			
			// Close connection
			socket.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
