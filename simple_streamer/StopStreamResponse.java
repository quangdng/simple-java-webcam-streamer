package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.JSONObject;

/*
 * This class is responsible to return stop stream response JSON message
 * that is used by a server to response to its clients.
 */

public class StopStreamResponse extends Response {
	private String type = "stoppedstream";

	public StopStreamResponse() {

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
