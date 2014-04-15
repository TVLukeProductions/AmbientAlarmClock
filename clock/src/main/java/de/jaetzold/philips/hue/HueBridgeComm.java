package de.jaetzold.philips.hue;

//Copyright (c) 2013 Stephan Jaetzold.
//
//Licensed under the Apache License, Version 2.0 (the "License");
//You may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and limitations under the License.
//
//This Source may have been modified for this project, the original ois maintained and can be found at https://github.com/jaetzold/philips-hue-java-sdk

import org.json2.JSONArray;
import org.json2.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.jaetzold.networking.SimpleServiceDiscovery;

import android.util.Log;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Encapsulate the actual network communication with the bridge device
 *
 * @author Stephan Jaetzold <p><small>Created at 21.03.13, 15:06</small>
 */
class HueBridgeComm
{
	Logger log = Logger.getLogger(this.getClass().getName());
//	{
//		log.setLevel(Level.ALL);
//		final ConsoleHandler handler = new ConsoleHandler();
//		handler.setLevel(Level.ALL);
//		log.addHandler(handler);
//	}

	final URL baseUrl;
	private static final String REQUEST_CHARSET = "UTF-8";
	static enum RM 
	{
		GET,POST,PUT,DELETE
	}

	HueBridgeComm(URL baseUrl) {
		this.baseUrl = baseUrl;
	}

	@SuppressWarnings("UnusedDeclaration")
	List<JSONObject> request(RM method, String fullPath, JSONObject json) throws IOException {
		return request(method, fullPath, json.toString());
	}

	List<JSONObject> request(RM method, String fullPath, String json) throws IOException {
		final URL url = new URL(baseUrl, fullPath);
		log.fine("Request to " +method +" " + url +(json!=null && json.length()>0 ? ": "+json : ""));
		final URLConnection connection = url.openConnection();
		if(connection instanceof HttpURLConnection) {
			HttpURLConnection httpConnection = (HttpURLConnection)connection;
			httpConnection.setRequestMethod(method.name());
			if(json!=null && json.length()>0) {
				if(method==RM.GET || method==RM.DELETE) {
					throw new HueCommException("Will not send json content for request method " +method);
				}
				httpConnection.setDoOutput(true);
				httpConnection.getOutputStream().write(json.getBytes(REQUEST_CHARSET));
			}
		} else {
			throw new IllegalStateException("The current baseUrl \"" +baseUrl +"\" is not http?");
		}

		final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), REQUEST_CHARSET));
		StringBuilder builder = new StringBuilder();
		String line;
		while((line = reader.readLine())!=null) {
			builder.append(line).append('\n');
		}
		log.fine("Response from " + method + " " + url + ": " + builder);
		final String jsonString = builder.toString();
		List<JSONObject> result = new ArrayList<JSONObject>();
		if(jsonString.trim().startsWith("{")) {
			result.add(new JSONObject(jsonString));
		} else {
			final JSONArray array = new JSONArray(jsonString);
			for(int i=0; i<array.length(); i++) {
				result.add(array.getJSONObject(i));
			}
		}
		return result;
	}

	static List<HueBridge> discover() 
	{
		Log.d("HUE", "BridgeComm.discover");
		final Logger log = Logger.getLogger(HueBridge.class.getName());
		final SimpleServiceDiscovery serviceDiscovery = new SimpleServiceDiscovery();
		int attempted = 0;
		int maxAttempts = Math.min(4, Math.max(1, HueBridge.discoveryAttempts));
		Map<String, URL> foundBriges = new HashMap<String, URL>();
		// if nothing is found the first time try up to maxAttempts times with increasing timeouts
		while(foundBriges.isEmpty() && attempted<maxAttempts) {
			serviceDiscovery.setSearchMx(1+attempted);
			serviceDiscovery.setSocketTimeout(500 + attempted*1500);
			foundBriges = serviceDiscovery.discover(SimpleServiceDiscovery.SEARCH_TARGET_ROOTDEVICE);
			attempted++;
		}
		List<HueBridge> result = new ArrayList<HueBridge>();
		for(Map.Entry<String, URL> entry : foundBriges.entrySet()) {
			final HueBridge bridge = new HueBridge(entry.getValue(), null);
			bridge.UDN = entry.getKey();
			result.add(bridge);
		}
		return result;
	}
}