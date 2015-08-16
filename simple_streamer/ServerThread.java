package simple_streamer;

/**
 * @author quangdng
 */

import java.io.*;
import java.net.*;

import org.json.simple.JSONObject;

/*
 * This class is responsible to handle request from clients and response
 * to those requests accordingly.
 */

public class ServerThread extends Thread {

	// Socket, reader & writer stuffs
	public BufferedReader reader = null;
	private PrintWriter writer = null;
	Socket socket = null;

	// Datasource for local streaming
	private WebcamThread webcam = null;

	// Datasource for remote streaming
	private RemoteThread remote = null;

	// Main thread for manipulating client number etc
	protected EntryPoint mainThread = null;

	// Control flags
	private boolean isRemote; // Remote flag
	private boolean stopStream = false; // Stop stream flag

	// Connection flag
	private boolean justConnected = true;
	private boolean streaming = false;

	// JSON parser
	FromJSON parser = new FromJSON();

	// Rate limiting
	private int rateLimit = 100;

	// Client information
	protected String clientHost = null;
	protected int clientPort;

	/*
	 * Constructor for local streaming
	 */
	public ServerThread(Socket s, WebcamThread w, EntryPoint mainThread) {
		this.socket = s;
		this.webcam = w;
		this.mainThread = mainThread;
		this.isRemote = false;
	}

	/*
	 * Constructor for remote streaming
	 */
	public ServerThread(Socket s, RemoteThread remote, EntryPoint mainThread) {
		this.socket = s;
		this.remote = remote;
		this.mainThread = mainThread;
		this.isRemote = true;
	}

	public void run() {

		// Store client request
		String clientRequest = null;
		JSONObject request = null;

		/*
		 * Setup buffered reader and writer
		 */
		try {
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			/*
			 * Send JSON features request
			 */
			while (!stopStream) {

				// Send features response at the start of connection
				if (justConnected) {
					StatusResponse status;
					if (isRemote) {
						status = new StatusResponse("remote",
								mainThread.clientNum, true, true);
					} else {
						status = new StatusResponse("local",
								mainThread.clientNum, true, true);
					}
					writer.println(status.ToJSON());
					justConnected = false;
				} else {
					// Read client request if not streaming image
					if (!streaming) {
						clientRequest = reader.readLine();
						request = parser.parse(clientRequest);

						// Handle start stream request & set rate limit
						if (request.get("request").equals("startstream")) {
							if (request.containsKey("ratelimit")) {
								Number customRateLimit = (Number) request
										.get("ratelimit");
								if (customRateLimit.intValue() > 100)
									rateLimit = customRateLimit.intValue();
							}

							// Save client ip & its server port
							clientHost = socket.getRemoteSocketAddress()
									.toString().split(":")[0].replace("/", "");
							clientPort = ((Number) request.get("sport"))
									.intValue();

							StartStreamResponse startStreamRes = new StartStreamResponse();
							writer.println(startStreamRes.ToJSON());
						}

						// Start stream
						streaming = true;

						// Look for client stopstream request to close properly
						StopWatchThread stopWatch = new StopWatchThread(this);
						stopWatch.start();

					} else {
						while (streaming) {
							// Stream image
							if (!isRemote) {
								ImageResponse imgRes = new ImageResponse(
										webcam.imgData);
								writer.println(imgRes.ToJSON());

							} else {
								ImageResponse imgRes = new ImageResponse(
										remote.imgData);
								writer.println(imgRes.ToJSON());
							}

							// Check for sudden close from client
							if (writer.checkError()) {
								stopStream = true;
								break;
							}
							// Sleeping time
							sleep(rateLimit);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();

			// Decrease the number of client and remove itself from main thread
			// server array.
			mainThread.clientNum--;
			mainThread.serverArray.remove(this);

		} catch (InterruptedException e) {
			
			// Send stopped stream response
			StopStreamResponse stopRes = new StopStreamResponse();
			writer.println(stopRes.ToJSON());
			stopStream = true;
			streaming = false;
			
		} finally {
			try {

				// Close connection
				writer.close();
				reader.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Decrease the number of client and remove itself from main thread
		// server array.
		mainThread.clientNum--;
		mainThread.serverArray.remove(this);
	}
}
