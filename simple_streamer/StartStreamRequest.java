package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.JSONObject;

/*
 * This class is responsible to return start stream request JSON message
 * that is used by a client to send request to its server.
 */

public class StartStreamRequest extends Request {
	private String type = "startstream";
	private int ratelimit = 100; // default
	private int sport = 6262;; // default port
	private boolean customRatelimit = false; // whether client has provided a
												// custom ratelimit

	public StartStreamRequest() {
	}

	public StartStreamRequest(int ratelimit, int sport) {
		this.ratelimit = ratelimit;
		this.sport = sport;
		this.customRatelimit = true;
	}

	public StartStreamRequest(int sport) {
		this.sport = sport;
		this.customRatelimit = false;
	}

	public int getRatelimit() {
		return ratelimit;
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

		if (customRatelimit) {
			obj.put("ratelimit", this.ratelimit);
		}

		obj.put("sport", sport);

		return obj.toJSONString();
	}
}
