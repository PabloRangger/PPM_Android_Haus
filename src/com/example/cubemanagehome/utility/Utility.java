package com.example.cubemanagehome.utility;

import java.net.MalformedURLException;

import com.example.cubemanagehome.Main;

import android.content.Context;
import android.content.Intent;

public class Utility {

	public static final String URI_WEBSOCKET = "ws";
	public static final String URI_WEBSOCKET_S = "wss";
	public static final String URI_HTTP = "http";
	public static final String URI_HTTPS = "https";

	public static String getURI(String type, String host, int port)
			throws MalformedURLException {
		return type + "://" + host + ((port == 0) ? "" : (":" + port));
	}

	public static String getURI(String type, String host)
			throws MalformedURLException {
		return Utility.getURI(type, host, 0);
	}

	public static String getURLFromInput(String input) {
		input = (input.startsWith(URI_HTTP) || input.startsWith(URI_HTTPS)) ? input
				: URI_HTTP + "://" + input;
		return input;
	}

	public static String getAddressFromInput(String string) {
		string = string.replace(URI_HTTP + "://", "");
		string = string.replace(URI_HTTPS + "://", "");
		return string;
	}

	public static void switchToMainActivity(Context c) {
		Utility.switchToActivity(Main.class, c);
	}

	public static void switchToActivity(Class<?> c, Context context) {
		Intent intent = new Intent(context, c);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		context.startActivity(intent);
	}
}
