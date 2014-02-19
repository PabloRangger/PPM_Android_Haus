package com.example.cubemanagehome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.EventObject;
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
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.cubemanagehome.utility.API;
import com.example.cubemanagehome.utility.API.APIErrorCallback;
import com.example.cubemanagehome.utility.API.APIResponseCallback;
import com.example.cubemanagehome.utility.PathManager;
import com.example.cubemanagehome.utility.Token;
import com.example.cubemanagehome.utility.Utility;
import com.example.cubemanagehome.utility.WebSocket;
import com.example.cubemanagehome.utility.WebSocket.ConnectedToServerEvent;
import com.example.cubemanagehome.utility.WebSocket.ConnectedToServerEventListener;
import com.example.cubemanagehome.utility.WebSocket.DisconnectedFromServerEvent;
import com.example.cubemanagehome.utility.WebSocket.DisconnectedFromServerEventListener;
import com.example.cubemanagehome.utility.WebSocket.MessageReceivedEvent;
import com.example.cubemanagehome.utility.WebSocket.MessageReceivedEventListener;
import com.example.cubemanagehome.utility.XML;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

public class Main extends Activity {

	String ip;
	String ip_request;
	final String TAG = "de.cube.tag";
	JSONObject obj;
	API api = API.getApiInstance();
	WebSocket ws = new WebSocket();
	boolean options = false;

