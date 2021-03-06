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

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.cubemanagehome.utility.API;
import com.example.cubemanagehome.utility.PathManager;
import com.example.cubemanagehome.utility.WebSocket;
import com.example.cubemanagehome.utility.WebSocket.ConnectedToServerEvent;
import com.example.cubemanagehome.utility.WebSocket.ConnectedToServerEventListener;
import com.example.cubemanagehome.utility.WebSocket.DisconnectedFromServerEvent;
import com.example.cubemanagehome.utility.WebSocket.DisconnectedFromServerEventListener;
import com.example.cubemanagehome.utility.WebSocket.MessageReceivedEvent;
import com.example.cubemanagehome.utility.WebSocket.MessageReceivedEventListener;

import de.tavendo.autobahn.WebSocketException;


public class Main extends Activity {

	String ip;
	String ip_request;
	final String TAG = "de.cube.tag";
	JSONObject obj;
	API api = API.getApiInstance();
	WebSocket ws = new WebSocket();
	boolean options = false;
	
	public void ihrprofil(View view){
		Intent intent = new Intent(getApplication(), YourProfile.class);
		startActivity(intent);
	}

	public void options(View view) {

		TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl = (TableLayout) findViewById(R.id.settings);

		int childcount = ll.getChildCount();
		for (int i=0; i < childcount; i++){
		      TableLayout v = (TableLayout) ll.getChildAt(i);
		      int childs = v.getChildCount();
		      for (int j=0; j < childs; j++){
			      View vw = v.getChildAt(j);
			      vw.setClickable(false);
			}
		}
		
		int childc = tl.getChildCount();
		for (int i=0; i < childc; i++){
		      View v = tl.getChildAt(i);
		      v.setClickable(false);
		}
		
		ImageView tv = (ImageView) findViewById(R.id.optionsimg);
		tv.setClickable(false);
		if (!options) {

			Animation pushrightout = AnimationUtils.loadAnimation(this,
					R.anim.push_right_out);
			pushrightout.setAnimationListener(new Animation.AnimationListener(){
				ImageView tv = (ImageView) findViewById(R.id.optionsimg);
				TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
				TableLayout tl = (TableLayout) findViewById(R.id.settings);
			    public void onAnimationStart(Animation a){}
			    public void onAnimationRepeat(Animation a){}
			    public void onAnimationEnd(Animation a){
			    	tv.setClickable(true);
			    	int childcount = ll.getChildCount();
					for (int i=0; i < childcount; i++){
					      TableLayout v = (TableLayout) ll.getChildAt(i);
					      int childs = v.getChildCount();
					      for (int j=0; j < childs; j++){
						      View vw = v.getChildAt(j);
						      vw.setClickable(true);
						}
					}
					
					int childc = tl.getChildCount();
					for (int i=0; i < childc; i++){
					      View v = tl.getChildAt(i);
					      v.setClickable(true);
					}
			    }

			});
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
			
			pushrightout.setAnimationListener(new Animation.AnimationListener(){
				ImageView tv = (ImageView) findViewById(R.id.optionsimg);
				TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
				TableLayout tl = (TableLayout) findViewById(R.id.settings);
			    public void onAnimationStart(Animation a){}
			    public void onAnimationRepeat(Animation a){}
			    public void onAnimationEnd(Animation a){
			    	tv.setClickable(true);
			    	int childcount = ll.getChildCount();
					for (int i=0; i < childcount; i++){
					      TableLayout v = (TableLayout) ll.getChildAt(i);
					      int childs = v.getChildCount();
					      for (int j=0; j < childs; j++){
						      View vw = v.getChildAt(j);
						      vw.setClickable(true);
						}
					}
					
					int childc = tl.getChildCount();
					for (int i=0; i < childc; i++){
					      View v = tl.getChildAt(i);
					      v.setClickable(true);
					}
			    }

			});
			
			ll.setVisibility(0);
			ll.startAnimation(pushrightout);

			Animation pushrightin = AnimationUtils.loadAnimation(this,
					R.anim.push_left_out);
			tl.startAnimation(pushrightin);
			
			tl.setVisibility(8);
			options = false;
		}

	}

