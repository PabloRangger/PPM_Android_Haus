package com.example.cubemanagehome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import setup.JSONParser;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.cubemanagehome.utility.API;
import com.example.cubemanagehome.utility.Utility;
import com.example.cubemanagehome.utility.Utility.URIType;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketHandler;


public class Main extends Activity {
	
	String ip;
	String ip_request;
	final String TAG = "de.cube.tag";
	JSONObject obj;
	final WebSocketConnection mConnection = new WebSocketConnection();

	@SuppressLint("NewApi")
	

	
	public void options(View view){
		
		
		
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		
		String servername = new String();
		String serverip = new String();
		
		
		
		File myFile = new File("/storage/emulated/0/cube/cube.txt");
		FileInputStream fIn = null;
		try {
			fIn = new FileInputStream(myFile);
		} catch (FileNotFoundException e1) {
			Log.d("HELLOACT", "Exception 1");
		}
		BufferedReader myReader = new BufferedReader(
				new InputStreamReader(fIn));
		try {
			servername = myReader.readLine();
			serverip = myReader.readLine();
			myReader.close();
		} catch (IOException e1) {
			Log.d("HELLOACT", "Exception 2");
		}
		
		ip = serverip + ":8999";
		ip_request = serverip;
		
		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		try {
			this.start();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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

	private void start() throws JSONException, IOException {

		

		final String WSS_REQUEST_COUNT = "C!G:COUNT";
		final String WSS_REQUEST_ACTIVE = "C!G:ACTIVE";
		final String WSS_REQUEST_VALUE = "C!G:VALUE";
		// RESPONSES
		final String WSS_RESPONSE_ACTIVE = "W!R:ADD";
		final String WSS_RESPONSE_COUNT = "W!R:COUNT";
		final String WSS_RESPONSE_VALUE = "W!R:VALUE";
		final String WSS_RESPONSE_REMOVE = "W!R:REMOVE";

		
		
		
		

		final AlertDialog.Builder connectionlost = new AlertDialog.Builder(this)
				.setTitle("Connection lost")
				.setMessage(
						"The Connection to the Server was interrupted.\nTry to reconnect in the WLAN-Settings");

		final AlertDialog.Builder zerodevices = new AlertDialog.Builder(this)
				.setTitle("No Devices found")
				.setMessage(
						"At the moment no Devices are connected\nMaybe there is a problem on the Server");
		try {
			mConnection.connect(Utility.getURI(URIType.URI_WEBSOCKET, ip),
					new WebSocketHandler() {
				
						@Override
						public void onOpen() { // On open wird ausgeführt sobald
												// die
												// connection steht
							Log.d("", "Point 7");
							HashMap<String, String> hm = new HashMap<String, String>();
							hm.put("type", WSS_REQUEST_COUNT);
							JSONObject obj = new JSONObject(hm);

							mConnection.sendTextMessage(obj.toString());

						}

						@Override
						public void onTextMessage(String payload) { // soll
																	// ausgeführt
																	// werden sobald was
																	// recieved wird
							Log.d("PAYLOAD", payload);
							int count=0;
							
							try {
								 obj = new JSONObject(payload);
								String type = obj.getString("type");
								if (type.equals(WSS_RESPONSE_COUNT)) {
									JSONObject lol = obj.getJSONObject("data");
									count = lol.getInt("count");
									if (count > 0) {
										HashMap<String, Object> mp = new HashMap<String, Object>();
										mp.put("type", WSS_REQUEST_ACTIVE);
										JSONObject o = new JSONObject(mp);
										mConnection.sendTextMessage(o
												.toString());
									} else {
										zerodevices.show();
									}

								} else if (type.equals(WSS_RESPONSE_ACTIVE)) {
									
									
									new Thread(new Runnable() {
									
										@Override
										public void run() {
											int index = 1;
											boolean stop = false;
											while(!stop){
											String number = "" + index;
											JSONObject objct;
											try {
												
												objct = (JSONObject) API
														.Request(
																API.API_DEVICE,
																Utility.getURI(
																		URIType.URI_HTTP,
																		ip_request),
																number);
												
												int configured;
												configured = objct
														.getInt("configured");
												
												if (configured > 0) {
													final String name = objct
															.getString("name");
													
													final int id = objct.getInt("id");
													
													final JSONObject loc = objct.getJSONObject("location");
													final JSONObject plug = objct.getJSONObject("plugin");
													
													final String locname = loc.getString("name");
													final int locid = loc.getInt("id");
													
													
													if(plug.getString("objects").equals("toggle")){
													runOnUiThread(new Runnable() {
														public void run() {
															addlightButtons(id,
																	name, locname, locid);
														}
													});
													}
													else if(plug.getString("objects").equals("number")){
														runOnUiThread(new Runnable() {
															public void run() {
																addTemperatureDisplay(id,
																		name, locname, locid);
																changeTempValue(3, "21°");
															}
														});
													}
													else if(plug.getString("objects").equals("led")){
														runOnUiThread(new Runnable() {
															public void run() {
																addLedDisplay(id,
																		name, locname, locid);
																changeLedState(4, 1);
															}
														});
													}
													
												}
											} catch (Exception e) {
												stop = true;
											}
											index++;
										}
										}
									}).start();
									
								}

							
							}
								
							catch (Exception e) {
								Log.d(TAG, e.getMessage());
							}

						}

						@Override
						public void onClose(int code, String reason) { 
							connectionlost.show();
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
		} catch (Exception e) {

			Log.d(TAG, e.toString());
		}
		
	}

	void deletelightButtons(int id) {
		Button onButton = (Button) findViewById(id + 10000);
		onButton.setVisibility(View.INVISIBLE); // TODO
		Button offButton = (Button) findViewById(id + 11000);
		offButton.setVisibility(View.INVISIBLE);
	}

	void addlightButtons(int id, String bez, String location, int locid) {
		final int idcpy = id;
		TableLayout ll =  (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl;
		
		
		Button onButton = new Button(this);
		TextView name = new TextView(this);
		onButton.setText("ON");
		onButton.setBackgroundColor(Color.argb(255, 255, 102, 0));
		onButton.setId(id + 10000);

		onButton.setOnClickListener(new OnClickListener() {
			
			String idstr = "" + idcpy;
			
			@Override
			public void onClick(View v) {
				
				HashMap<String, Object> hm = new HashMap<String, Object>();
				hm.put("type", "C!G:VALUE");
				
				HashMap<String, String> data = new HashMap<String, String>();
				data.put("id", idstr);					//TODO Stimmt nicht!!
				data.put("value", "1");
				JSONObject obje = new JSONObject(data);
				
				hm.put("data", obje);
				JSONObject obj = new JSONObject(hm);

				mConnection.sendTextMessage(obj.toString());
				
				Log.d("CUBEEEE", obj.toString());
			}
		});
		Button offButton = new Button(this);
		offButton.setText("OFF");
		offButton.setBackgroundColor(Color.LTGRAY);
		offButton.setId(id + 11000);
		
		offButton.setOnClickListener(new OnClickListener() {
			
			String idstr = "" + idcpy;
			
			@Override
			public void onClick(View v) {
				
				HashMap<String, Object> hm = new HashMap<String, Object>();
				hm.put("type", "C!G:VALUE");
				
				HashMap<String, String> data = new HashMap<String, String>();
				data.put("id", idstr);					//TODO Stimmt nicht!!
				data.put("value", "0");
				JSONObject obje = new JSONObject(data);
				
				hm.put("data", obje);
				JSONObject obj = new JSONObject(hm);

				mConnection.sendTextMessage(obj.toString());
				
				Log.d("CUBEEEE", obj.toString());
				
			}
		});
		
		final AlertDialog.Builder deviceinfo = new AlertDialog.Builder(this);

		name.setText(bez + " " + id);
		

		TableRow tr = new TableRow(this);
		tr.setPadding(100, 100, 100, 100);
		tr.setBackgroundColor(Color.rgb(220, 220, 220));
		tr.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr.setId(id);
		tr.setClickable(true);
		tr.setOnClickListener(new OnClickListener(){

		
			public void onClick(View arg0) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						String number = "" + idcpy;
						JSONObject objct;
						try {
							
							objct = (JSONObject) API
									.Request(
											API.API_DEVICE,
											Utility.getURI(
													URIType.URI_HTTP,
													ip_request),
											number);

							int configured;
							configured = objct
									.getInt("configured");

							if (configured > 0) {
								final String name = objct
										.getString("name");
								
								final int id = objct.getInt("id");
								
								final String cat = objct.getString("created_at");
								
								final String description = objct.getString("description");

								runOnUiThread(new Runnable() {
									public void run() {
										deviceinfo.setTitle(name + " " + id).setMessage("Name: " + name + "\nID: " + id + "\nCreated At: " + cat + "\nDescription: " + description);
										deviceinfo.show();
									}
								});
								
							}
						} catch (Exception e) {
							Log.d(TAG, "geht nix");
						}
					
					}
				}).start();
				
			}
			
			
		});
		
		if(findViewById(locid+15000) == null){
			tl = new TableLayout(this);
			tl.setId(locid + 15000);
			
			TableRow tr3 = new TableRow(this);
			tr3.setMinimumWidth(LayoutParams.WRAP_CONTENT);
			tr3.setMinimumHeight(LayoutParams.WRAP_CONTENT);
			tr3.setPadding(0, 40, 0, 40);
			tr3.setId(locid + 16000);
			
			TextView locname = new TextView(this);
			locname.setText(location);
			
			tr3.addView(locname);
			
			tl.addView(tr3);
			ll.addView(tl);
	}
		else{
			tl =  (TableLayout) findViewById(locid + 15000);
			
		}
		
		
		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT); 
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id + 12000);
		
		FrameLayout fl = new FrameLayout(this);
		fl.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		fl.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		
		
		tr2.addView(name);
		fl.addView(onButton);
		fl.addView(offButton);
		tr.addView(fl);
		tl.addView(tr2);
		tl.addView(tr);
		

	}

