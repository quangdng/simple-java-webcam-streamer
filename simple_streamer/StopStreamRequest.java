package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.JSONObject;

/*
 * This class is responsible to return stop stream request JSON message
 * that is used by a client to send request to its server.
 */

public class StopStreamRequest extends Request {
	private String type = "stopstream";

	public StopStreamRequest() {

	}

	@Override
	String Type() {
		return type;
	}

	@SuppressWarnings("unchecked")
	@Override
	String ToJSON() {
		JSONObject obj = new JSONObject();
		obj.put("request", Type());
		return obj.toJSONString();
	}
}
