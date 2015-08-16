package simple_streamer;

/**
 * @author quangdng
 */

/*
 * This class is responsible to parse a JSON message into JSON
 * object to access its contents.
 */

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FromJSON {

	private static final JSONParser parser = new JSONParser();

	public FromJSON() {

	}

	public JSONObject parse(String JSONString) {
		JSONObject obj;
		try {
			obj = (JSONObject) parser.parse(JSONString);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}

		if (obj != null) {
			return obj;
		} else {
			return null;
		}
	}
}