	@SuppressLint("NewApi")
	void addTemperatureDisplay(int id, String bez, String location, int locid){
		
		final int idcpy = id;
		int idlol = id + 17000;
		TableLayout ll =  (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl;
		TextView tv= new TextView(this);
		tv.setText("No value set!");
		tv.setId(idlol);
		tv.setTextSize(20);
		
		TextView name = new TextView(this);
		
		final AlertDialog.Builder deviceinfo = new AlertDialog.Builder(this);

		name.setText(bez + " " + id);
		

		TableRow tr = new TableRow(this);
		tr.setPadding(100, 100, 100, 100);
		tr.setBackgroundColor(Color.rgb(220, 220, 220));
		tr.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr.setId(id);
		tr.setClickable(true);
		tr.setOnClickListener(new OnClickListener(){

		
			public void onClick(View arg0) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						String number = "" + idcpy;
						JSONObject objct;
						try {
							
							objct = (JSONObject) API
									.Request(
											API.API_DEVICE,
											Utility.getURI(
													URIType.URI_HTTP,
													ip_request),
											number);

							int configured;
							configured = objct
									.getInt("configured");

							if (configured > 0) {
								final String name = objct
										.getString("name");
								
								final int id = objct.getInt("id");
								
								final String cat = objct.getString("created_at");
								
								final String description = objct.getString("description");

								runOnUiThread(new Runnable() {
									public void run() {
										deviceinfo.setTitle(name + " " + id).setMessage("Name: " + name + "\nID: " + id + "\nCreated At: " + cat + "\nDescription: " + description);
										deviceinfo.show();
									}
								});
								
							}
						} catch (Exception e) {
							Log.d(TAG, "geht nix");
						}
					
					}
				}).start();
				
			}
			
	
		});
		
		if(findViewById(locid+15000) == null){
			tl = new TableLayout(this);
			tl.setId(locid + 15000);
			
			TableRow tr3 = new TableRow(this);
			tr3.setMinimumWidth(LayoutParams.WRAP_CONTENT);
			tr3.setMinimumHeight(LayoutParams.WRAP_CONTENT);
			tr3.setPadding(0, 40, 0, 40);
			tr3.setId(locid + 16000);
			
			TextView locname = new TextView(this);
			locname.setText(location);
			
			tr3.addView(locname);
			
			tl.addView(tr3);
			ll.addView(tl);
	}
		else{
			tl =  (TableLayout) findViewById(locid + 15000);
		}
		
		tr.addView(tv);
		
		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT); 
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id + 12000);
		
		tr2.addView(name);
		
		tl.addView(tr2);
		tl.addView(tr);
		
		
		
	}
	
	void changeTempValue(int id, String value){
		
		TextView temp = (TextView) findViewById(id + 17000);
		temp.setText(value);
		temp.setTextSize(50);
		
	}
	
	@SuppressLint("NewApi")
	void addLedDisplay(int id, String bez, String location, int locid){
		
		final int idcpy = id;
		TableLayout ll =  (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl;
		
		
		ImageView img = new ImageView(this);
		img.setBackgroundResource(R.drawable.kreis_schwarz);
		img.setMaxHeight(10);
		img.setMaxWidth(10);
		img.setId(idcpy + 18000);
		
		TextView name = new TextView(this);
		
		final AlertDialog.Builder deviceinfo = new AlertDialog.Builder(this);

		name.setText(bez + " " + id);
		

		TableRow tr = new TableRow(this);
		tr.setPadding(100, 100, 100, 100);
		tr.setBackgroundColor(Color.rgb(220, 220, 220));
		tr.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr.setId(id);
		tr.setClickable(true);
		tr.setOnClickListener(new OnClickListener(){

		
			public void onClick(View arg0) {
				
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						
						String number = "" + idcpy;
						JSONObject objct;
						try {
							
							objct = (JSONObject) API
									.Request(
											API.API_DEVICE,
											Utility.getURI(
													URIType.URI_HTTP,
													ip_request),
											number);

							int configured;
							configured = objct
									.getInt("configured");

							if (configured > 0) {
								final String name = objct
										.getString("name");
								
								final int id = objct.getInt("id");
								
								final String cat = objct.getString("created_at");
								
								final String description = objct.getString("description");

								runOnUiThread(new Runnable() {
									public void run() {
										deviceinfo.setTitle(name + " " + id).setMessage("Name: " + name + "\nID: " + id + "\nCreated At: " + cat + "\nDescription: " + description);
										deviceinfo.show();
									}
								});
								
							}
						} catch (Exception e) {
							Log.d(TAG, "geht nix");
						}
					
					}
				}).start();
				
			}
			
	
		});
		
		if(findViewById(locid+15000) == null){
			tl = new TableLayout(this);
			tl.setId(locid + 15000);
			
			TableRow tr3 = new TableRow(this);
			tr3.setMinimumWidth(LayoutParams.WRAP_CONTENT);
			tr3.setMinimumHeight(LayoutParams.WRAP_CONTENT);
			tr3.setPadding(0, 40, 0, 40);
			tr3.setId(locid + 16000);
			
			TextView locname = new TextView(this);
			locname.setText(location);
			
			tr3.addView(locname);
			
			tl.addView(tr3);
			ll.addView(tl);
	}
		else{
			tl =  (TableLayout) findViewById(locid + 15000);
		}
		
		
		
		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT); 
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id + 12000);
		
		
		tr.addView(img);
		tr2.addView(name);
		
		tl.addView(tr2);
		tl.addView(tr);
		
		
		
	}
	
	void changeLedState(int id, int value){
		
		ImageView img = (ImageView) findViewById(id + 18000);
		
		if(value > 0){
			
			img.setBackgroundResource(R.drawable.kreis_orange);
			
		}
		else{
			
			img.setBackgroundResource(R.drawable.kreis_schwarz);
			
		}
		
	}
	
}
