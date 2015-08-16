package simple_streamer;

/**
 * @author quangdng
 */

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.json.simple.JSONObject;

/*
 * This class is responsible to find a ready server if SimpleStreamer is running
 * in remote mode. It also implements breadth first search algorithm to search for
 * ready servers if the desired one is overloaded.
 */

public class HandoverThread extends Thread {
	
	// Desired server info
	private LinkedHashMap server = new LinkedHashMap();
	
	// Information queue used by breadth first search
	protected Queue<LinkedHashMap> checkQueue = new LinkedList<LinkedHashMap>();
	
	// Link to main thread to get important information
	private EntryPoint mainThread = null;

	// Valid server to be returned to main thread.
	protected LinkedHashMap validServer = null;

	
	/**
	 * Constructor
	 */
	public HandoverThread(EntryPoint mainThread) {
		this.mainThread = mainThread;
		server.put("ip", mainThread.remoteHost);
		server.put("port", mainThread.rport);

		checkQueue.add(server);
		// validServer = server;
	}

	public void run() {
		synchronized (this) {

			// JSON parser
			FromJSON parser = new FromJSON();

			// Initiate connection variables
			InetAddress address = null;
			Socket socket = null;
			BufferedReader reader = null;
			PrintWriter writer = null;
			
			// Control flag
			boolean success = false;
			
			// Breadth first search
			while (!success && !checkQueue.isEmpty()) {

				// Get and remove first node from queue
				LinkedHashMap currentServer = checkQueue.poll();

				// Set up connection
				try {
					address = InetAddress.getByName((String) currentServer
							.get("ip"));
					socket = new Socket(address,
							((Number) currentServer.get("port")).intValue());
					reader = new BufferedReader(new InputStreamReader(
							socket.getInputStream()));
					writer = new PrintWriter(socket.getOutputStream(), true);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();

				}
				
				// Test connection to current node
				while (true) {
					try {
						String serverResponse = reader.readLine();
						JSONObject response = parser.parse(serverResponse);
						if (!response.get("response").equals("image"))
							// System.out.println(serverResponse);

							// Handle status response
							if (response.get("response").equals("status")) {
								// Prepare startstream request for rate limiting
								// or
								// not
								StartStreamRequest startStream = null;
								if (response.get("ratelimiting").equals("yes")) {
									startStream = new StartStreamRequest(100,
											6262);
								}
								writer.println(startStream.ToJSON());
							}

						// Check for overloaded
						if (response.get("response").equals("overloaded")) {
							// Add the client server provided to checkQueue then
							// quit the loop

							// Add server first if available for better
							// performance
							if (response.containsKey("server")) {
								Map sMap = (Map) response.get("server");
								LinkedHashMap server = new LinkedHashMap(sMap);
								checkQueue.add(server);
							}

							// Add client later
							for (Object e : (List) response.get("clients")) {
								Map eMap = (Map) e;
								LinkedHashMap client = new LinkedHashMap(eMap);
								checkQueue.add(client);
							}
							break;
						}

						// Return valid server
						if (response.get("response").equals("startingstream")) {
							this.validServer = currentServer;
							// System.out.println(currentServer);
						}

						// Safely send stop request to close test connection
						if (response.get("response").equals("image")) {
							StopStreamRequest stopReq = new StopStreamRequest();
							writer.println(stopReq.ToJSON());
						}

						// Handle StopStream Response
						if (response.get("response").equals("stoppedstream")) {
							success = true;
							break;
						}

					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				try {
					// Close socket
					socket.close();
					writer.close();
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			// Notify main thread that a valid server is found
			notify();
		}
	}
}
