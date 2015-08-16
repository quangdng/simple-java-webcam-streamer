package simple_streamer;

/**
 * @author quangdng
 */

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.kohsuke.args4j.*;

/*
 * This is the main class acts as entry point of SimpleStreamer.
 * This class is responsible to handle parameter input, start different
 * threads that are required in SimpleStreamer.
 */

public class EntryPoint {

	/*
	 * Instance variables represent command line arguments
	 */

	// -sport argument
	@Option(name = "-sport", required = false, 
			usage = "Specify server port to set up connection", 
			metaVar = "Y")
	private int sport = 6262;

	// -remote argument
	@Option(name = "-remote", required = false, 
			usage = "Specify the host name of remote server to connect to", 
			metaVar = "hostname")
	protected String remoteHost = null;

	// -rport argument
	@Option(name = "-rport", required = false, 
			usage = "Specify the port of remote server to connect to", 
			metaVar = "X")
	protected int rport;

	// -rate argument
	@Option(name = "-rate", required = false, 
			usage = "Specify the rate limit", metaVar = "Z")
	private int rate = 100;

	// Client number
	protected int clientNum;
	private final int MAX_CLIENT_NUM = 3;

	// ServerThread array for overloaded response
	protected ArrayList<ServerThread> serverArray = new ArrayList<ServerThread>();

	/**
	 * This method provides convenient way to parse command line arguments and
	 * validate them.
	 * 
	 * @param args Command line arguments array
	 * @throws IOException
	 */
	public void parseArgs(String[] args) throws IOException {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			// parse the arguments.
			parser.parseArgument(args);
			if (this.remoteHost != null) {
				if (this.rport == 0) {
					throw new CmdLineException(parser,
							"You have to specify port of remote server.");
				}
			} else if (this.rport > 0) {
				if (this.remoteHost == null) {
					throw new CmdLineException(parser,
							"You have to specify host name of remote server.");
				}
			}

		} catch (CmdLineException e) {
			// Print error message
			System.err.println(e.getMessage() + "\n");
			System.err
					.println("java -jar SimpleStreamer.jar [options...] arguments...");
			// print the list of available options
			parser.printUsage(System.err);
			System.err.println();

			// Print option sample
			System.err
					.println("Example: java -jar SimpleStreamer.jar [-sport X] [-remote hostname [-rport Y]] [-rate Z]");

			System.exit(-1);
		}
	}

	public static void main(String[] args) throws IOException {
		EntryPoint mainThread = new EntryPoint();
		mainThread.parseArgs(args);

		// Check for running remote or locally
		boolean isRemote = false;
		if (mainThread.remoteHost != null) {
			isRemote = true;
		}

		// Start in remote mode and look for free server if the input one is
		// overloaded
		WebcamThread webcam = null;
		RemoteThread remoteThread = null;
		if (isRemote) {

			HandoverThread findServer = new HandoverThread(mainThread);
			findServer.start();

			LinkedHashMap validServer = null;
			
			// Wait until successfully found a ready server
			synchronized (findServer) {
				try {
					findServer.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				validServer = findServer.validServer;
			}
			
			// Start remote thread
			remoteThread = new RemoteThread((String) validServer.get("ip"),
					((Number) validServer.get("port")).intValue(),
					mainThread.rate, mainThread.sport);
			remoteThread.start();
		}

		// Start webcam in local mode
		else {
			webcam = new WebcamThread();
			webcam.start();
		}

		/*
		 * Set up Server
		 */
		Socket socket = null;
		ServerSocket serverSocket = null;
		System.out.println("Server is listening on port: " + mainThread.sport);

		try {
			serverSocket = new ServerSocket(mainThread.sport);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Set initial client number
		mainThread.clientNum = 0;
		
		// Start server
		while (true) {

			try {
				socket = serverSocket.accept();
				ServerThread server = null;

				// Handle overloaded
				if (mainThread.clientNum == mainThread.MAX_CLIENT_NUM) {
					OverloadedResponseThread overload = new OverloadedResponseThread(
							socket, mainThread, isRemote);
					overload.start();

				} else {
					// Choose remoteThread as data source if running remote
					if (isRemote) {
						server = new ServerThread(socket, remoteThread,
								mainThread);
						server.start();
					}
					// Choose webcam thread as datasource if running locally
					else {
						server = new ServerThread(socket, webcam, mainThread);
						server.start();
					}
					
					mainThread.clientNum++;
					mainThread.serverArray.add(server);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
