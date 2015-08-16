package simple_streamer;

/**
 * @author quangdng
 */

import java.util.LinkedHashMap;
import java.util.LinkedList;
import org.json.simple.JSONObject;

/*
 * This class is responsible to prepare Overloaded JSON message
 * that is used by a server to response to its clients.
 */

public class OverloadResponse extends Response {
	private String type = "overloaded";
	private LinkedList clients = new LinkedList();
	private LinkedHashMap server = new LinkedHashMap();

	public OverloadResponse() {

	}

	public OverloadResponse(LinkedList clients) {
		this.clients = clients;
	}

	public OverloadResponse(LinkedList clients, LinkedHashMap server) {
		this.clients = clients;
		this.server = server;
	}

	@Override
	String Type() {
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	String ToJSON() {
		JSONObject obj = new JSONObject();
		obj.put("response", Type());
		obj.put("clients", clients);

		if (!server.isEmpty()) {
			obj.put("server", server);
		}

		return obj.toJSONString();
	}
}
