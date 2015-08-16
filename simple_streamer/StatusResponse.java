package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.JSONObject;

/*
 * This class is responsible to return status response JSON message
 * that is used by a server to response to its clients.
 */

public class StatusResponse extends Response {
	private String type = "status";
	private String mode = null;
	private int clientNum = 0;
	private String ratelimiting = null;
	private String handover = null;

	public StatusResponse(String mode, int clientNum, boolean ratelimiting,
			boolean handover) {
		this.mode = mode;
		this.clientNum = clientNum;

		if (ratelimiting) {
			this.ratelimiting = "yes";
		} else {
			this.ratelimiting = "no";
		}

		if (handover) {
			this.handover = "yes";
		} else {
			this.handover = "no";
		}
	}

	public String getMode() {
		return mode;
	}

	public int getClientNum() {
		return clientNum;
	}

	public String rateLimit() {
		return ratelimiting;
	}

	public String handover() {
		return handover;
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
		obj.put("streaming", mode);
		obj.put("clients", clientNum);
		obj.put("ratelimiting", ratelimiting);
		obj.put("handover", handover);

		return obj.toJSONString();
	}
}
