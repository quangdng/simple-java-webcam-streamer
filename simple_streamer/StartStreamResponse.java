package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.JSONObject;

/*
 * This class is responsible to return start stream response JSON message
 * that is used by a server to response to its clients.
 */

public class StartStreamResponse extends Response {
	private String type = "startingstream";

	public StartStreamResponse() {

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
		return obj.toJSONString();
	}
}
