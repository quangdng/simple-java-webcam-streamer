package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.parser.JSONParser;

/*
 * This is an abstract class for all kind of response to extends
 */

abstract class Response {

	protected static final JSONParser parser = new JSONParser();

	abstract String Type();

	abstract String ToJSON();
}