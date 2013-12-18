package com.example.cubemanagehome.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class API {
	public static final String API_LOCATION = "/api/location/";
	public static final String API_DEVICE = "/api/device/";
	public static final String API_USER = "/api/user/";
	public static final String API_PLUGIN = "/api/plugin/";
	public static final String API_VENDOR = "/api/vendor/";

	public static Object Request(String api, String uri, String request)
			throws IOException, JSONException {
		URL requestUri = new URL(uri + api + ((request != null) ? request : ""));
		BufferedReader br = new BufferedReader(new InputStreamReader(
				requestUri.openStream()));
		String line = "";
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		Object o;
		try {
			o = new JSONObject(sb.toString());
		} catch (Exception e) {
			o = new JSONArray(sb.toString());
		}
		return o;
	}
}
