package de.thingweb.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.JPanel;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;

import de.thingweb.client.Client;
import de.thingweb.client.ClientFactory;
import de.thingweb.client.UnsupportedException;
import de.thingweb.desc.ThingDescriptionParser;
import de.thingweb.gui.ThingPanelUI;
import de.thingweb.thing.Thing;
import junit.framework.TestCase;

public class TestThingPanelUI extends TestCase {

	@Test
	public void testUrlTutorialDoor() throws JsonParseException, IOException, UnsupportedException, URISyntaxException {
		URL jsonld = new URL("https://raw.githubusercontent.com/w3c/wot/master/TF-TD/TD%20Samples/door.jsonld");
		ClientFactory cf = new ClientFactory();
		Client client = cf.getClientUrl(jsonld);
		@SuppressWarnings("unused")
		JPanel panelLed = new ThingPanelUI(client);
	}
	
	@Test
	// inputData outputData null or ""
	public void testLocal1() throws JsonParseException, IOException, UnsupportedException, URISyntaxException {
		String foo ="{\"metadata\":{\"name\":\"Ugly strange n\u00E4ime\",\"protocols\":{\"CoAP\":{\"uri\":\"coap://MD1EWQUC/things/ugly+strange+n%c3%a4ime\",\"priority\":1},\"HTTP\":{\"uri\":\"http://MD1EWQUC:8080/things/ugly+strange+n%c3%a4ime\",\"priority\":2}},\"encodings\":[\"JSON\"]},\"interactions\":[{\"@type\":\"Property\",\"name\":\"not url komp\u00E4tibel\",\"writable\":false,\"outputData\":\"xsd:string\"},{\"@type\":\"Action\",\"name\":\"wierdly named \u00E4ktschn\",\"inputData\":null,\"outputData\":\"\"}],\"@context\":\"http://w3c.github.io/wot/w3c-wot-td-context.jsonld\"}";
		Thing td = ThingDescriptionParser.fromBytes(foo.getBytes());
		
		ClientFactory cf = new ClientFactory();
		Client client = cf.getClientFromTD(td);
		
		@SuppressWarnings("unused")
		JPanel panelLed = new ThingPanelUI(client);
	}

}