	public void logout(View view) {
		File tkfile = PathManager.getAbsoluteFilePath(getApplicationContext(),
				PathManager.FILE_TOKEN);
		if (tkfile.delete()) {
			Intent intent = new Intent(getApplication(), Hello_activity.class);
			startActivity(intent);
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

		ip_request = serverip;

		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		try {
			start();
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
				else if(type.equals(WebSocket.WSS_RESPONSE_ACTIVE)){
					Log.d("CUBE", e.getMessage().toString());
					JSONObject object = e.getMessage();
					JSONArray jarr = new JSONArray();
					try {
						jarr = object.getJSONArray("data");
					} catch (JSONException e1) {
						Log.d("CUBE", "JSON EXCEPTION 1");
					}
					
					for(int i = 0; i<jarr.length(); i++){	
						try {
							final JSONObject obj = jarr.getJSONObject(i); //TODO checkifAlreadyinthere(JSONArray jarr, JSONObject obj)
							if(obj.getInt("configured") > 0){
								
								new Thread(new Runnable(){

									public void run() {
										try {
											final JSONObject location = (JSONObject) API
													.getApiInstance()
													.getApiRequest(
															API.API_LOCATION,
															obj.getString("location"));
											
											
											final JSONObject plugin = (JSONObject) API
													.getApiInstance()
													.getApiRequest(
															API.API_PLUGIN,
															obj.getString("plugin"));
											
											if(plugin.getString("objects").equals("motor") || plugin.getString("objects").equals("slider") || plugin.getString("objects").equals("led") || plugin.getString("objects").equals("number") || plugin.getString("objects").equals("toggle")){
												runOnUiThread(new Runnable() {
													 public void run() {
														 addDevice(obj, location, plugin);
													 }
												});
											}											
										} 
											catch (Exception e) {
											e.printStackTrace();
										}
										
										}}).start();
								
								
							}
							
						} catch (JSONException e1) {
							Log.d("CUBE", "JSON EXCEPTION 2");
						}
						
						
						
					}
					
				}
				else if (type.equals(WebSocket.WSS_RESPONSE_NEW)){
					//TODO
				}
				else if(type.equals(WebSocket.WSS_RESPONSE_REMOVE)){
					//TODO
				}
				else if(type.equals(WebSocket.WSS_RESPONSE_UPDATE)){}
					//TODO
				else if(type.equals(WebSocket.WSS_RESPONSE_ERROR_API)){
					//TODO
				}
				else if(type.equals(WebSocket.WSS_RESPONSE_TOKEN_REFUSE)){
					File tkfile = PathManager.getAbsoluteFilePath(getApplicationContext(),
							PathManager.FILE_TOKEN);
					if (tkfile.delete()) {
						final AlertDialog.Builder alert_lof = new AlertDialog.Builder(getApplicationContext())
						.setTitle("Session Expired")
						.setMessage(
								"The Server-User-Session Expired. Try to logging in again");

						alert_lof.show();
						
						finish();
					} else {
						final AlertDialog.Builder alert_lof = new AlertDialog.Builder(getApplicationContext())
								.setTitle("Logout failed")
								.setMessage(
										"A critical Error occured, try to restart your app");

						alert_lof.show();

					}
				}
				
				
			}
		});

