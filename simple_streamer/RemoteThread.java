package simple_streamer;

/**
 * @author quangdng
 */

import java.io.*;
import java.net.*;

import javax.swing.JFrame;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;

/*
 * This class is responsible to connect to a streaming server, retrieve
 * image data from that server and output those images to a GUI window.
 */

public class RemoteThread extends Thread {
	private String remoteHost = "localhost"; // Default host
	private int rport = 6262; // Default port
	private int rateLimit = 100; // Default rate limit

	// Connection stuffs
	private Socket socket = null;
	private BufferedReader reader = null;
	private PrintWriter writer = null;
	InetAddress address = null;

	// Port info
	private int sport; // Server port of a client (because a client also acts as
						// a server)

	// Stop flag
	private boolean stopStream = false;
	protected boolean doExit = false;

	// JSON parser
	FromJSON parser = new FromJSON();

	// Datasource for server
	public String imgData = null;

	/**
	 * Constructor
	 */
	public RemoteThread(String remoteHost, int rport, int rateLimit, int sport) {
		this.remoteHost = remoteHost;
		this.rport = rport;
		if (rateLimit != 0) {
			this.rateLimit = rateLimit;
		}
		this.sport = sport;
	}

	public void run() {
		// Setup GUI Component
		Viewer myViewer = null;
		JFrame frame = null;

		// Server & Client communication
		String serverResponse = null;
		JSONObject response = null;

		/*
		 * Initiate connection
		 */
		try {
			// Set up connection
			address = InetAddress.getByName(remoteHost);
			socket = new Socket(address, rport);
			reader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			writer = new PrintWriter(socket.getOutputStream(), true);

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Watch for enter to exit
		ProperExitThread exit = new ProperExitThread(this);
		exit.start();

		try {
			while (!stopStream) {
				serverResponse = reader.readLine();
				response = parser.parse(serverResponse);
				if (!response.get("response").equals("image"))

					// Handle status response
					if (response.get("response").equals("status")) {

						// Prepare startstream request for rate limiting or not
						StartStreamRequest startStream;
						if (response.get("ratelimiting").equals("yes")) {
							startStream = new StartStreamRequest(rateLimit,
									sport);
						} else {
							startStream = new StartStreamRequest(sport);
						}
						writer.println(startStream.ToJSON());
					}

				// Handle overloaded response
				if (response.get("response").equals("overloaded")) {
					stopStream = true;
				}

				// Handle starting stream response
				if (response.get("response").equals("startingstream")) {
					myViewer = new Viewer();
					frame = new JFrame("Remote Stream");
					frame.setVisible(true);
					frame.setSize(320, 240);
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.add(myViewer);
				}

				if (doExit) {
					// Stop request
					StopStreamRequest stopReq = new StopStreamRequest();
					writer.println(stopReq.ToJSON());
				}

				// Handle StopStream Response
				if (response.get("response").equals("stoppedstream")) {
					stopStream = true;
				}

				// Handle image
				if (response.get("response").equals("image")) {
					imgData = (String) response.get("data");

					// Decode image and update view
					byte[] nobase64_image = Base64.decodeBase64(imgData);
					byte[] decompressed_image = Compressor
							.decompress(nobase64_image);
					myViewer.ViewerInput(decompressed_image);
					frame.repaint();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {

				// Close the connection
				writer.close();
				reader.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(-1);
		}
	}

}
