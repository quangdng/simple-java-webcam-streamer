package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.JSONObject;

/*
 * This class is responsible to return image data JSON message
 * that is used by a server to stream to its clients.
 */

public class ImageResponse extends Response {
	private String type = "image";
	private String data = "";
	
	/*
	 * Constructor
	 */
	public ImageResponse(String data) {
		this.data = data;
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
		obj.put("data", data);

		return obj.toJSONString();
	}
}