		ws.connect(ip_request, 8999);

	}

	public void deleteDevice(int id, int locid) { //TODO Check, change Arguments
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

	void addDevice(JSONObject dev, JSONObject loc, JSONObject plugin){
		final JSONObject device = dev;
		TableLayout ll = (TableLayout) findViewById(R.id.lout_main_table);
		TableLayout tl;
		TableRow tr3 = new TableRow(this);
		int width = 0;
		int height = 0;
		LinearLayout tr_h = new LinearLayout(this);
		
		String bez = null;
		int locid = 0;
		int id = 0;
		String location = null;

		try {
			bez = dev.getString("name");
			locid = loc.getInt("id");
			id = dev.getInt("id");
			location = loc.getString("name");
		} catch (JSONException e) {
			Log.d("DEV", "Exception 1");
		}
		final int idcpy = id;
		final int locidcpy = locid;
		
		try {
			if(plugin.getString("objects").equals("toggle") || plugin.getString("objects").equals("motor")){
				
				Button onButton = new Button(this);
				
				if(plugin.getString("objects").equals("toggle"))
				onButton.setText("ON");
				else
				onButton.setText("+");
				
				onButton.setBackgroundColor(Color.parseColor("#F52200"));

				onButton.setOnClickListener(new OnClickListener() {

					String idstr = "" + idcpy;

					public void onClick(View v) {

						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("id", idstr);
						data.put("value", "1");

						ws.send(WebSocket.WSS_EXECUTE_CLIENT_COMMAND, data);

					}
				});
				
				
				Button offButton = new Button(this);
				
				if(plugin.getString("objects").equals("toggle"))
				offButton.setText("OFF");
				else
				offButton.setText("-");
				
				offButton.setBackgroundColor(Color.LTGRAY);
				offButton.setOnClickListener(new OnClickListener() {

					String idstr = "" + idcpy;

					public void onClick(View v) {

						HashMap<String, Object> data = new HashMap<String, Object>();
						data.put("id", idstr);
						data.put("value", "0");

						ws.send(WebSocket.WSS_EXECUTE_CLIENT_COMMAND, data);

					}
				});
				
				tr_h.setMinimumWidth(LayoutParams.WRAP_CONTENT);
				tr_h.setMinimumHeight(LayoutParams.WRAP_CONTENT);
				tr_h.addView(onButton);
				tr_h.addView(offButton);
				
			}
			else if(plugin.getString("objects").equals("number")){
				TextView tv = new TextView(this);
				tv.setText(dev.getInt("value") + "�");
				tv.setId(idcpy + 17000);
				tv.setTextSize(60);
				
				height = 50;
				
				tr_h.setMinimumWidth(LayoutParams.WRAP_CONTENT);
				tr_h.setMinimumHeight(LayoutParams.WRAP_CONTENT);
				tr_h.addView(tv);
				
			}
			else if(plugin.getString("objects").equals("slider")){
				SeekBar sb = new SeekBar(this);
				sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					HashMap<String, Object> data = new HashMap<String, Object>();
					public void onStopTrackingTouch(SeekBar arg0) {
						ws.send(WebSocket.WSS_EXECUTE_CLIENT_COMMAND, data);
					}
					
					public void onStartTrackingTouch(SeekBar arg0) {
						//DoNothing
					}
					
					public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
						String idstr = "" + idcpy;
						
						data.put("id", idstr);
						data.put("value", "" + arg1);

						
					}
				});
				LayoutParams lp = new LayoutParams(ll.getWidth()/2, LayoutParams.WRAP_CONTENT);
			    sb.setLayoutParams(lp);
				tr_h.setMinimumWidth(LayoutParams.WRAP_CONTENT);
				tr_h.setMinimumHeight(LayoutParams.WRAP_CONTENT);
				tr_h.addView(sb);
			}
			else if(plugin.getString("objects").equals("led")){
				ImageView img = new ImageView(this);
				if(dev.getString("value").equals("" + 0)){
				img.setBackgroundResource(R.drawable.kreis_schwarz);
				}else{img.setBackgroundResource(R.drawable.kreis_orange);}
				img.setId(idcpy + 18000);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						130, 130);
				img.setLayoutParams(layoutParams);
				
				tr_h.setMinimumWidth(LayoutParams.WRAP_CONTENT);
				tr_h.setMinimumHeight(LayoutParams.WRAP_CONTENT);
				tr_h.addView(img);
				
				
			};
		} catch (JSONException e) {
			Log.d("DEV", "Exception 2");
		}
		TextView name = new TextView(this);
		name.setText(bez);
		
		TextView idtxt = new TextView(this);
		idtxt.setText("ID: " + id);
		idtxt.setGravity(Gravity.RIGHT);
		name.setGravity(Gravity.LEFT);
		
		
		LinearLayout lin_desc = new LinearLayout(this);
		
		lin_desc.addView(name);
		lin_desc.addView(idtxt);
		
		TableRow tr = new TableRow(this);
		tr.setBackgroundColor(Color.argb(150, 220, 220, 220));
		tr.setMinimumWidth(LayoutParams.MATCH_PARENT);
		tr.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr.setId(id + 9000);
		tr.setClickable(true);
		tr.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				TableLayout tablayout = (TableLayout) findViewById(R.id.lout_main_table);
				RelativeLayout rl = (RelativeLayout) findViewById(R.id.lout_main);
				ImageView tv = (ImageView) findViewById(R.id.optionsimg);
				

				TextView text = new TextView(getApplication());
				
				
				tv.setClickable(false);
				
				int childc = tablayout.getChildCount();
				for (int i=0; i < childc; i++){
				      TableLayout view = (TableLayout) tablayout.getChildAt(i);
				      int childs = view.getChildCount();
				      for (int j=0; j < childs; j++){
					      View vw = view.getChildAt(j);
					      vw.setClickable(false);
					}
				}
				int id = 0;
				try {
					id = device.getInt("id");
					text.setText("ID: " + "" + id  + "\nName: " + device.getString("name") + "\nPlugin: " + device.getString("plugin") + "\n\n\nDescription: " + device.getString("description"));
				} catch (JSONException e) {
					Log.d("DEV", "Exception 4");
				}
			
				text.setTextSize(16);
				text.setTextColor(Color.BLACK);
				text.setId(id + 50000);
				final int idcpy = id;
				
				
				int height = (tablayout.getWidth()/2);
				text.setPadding(0, height, 0, height);
				
				
				Animation pushrightout = AnimationUtils.loadAnimation(getApplicationContext(),
						R.anim.push_right_in_true);
				pushrightout.setAnimationListener(new Animation.AnimationListener(){
					
				    public void onAnimationStart(Animation a){}
				    public void onAnimationRepeat(Animation a){}
				    public void onAnimationEnd(Animation a){
				    	TableLayout tablayout = (TableLayout) findViewById(R.id.lout_main_table);
				    	int childc = tablayout.getChildCount();
						for (int i=0; i < childc; i++){
						      TableLayout view = (TableLayout) tablayout.getChildAt(i);
						      int childs = view.getChildCount();
						      for (int j=0; j < childs; j++){
							      View vw = view.getChildAt(j);
							      vw.setClickable(true);
							}
						}
				    	
				    }

				});
				
				
				
				text.startAnimation(pushrightout);

				Animation pushrightin = AnimationUtils.loadAnimation(getApplicationContext(),
						R.anim.push_right_out);
				
				tablayout.setVisibility(8);
				tablayout.startAnimation(pushrightin);
				
				
				text.setVisibility(0);
				rl.addView(text);
				
				RelativeLayout.LayoutParams layoutParams = 
					    (RelativeLayout.LayoutParams)text.getLayoutParams();
					layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		
				text.setLayoutParams(layoutParams);
				text.setGravity(Gravity.CENTER);
				
				text.setOnClickListener(new OnClickListener(){

					public void onClick(View arg0) {
						TableLayout tablayout = (TableLayout) findViewById(R.id.lout_main_table);
						RelativeLayout rl = (RelativeLayout) findViewById(R.id.lout_main);
						TextView text = (TextView) findViewById(idcpy + 50000);
						
						
						int childc = tablayout.getChildCount();
						for (int i=0; i < childc; i++){
						      TableLayout view = (TableLayout) tablayout.getChildAt(i);
						      int childs = view.getChildCount();
						      for (int j=0; j < childs; j++){
							      View vw = view.getChildAt(j);
							      vw.setClickable(false);
							}
						}
						
						
						
						Animation pushrightout = AnimationUtils.loadAnimation(getApplicationContext(),
								R.anim.push_left_in);
						pushrightout.setAnimationListener(new Animation.AnimationListener(){
						
							ImageView tv = (ImageView) findViewById(R.id.optionsimg);
						    public void onAnimationStart(Animation a){}
						    public void onAnimationRepeat(Animation a){}
						    public void onAnimationEnd(Animation a){
						    	TableLayout tablayout = (TableLayout) findViewById(R.id.lout_main_table);
						    	
						    	int childc = tablayout.getChildCount();
								for (int i=0; i < childc; i++){
								      TableLayout view = (TableLayout) tablayout.getChildAt(i);
								      int childs = view.getChildCount();
								      for (int j=0; j < childs; j++){
									      View vw = view.getChildAt(j);
									      vw.setClickable(true);
									}
								}
						    	tv.setClickable(true);
						    }

						});
						tablayout.setVisibility(0);
						tablayout.startAnimation(pushrightout);

						Animation pushrightin = AnimationUtils.loadAnimation(getApplicationContext(),
								R.anim.push_left_out_true);
						text.startAnimation(pushrightin);
						text.setVisibility(8);
						
						rl.removeView(text);
					}});
				
			}});
		
		if (findViewById(locid + 15000) == null) {
			tl = new TableLayout(this);
			tl.setId(locid + 15000);
			tl.setPadding(ll.getWidth()/8, 0, ll.getWidth()/8, 0);

			
			tr3.setMinimumWidth(LayoutParams.WRAP_CONTENT);
			tr3.setMinimumHeight(LayoutParams.WRAP_CONTENT);
			
			
			TextView locname = new TextView(this);
			locname.setText(location);
			locname.setTextSize(18);

			TableLayout tab = (TableLayout) findViewById(R.id.settings);
			TextView locnav = new TextView(this);
			locnav.setText("\t- " + location);
			locnav.setTextSize(17);
			TableRow tabr = new TableRow(this);
			tabr.setPadding(0, 15, 0, 15);

			
			tabr.addView(locnav);
			Animation pushdownin = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
			tabr.startAnimation(pushdownin);
			tab.addView(tabr);

			tabr.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					TableLayout tab1 =  (TableLayout) findViewById(R.id.lout_main_table);
					TableLayout tab2 =  (TableLayout) findViewById(R.id.settings);
					ImageView tv = (ImageView) findViewById(R.id.optionsimg);
					tv.setClickable(false);
					 
					int childcount = tab2.getChildCount();
					for (int i=0; i < childcount; i++){
					      View view = tab2.getChildAt(i);
					      view.setClickable(false);
					}
					
					int childc = tab1.getChildCount();
					for (int i=0; i < childc; i++){
					      TableLayout view = (TableLayout) tab1.getChildAt(i);
					      int childs = view.getChildCount();
					      for (int j=0; j < childs; j++){
						      View vw = view.getChildAt(j);
						      vw.setClickable(false);
						}
					}
					
					Animation pushrightout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_in);
					pushrightout.setAnimationListener(new Animation.AnimationListener(){
						ImageView tv = (ImageView) findViewById(R.id.optionsimg);
						TableLayout tab2 =  (TableLayout) findViewById(R.id.settings);
						TableLayout tab1 =  (TableLayout) findViewById(R.id.lout_main_table);
					    public void onAnimationStart(Animation a){}
					    public void onAnimationRepeat(Animation a){}
					    public void onAnimationEnd(Animation a){
					    	tv.setClickable(true);
					    	int childcount = tab2.getChildCount();
							for (int i=0; i < childcount; i++){
							      View view = tab2.getChildAt(i);
							      view.setClickable(true);
							}
							
							int childc = tab1.getChildCount();
							for (int i=0; i < childc; i++){
							      TableLayout view = (TableLayout) tab1.getChildAt(i);
							      int childs = view.getChildCount();
							      for (int j=0; j < childs; j++){
								      View vw = view.getChildAt(j);
								      vw.setClickable(true);
								}
							}
					    }

					});
					tab1.setVisibility(0);
					tab1.startAnimation(pushrightout);
					
					
					Animation pushrightin = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.push_left_out);
					tab2.startAnimation(pushrightin);
					tab2.setVisibility(8);
					options = false;

					
					final Handler handler = new Handler();
				    handler.postDelayed(new Runnable() {
				    	public void run() {
			            	ScrollView sv = (ScrollView) findViewById(R.id.scrollView1);
							TableLayout tl = (TableLayout) findViewById(locidcpy + 15000);
							int y = (int) tl.getY();
							if(y >= 30){
								sv.scrollTo(0, y+100);
							}
							else{
								sv.scrollTo(0, y);
							}
			            }
			        }, 100);
				}
			});
			
			tr3.addView(locname);
			
			locname.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			
			int w = ll.getWidth()/2 - ll.getWidth()/8 - locname.getMeasuredWidth()/2;
			
			if(ll.getChildCount() == 0){
			tr3.setPadding(w, 5, 0, 40);
			ProgressBar bar = (ProgressBar) findViewById(R.id.yologressbar);
			bar.setVisibility(8);}
			else
			tr3.setPadding(w, 100, 0, 40);
			
			tl.addView(tr3);
			
			
			ll.addView(tl);
		} else {
			tl = (TableLayout) findViewById(locid + 15000);

		}

		TableRow tr2 = new TableRow(this);
		tr2.setMinimumWidth(LayoutParams.MATCH_PARENT);
		tr2.setMinimumHeight(LayoutParams.WRAP_CONTENT);
		tr2.setPadding(0, 40, 0, 0);
		tr2.setId(id + 12000);

		
		tr2.addView(lin_desc);

		
		
		Animation pushdownin = AnimationUtils.loadAnimation(this,
				R.anim.push_left_in);
		tr.startAnimation(pushdownin);
		tr2.startAnimation(pushdownin);
		
		tr.addView(tr_h);
		tl.addView(tr2);
		tl.addView(tr);

		tr_h.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		tr2.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		
		
		width = (ll.getWidth()/2) - (tr_h.getMeasuredWidth()/2) - (ll.getWidth()/8);
		height = (int) (ll.getWidth()/2.4) - (tr_h.getMeasuredHeight());
		tr.setPadding(width, height, 0, height);
		
		int idw = ll.getWidth() - ll.getWidth()/4 - tr2.getMeasuredWidth();
		idtxt.setPadding(idw, 0, 0, 0);
	
	}

	void changeTempValue(int id, String value) {

		TextView temp = (TextView) findViewById(id + 17000);
		temp.setText(value);
		temp.setTextSize(50);

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
