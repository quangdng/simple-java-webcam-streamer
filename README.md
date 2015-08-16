## Project Idea
The project aims to build a  complex distributed system - a simple streaming application that can stream webcam images;
called SimpleStreamer.

I make use of https://github.com/sarxos/webcam-capture library for capturing images from the webcam.

In this project, I also provide a ready to use .jar file which includes all required packages.

## Architecture
The SimpleStreamer application will include both server and client behaviour. There are two modes that SimpleStreamer 
can run in: Local and Remote. 

In Local mode, SimpleStreamer will read images from the default webcam and render those images. In Remote mode, 
SimpleStreamer will connect to another SimpleStreamer application (that is already running somewhere) and receive 
images from that application. In this way SimpleStreamer acts like a client and server. 

Every SimpleStreamer application is acting like a server regardless of which mode it starts in. Any other 
SimpleStreamer application can connect as a client. The images given from the server to the client are either those 
that are obtained from the local webcam (when the server is running in Local mode) or those obtained from some other 
server (when the server is running in Remote mode).

## Protocol
We will use TCP for simplicity. We will use port 6262 as the default port, in the absence of port numbers being given 
on the command line.

The protocol between client and server will be quite simple. When the client connects, the server will immediately 
send a JSON message detailing the features implemented, and then the client will send at least one and possibly more 
Requests to the server, using JSON formatted messages. Each JSON message will be on a single line, i.e. delimited by 
a new line character.

The server will respond to the client depending on the Requests received.

The server theoretically will respondto any number of clients concurrently; though in practice of course there will be
a limit to how many clients can be serviced. In this project there will be a hard limit of 3 clients, but it will be 
mitigated by handover functionality.

## How To Use
Let's all use Java 1.7.

The command line execution is:

java -jar SimpleStreamer.jar [-sport X] [-remote hostname [-rport Y]] [-rate Z]

Where sport is the server port to use, defaulting to 6262 if no sport is given, remote specifies a hostname to connect
to if the local webcam is not to be used but rather a server is to be connected to, rport specifies a port number for 
the remote server, and rate specifies a sleep time that is desired. For example:

java -jar SimpleStreamer.jar

will use the local webcam and provide a server on the default port.

java -jar SimpleStreamer.jar -remote sunrise.cis.unimelb.edu.au -rport 2323

will try obtaining the stream from the remote host on port 2323, but locally it will be serving on the default port.

java -jar SimpleStreamer.jar -rate 200

is odd, because the local webcam is being used and so the rate option has no purpose. 