	public void options(View view) {

		TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl = (TableLayout) findViewById(R.id.settings);

		ImageView tv = (ImageView) findViewById(R.id.optionsimg);
		tv.setClickable(false);
		if (!options) {

			Animation pushrightout = AnimationUtils.loadAnimation(this,
					R.anim.push_right_out);
			ll.startAnimation(pushrightout);
			ll.setVisibility(8);

			Animation pushrightin = AnimationUtils.loadAnimation(this,
					R.anim.push_right_in);
			tl.setVisibility(0);
			tl.startAnimation(pushrightin);

			options = true;
		} else {

			Animation pushrightout = AnimationUtils.loadAnimation(this,
					R.anim.push_left_in);
			ll.setVisibility(0);
			ll.startAnimation(pushrightout);

			Animation pushrightin = AnimationUtils.loadAnimation(this,
					R.anim.push_left_out);
			tl.startAnimation(pushrightin);
			tl.setVisibility(8);
			options = false;
		}

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {

			ImageView tv = (ImageView) findViewById(R.id.optionsimg);

			public void run() {
				tv.setClickable(true);
			}
		}, 600);

	}

	public void logout(View view) {
		File tkfile = PathManager.getAbsoluteFilePath(getApplicationContext(),
				PathManager.FILE_TOKEN);
		if (tkfile.delete()) {
			finish();
		} else {
			final AlertDialog.Builder alert_lof = new AlertDialog.Builder(this)
					.setTitle("Logout failed")
					.setMessage(
							"A critical Error occured, try to restart your app");

			alert_lof.show();

		}
	}

	protected void onCreate(Bundle savedInstanceState) {

		String serverip = new String();

		File myFile = PathManager.getAbsoluteFilePath(getApplicationContext(),
				PathManager.FILE_SERVERS);
		FileInputStream fIn = null;
		try {
			fIn = new FileInputStream(myFile);
		} catch (FileNotFoundException e1) {
			Log.d("MAINACT", "Exception 1");
		}
		BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
		try {
			myReader.readLine();
			serverip = myReader.readLine();
			myReader.close();
		} catch (IOException e1) {
			Log.d("MAINACT", "Exception 2");
		}

		ip = serverip + ":8999";
		ip_request = serverip;

		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		try {
			this.start();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WebSocketException e) {
			Log.d("MAINACT", "FETT WS EXCEPTION");
		}
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);

		setContentView(R.layout.activity_main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void start() throws WebSocketException, IOException {

		final AlertDialog.Builder connectionlost = new AlertDialog.Builder(this)
				.setTitle("Connection lost")
				.setMessage(
						"The Connection to the Server was interrupted.\nTry to reconnect in the WLAN-Settings");

		ws.addConnectListener(new ConnectedToServerEventListener() {
			public void onConnect(ConnectedToServerEvent e) {
				HashMap<String, Object> data = new HashMap<String, Object>();
				data.put("token", api.getTk().toString());
				ws.send(WebSocket.WSS_SET_TOKEN, data);
			}
		});

		ws.addDisconnectListener(new DisconnectedFromServerEventListener() {
			public void onDisconnect(DisconnectedFromServerEvent e) {
				connectionlost.show();
			}
		});

		ws.addMessageReceivedListener(new MessageReceivedEventListener() {

			public void onMessageReceived(MessageReceivedEvent e) {

				String type = e.getType();

				if (type.equals(WebSocket.WSS_RESPONSE_TOKEN_ACCEPT)) {
					ws.send(WebSocket.WSS_REQUEST_ACTIVE);
				}

			}
		});

		ws.connect(ip_request, 8999);

	}

	// final String WSS_REQUEST_COUNT = "C!G:COUNT";
	// final String WSS_REQUEST_ACTIVE = "C!G:ACTIVE";
	// final String WSS_REQUEST_VALUE = "C!G:VALUE";
	// // RESPONSES
	// final String WSS_RESPONSE_ACTIVE = "W!R:ADD";
	// final String WSS_RESPONSE_COUNT = "W!R:COUNT";
	// final String WSS_RESPONSE_VALUE = "W!R:VALUE";
	// final String WSS_RESPONSE_REMOVE = "W!R:REMOVE";
	//
	//
	//
	// final AlertDialog.Builder zerodevices = new AlertDialog.Builder(this)
	// .setTitle("No Devices found")
	// .setMessage(
	// "At the moment no Devices are connected\nMaybe there is a problem on the Server");
	// try {
	// mConnection.connect(Utility.getURI(Utility.URI_WEBSOCKET, ip),
	// new WebSocketHandler() {
	//
	// @Override
	// public void onOpen() { // On open wird ausgeführt sobald
	// // die
	// // connection steht
	//
	//
	// }
	//
	// @Override
	// public void onTextMessage(String payload) { // soll
	// // ausgeführt
	// // werden sobald was
	// // recieved wird
	// Log.d("PAYLOAD", payload);
	// int count=0;
	//
	// try {
	// obj = new JSONObject(payload);
	// String type = obj.getString("type");
	// if (type.equals(WSS_RESPONSE_COUNT)) {
	// JSONObject lol = obj.getJSONObject("data");
	// count = lol.getInt("count");
	// if (count > 0) {
	// HashMap<String, Object> mp = new HashMap<String, Object>();
	// mp.put("type", WSS_REQUEST_ACTIVE);
	// JSONObject o = new JSONObject(mp);
	// mConnection.sendTextMessage(o
	// .toString());
	// } else {
	// zerodevices.show();
	// }
	//
	// } else if (type.equals(WSS_RESPONSE_ACTIVE)) {
	//
	//
	// // new Thread(new Runnable() {
	// //
	// // @Override
	// // public void run() {
	// // int index = 1;
	// // boolean stop = false;
	// // while(!stop){
	// // String number = "" + index;
	// // JSONObject objct;
	// // try {
	// //
	// // objct = (JSONObject) API
	// // .Request(
	// // API.API_DEVICE,
	// // Utility.getURI(
	// // Utility.URI_HTTP,
	// // ip_request),
	// // number);
	// //
	// // int configured;
	// // configured = objct
	// // .getInt("configured");
	// //
	// // if (configured > 0) {
	// // final String name = objct
	// // .getString("name");
	// //
	// // final int id = objct.getInt("id");
	// //
	// // final JSONObject loc = objct.getJSONObject("location");
	// // final JSONObject plug = objct.getJSONObject("plugin");
	// //
	// // final String locname = loc.getString("name");
	// // final int locid = loc.getInt("id");
	// //
	// //
	// // if(plug.getString("objects").equals("toggle")){
	// // runOnUiThread(new Runnable() {
	// // public void run() {
	// // addlightButtons(id,
	// // name, locname, locid);
	// // }
	// // });
	// // }
	// // else if(plug.getString("objects").equals("number")){
	// // runOnUiThread(new Runnable() {
	// // public void run() {
	// // addTemperatureDisplay(id,
	// // name, locname, locid);
	// // changeTempValue(3, "21°");
	// // }
	// // });
	// // }
	// // else if(plug.getString("objects").equals("led")){
	// // runOnUiThread(new Runnable() {
	// // public void run() {
	// // addLedDisplay(id,
	// // name, locname, locid);
	// // changeLedState(4, 1);
	// // }
	// // });
	// // }
	// //
	// // }
	// // } catch (Exception e) {
	// // stop = true;
	// // }
	// // index++;
	// // }
	// // }
	// // }).start();
	//
	// }
	//
	//
	// }
	//
	// catch (Exception e) {
	// Log.d(TAG, e.getMessage());
	// }
	//
	// }
	//
	// @Override
	// public void onClose(int code, String reason) {
	// connectionlost.show();
	// }
	//
	// // @Override
	// // public void onOpen() {
	// // Log.d(TAG, "Status: Connected to " + wsuri);
	// // mConnection.sendTextMessage("Hello, world!");
	// // }
	// //
	// // @Override
	// // public void onTextMessage(String payload) {
	// // Log.d(TAG, "Got echo: " + payload);
	// // }
	// //
	// // @Override
	// // public void onClose(int code, String reason) {
	// // Log.d(TAG, "Connection lost.");
	// // }
	// });
	// } catch (Exception e) {
	//
	// Log.d(TAG, e.toString());
	// }
	//
	// }

	public void deleteDevice(int id, int locid) {
		final TableRow tr2 = (TableRow) findViewById(id + 12000);
		final TableRow tr = (TableRow) findViewById(id + 9000);
		final int idloc = locid;

		Animation pushdownin = AnimationUtils.loadAnimation(this,
				R.anim.push_left_out);
		tr.startAnimation(pushdownin);
		tr2.startAnimation(pushdownin);

		tr.removeAllViewsInLayout();
		tr2.removeAllViewsInLayout();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {

				TableLayout ll = (TableLayout) findViewById(idloc + 15000);
				ll.removeView(tr);
				ll.removeView(tr2);

				if (ll.getChildCount() == 1) {
					TableLayout tl = (TableLayout) findViewById(R.id.lout_main_table);
					tl.removeView(ll);
				}
			}
		}, 1000);

	}

	// TODO addDevice Ueberfunktion

	void addlightButtons(int id, String bez, String location, int locid) {
		final int idcpy = id;
		TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
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
				data.put("id", idstr);
				data.put("value", "1");
				JSONObject obje = new JSONObject(data);

				hm.put("data", obje);
				JSONObject obj = new JSONObject(hm);

				// TODO

				Log.d("CUBE", obj.toString());
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
				data.put("id", idstr);
				data.put("value", "0");
				JSONObject obje = new JSONObject(data);

				hm.put("data", obje);
				JSONObject obj = new JSONObject(hm);

				//TODO

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
		tr.setId(id + 9000);
		tr.setClickable(true);
		tr.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				new Thread(new Runnable() {
					JSONObject objct;

					@Override
					public void run() {

						String number = "" + idcpy;

						try {

							api.asyncGetApiRequest(API.API_DEVICE, number,
									new APIResponseCallback() {

										@Override
										public void onResult(JSONObject o) {
											objct = o;

										}
									}, null);

							int configured;
							configured = objct.getInt("configured");

							if (configured > 0) {
								final String name = objct.getString("name");

								final int id = objct.getInt("id");

								final String cat = objct
										.getString("created_at");

								final String description = objct
										.getString("description");

								runOnUiThread(new Runnable() {
									public void run() {
										deviceinfo
												.setTitle(name + " " + id)
												.setMessage(
														"Name: "
																+ name
																+ "\nID: "
																+ id
																+ "\nCreated At: "
																+ cat
																+ "\nDescription: "
																+ description);
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

		if (findViewById(locid + 15000) == null) {
			tl = new TableLayout(this);
			tl.setId(locid + 15000);

			TableRow tr3 = new TableRow(this);
			tr3.setMinimumWidth(LayoutParams.WRAP_CONTENT);
			tr3.setMinimumHeight(LayoutParams.WRAP_CONTENT);
			tr3.setPadding(0, 40, 0, 40);
			tr3.setId(locid + 16000);

			TextView locname = new TextView(this);
			locname.setText(location);

			TableLayout tab = (TableLayout) findViewById(R.id.settings);
			TextView locnav = new TextView(this);
			locnav.setText("\t- " + location);
			TableRow tabr = new TableRow(this);
			tabr.setPadding(0, 2, 0, 2);

			tabr.addView(locnav);
			tab.addView(tabr);

			tr3.addView(locname);

			tl.addView(tr3);
			ll.addView(tl);
		} else {
			tl = (TableLayout) findViewById(locid + 15000);

		}

		LinearLayout tr_h = new LinearLayout(this);
		tr_h.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr_h.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr_h.addView(onButton);
		tr_h.addView(offButton);

		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id + 12000);

		tr2.addView(name);

		Animation pushdownin = AnimationUtils.loadAnimation(this,
				R.anim.push_left_in);
		tr.startAnimation(pushdownin);
		tr2.startAnimation(pushdownin);

		tr.addView(tr_h);
		tl.addView(tr2);
		tl.addView(tr);

	}

	@SuppressLint("NewApi")
	void addTemperatureDisplay(int id, String bez, String location, int locid) {

		final int idcpy = id;
		int idlol = id + 17000;
		TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl;
		TextView tv = new TextView(this);
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
		tr.setId(id + 9000);
		tr.setClickable(true);
		tr.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				new Thread(new Runnable() {
					JSONObject objct;

					@Override
					public void run() {

						String number = "" + idcpy;

						try {

							api.asyncGetApiRequest(API.API_DEVICE, number,
									new APIResponseCallback() {

										@Override
										public void onResult(JSONObject o) {
											objct = o;

										}
									}, null);

							int configured;
							configured = objct.getInt("configured");

							if (configured > 0) {
								final String name = objct.getString("name");

								final int id = objct.getInt("id");

								final String cat = objct
										.getString("created_at");

								final String description = objct
										.getString("description");

								runOnUiThread(new Runnable() {
									public void run() {
										deviceinfo
												.setTitle(name + " " + id)
												.setMessage(
														"Name: "
																+ name
																+ "\nID: "
																+ id
																+ "\nCreated At: "
																+ cat
																+ "\nDescription: "
																+ description);
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

		if (findViewById(locid + 15000) == null) {
			tl = new TableLayout(this);
			tl.setId(locid + 15000);

			TableRow tr3 = new TableRow(this);
			tr3.setMinimumWidth(LayoutParams.WRAP_CONTENT);
			tr3.setMinimumHeight(LayoutParams.WRAP_CONTENT);
			tr3.setPadding(0, 40, 0, 40);
			tr3.setId(locid + 16000);

			TextView locname = new TextView(this);
			locname.setText(location);

			TableLayout tab = (TableLayout) findViewById(R.id.settings);
			TextView locnav = new TextView(this);
			locnav.setText("\t- " + location);
			TableRow tabr = new TableRow(this);
			tabr.setPadding(0, 2, 0, 2);

			tabr.addView(locnav);
			tab.addView(tabr);

			tr3.addView(locname);

			tl.addView(tr3);
			ll.addView(tl);
		} else {
			tl = (TableLayout) findViewById(locid + 15000);
		}

		tr.addView(tv);

		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id + 12000);

		tr2.addView(name);

		Animation pushdownin = AnimationUtils.loadAnimation(this,
				R.anim.push_left_in);
		tr.startAnimation(pushdownin);
		tr2.startAnimation(pushdownin);

		tl.addView(tr2);
		tl.addView(tr);

	}

	void changeTempValue(int id, String value) {

		TextView temp = (TextView) findViewById(id + 17000);
		temp.setText(value);
		temp.setTextSize(50);

	}

	@SuppressLint("NewApi")
	void addLedDisplay(int id, String bez, String location, int locid) {

		final int idcpy = id;
		TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl;

		ImageView img = new ImageView(this);
		img.setBackgroundResource(R.drawable.kreis_schwarz);
		img.setId(idcpy + 18000);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				100, 100);
		img.setLayoutParams(layoutParams);

		TextView name = new TextView(this);

		final AlertDialog.Builder deviceinfo = new AlertDialog.Builder(this);

		name.setText(bez + " " + id);

		TableRow tr = new TableRow(this);
		tr.setPadding(100, 100, 100, 100);
		tr.setBackgroundColor(Color.rgb(220, 220, 220));
		tr.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr.setId(id + 9000);
		tr.setClickable(true);
		tr.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {

				new Thread(new Runnable() {
					JSONObject objct;

					@Override
					public void run() {

						String number = "" + idcpy;

						try {

							api.asyncGetApiRequest(API.API_DEVICE, number,
									new APIResponseCallback() {

										@Override
										public void onResult(JSONObject o) {
											objct = o;

										}
									}, null);

							int configured;
							configured = objct.getInt("configured");

							if (configured > 0) {
								final String name = objct.getString("name");

								final int id = objct.getInt("id");

								final String cat = objct
										.getString("created_at");

								final String description = objct
										.getString("description");

								runOnUiThread(new Runnable() {
									public void run() {
										deviceinfo
												.setTitle(name + " " + id)
												.setMessage(
														"Name: "
																+ name
																+ "\nID: "
																+ id
																+ "\nCreated At: "
																+ cat
																+ "\nDescription: "
																+ description);
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

		if (findViewById(locid + 15000) == null) {
			tl = new TableLayout(this);
			tl.setId(locid + 15000);

			TableRow tr3 = new TableRow(this);
			tr3.setMinimumWidth(LayoutParams.WRAP_CONTENT);
			tr3.setMinimumHeight(LayoutParams.WRAP_CONTENT);
			tr3.setPadding(0, 40, 0, 40);
			tr3.setId(locid + 16000);

			TextView locname = new TextView(this);
			locname.setText(location);

			TableLayout tab = (TableLayout) findViewById(R.id.settings);
			TextView locnav = new TextView(this);
			locnav.setText("\t- " + location);
			TableRow tabr = new TableRow(this);
			tabr.setPadding(0, 2, 0, 2);

			tabr.addView(locnav);
			tab.addView(tabr);

			tr3.addView(locname);

			tl.addView(tr3);
			ll.addView(tl);
		} else {
			tl = (TableLayout) findViewById(locid + 15000);
		}

		LinearLayout tr_h = new LinearLayout(this);
		tr_h.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr_h.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr_h.addView(img);

		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.WRAP_CONTENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id + 12000);

		tr.addView(tr_h);
		tr2.addView(name);

		Animation pushdownin = AnimationUtils.loadAnimation(this,
				R.anim.push_left_in);
		tr.startAnimation(pushdownin);
		tr2.startAnimation(pushdownin);

		tl.addView(tr2);
		tl.addView(tr);

	}

	void changeLedState(int id, int value) {

		ImageView img = (ImageView) findViewById(id + 18000);

		if (value > 0) {

			img.setBackgroundResource(R.drawable.kreis_orange);

		} else {

			img.setBackgroundResource(R.drawable.kreis_schwarz);

		}

	}

}
