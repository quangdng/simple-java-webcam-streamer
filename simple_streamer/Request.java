package simple_streamer;

/**
 * @author quangdng
 */

import org.json.simple.parser.JSONParser;

/*
 * This is an abstract class for all kind of request to extends
 */

abstract class Request {

	protected static final JSONParser parser = new JSONParser();

	abstract String Type();

	abstract String ToJSON();
}