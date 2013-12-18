package com.example.cubemanagehome.utility;

import java.net.MalformedURLException;
import java.net.URL;

public class Utility {
	public enum URIType {
		URI_WEBSOCKET, URI_WEBSOCKET_S, URI_HTTP, URI_HTTPS
	}

	public static String getURI(URIType type, String host, int port)
			throws MalformedURLException {
		String prefix = "";
		switch (type) {
		case URI_HTTP:
			prefix = "http";
			break;
		case URI_HTTPS:
			prefix = "https";
			break;
		case URI_WEBSOCKET:
			prefix = "ws";
			break;
		case URI_WEBSOCKET_S:
			prefix = "wss";
			break;
		default:
			throw new MalformedURLException();
		}
		return prefix + "://" + host + ((port == 0) ? "" : (":" + port));
	}

	public static String getURI(URIType type, String host) throws MalformedURLException {
		return Utility.getURI(type, host, 0);
	}
}
