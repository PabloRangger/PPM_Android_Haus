package com.example.cubemanagehome;


import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import setup.JSONParser;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Main extends Activity {
	
	

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		try {
			this.start();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void start() throws JSONException {

		final String wsuri = "ws://192.168.191.1:8999";
		final String TAG = "de.cube.tag";

		final String WSS_REQUEST_COUNT = "C!G:COUNT";
		final String WSS_REQUEST_ACTIVE = "C!G:ACTIVE";
		final String WSS_REQUEST_VALUE = "C!G:VALUE";
		// RESPONSES
		final String WSS_RESPONSE_ACTIVE = "W!E:ADD";
		final String WSS_RESPONSE_COUNT = "W!R:COUNT";
		final String WSS_RESPONSE_VALUE = "W!R:VALUE";
		final String WSS_RESPONSE_REMOVE = "W!E:REMOVE";

		final WebSocketConnection mConnection = new WebSocketConnection();

		final AlertDialog.Builder connectionlost = new AlertDialog.Builder(this)
				.setTitle("Connection lost")
				.setMessage(
						"The Connection to the Server was interrupted.\nTry to reconnect in the WLAN-Settings");

		final AlertDialog.Builder zerodevices = new AlertDialog.Builder(this)
				.setTitle("No Devices found")
				.setMessage(
						"At the moment no Devices are connected\nMaybe there is a problem on the Server");
		
		

		try {
			mConnection.connect(wsuri, new WebSocketHandler() {
				@Override
				public void onOpen() { // On open wird ausgeführt sobald die
										// connection steht
					
					HashMap<String, String> hm = new HashMap<String, String>();
					hm.put("type", WSS_REQUEST_COUNT);
					JSONObject obj = new JSONObject(hm);
					
					mConnection.sendTextMessage(obj.toString());
					
				}

				@Override
				public void onTextMessage(String payload) { // soll ausgeführt
					Log.d(TAG, payload); // werden sobald was
										// recieved wird

					try {
						JSONObject obj = new JSONObject(payload);
						
						JSONObject lol = obj.getJSONObject("data");
						int count = lol.getInt("count");

						if (count == 0) {
							Log.d(TAG, "PUNKTzero");
							zerodevices.show();

						} else {
//							Log.d(TAG, "PUNKT1");
//							JSONObject objct = getJSONfromURL("http://live.cube.at/api/device/1");
//							Log.d(TAG, "PUNKT2");
//							String name = objct.getString("name");
//							
//
//							Log.d(TAG, "Devicename: " + name);
							addlightButtons(1, "Lightbutton");
							addlightButtons(2, "Lightbutton");
						}

					} catch (Exception e) {
						Log.d(TAG, e.getMessage());
					}

				}

				@Override
				public void onClose(int code, String reason) { // wird
																// ausgeführt
																// wenn die
																// connection
																// gelostet wird
					connectionlost.show();
				}

				private JSONObject getJSONfromURL(String link) throws Exception {
					JSONParser jParser = new JSONParser();
					 
			        // Getting JSON from URL
			        JSONObject json = jParser.getJSONFromUrl(link);
			        
			        return json;
				}

				// @Override
				// public void onOpen() {
				// Log.d(TAG, "Status: Connected to " + wsuri);
				// mConnection.sendTextMessage("Hello, world!");
				// }
				//
				// @Override
				// public void onTextMessage(String payload) {
				// Log.d(TAG, "Got echo: " + payload);
				// }
				//
				// @Override
				// public void onClose(int code, String reason) {
				// Log.d(TAG, "Connection lost.");
				// }
			});
		} catch (WebSocketException e) {

			Log.d(TAG, e.toString());
		}
	}
	

	void deletelightButtons(int id){
		
		
		
		
	}
	
	void addlightButtons(int id, String bez){
		
		
		TableLayout ll = (TableLayout)findViewById(R.id.lout_main_table);		
		
		Button onButton = new Button(this);
		Button offButton = new Button(this);
		TextView name = new TextView(this);
		onButton.setText("ON");
		onButton.setBackgroundColor(Color.GREEN);
		onButton.setTextColor(Color.WHITE);
		onButton.setId(id+10000);
		
		//TODO: Onklicklistener
		
		offButton.setText("OFF");
		offButton.setBackgroundColor(Color.RED);
		offButton.setTextColor(Color.WHITE);
		offButton.setId(id+11000);
		
		//TODO: Onklicklistener

		name.setText(bez + id);
		
		
		TableRow tr = new TableRow(this);
		tr.setPadding(100, 100, 100, 100);
		tr.setBackgroundColor(Color.rgb(220, 220, 220));
		tr.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr.setId(id);
		
		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id+12000);
		
		
		tr2.addView(name);
		tr.addView(onButton);
		tr.addView(offButton);
		ll.addView(tr2);
		ll.addView(tr);
		
		
	}

	
}
