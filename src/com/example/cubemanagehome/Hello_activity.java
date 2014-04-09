package com.example.cubemanagehome;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;











import com.example.cubemanagehome.utility.API;
import com.example.cubemanagehome.utility.Credentials;
import com.example.cubemanagehome.utility.PathManager;
import com.example.cubemanagehome.utility.Token;
import com.example.cubemanagehome.utility.User;
import com.example.cubemanagehome.utility.XML;

import android.os.Bundle;
import android.os.Handler;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;

public class Hello_activity extends Activity {

	String ip_request = "192.168.1.1";
	API api = API.getApiInstance();

	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_hello_activity);
		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		final AlertDialog.Builder alert_sessionexpired = new AlertDialog.Builder(this)
		.setTitle("Session expired")
		.setMessage(
				"The User-Server Session expired, try to log in again");
		
		api.setContext(getApplicationContext());
		String serverip = new String();
		

		
		this.onStart();
		super.onCreate(savedInstanceState);

		File mPath = PathManager.getAbsoluteFilePath(getApplicationContext(),
				PathManager.FILE_SERVERS);
		File tkPath = PathManager.getAbsoluteFilePath(getApplicationContext(),
				PathManager.FILE_TOKEN);

		if (!mPath.exists()) {

			setContentView(R.layout.initial_configuration_dialog);

		} else {

			FileInputStream fIn = null;
			try {
				fIn = new FileInputStream(mPath);
			} catch (FileNotFoundException e1) {
				Log.d("HELLOACT", "Exception 1");
			}
			BufferedReader myReader = new BufferedReader(new InputStreamReader(
					fIn));
			try {
				myReader.readLine();
				serverip = myReader.readLine();
				api.bind(serverip);
				myReader.close();
			} catch (IOException e1) {
				Log.d("HELLOACT", "Exception 2");
			}

			if (!tkPath.exists()) {
				setContentView(R.layout.activity_login);
			} else {
				try {
					Token tok = (Token) XML.Read(Token.class, tkPath);
					Log.d("API", tok.getToken());
					api.login(tok.getToken(), new API.APIAuthCallback() {	
						public void onSuccess(User u, Token t) {
							runOnUiThread(new Runnable() {
								public void run() {
									Intent intent = new Intent(getApplication(), Main.class);
									startActivity(intent);
									finish();
								}
							});
						}
						public void onError(String error) {
							runOnUiThread(new Runnable() {
								public void run() {
									alert_sessionexpired.show();
									setContentView(R.layout.activity_login);
								}
							});	
						}
					});
				} catch (Exception e) {
					Log.d("MAINACT", "EJREJASDOJAS");
				}
				

			}
		}
	}

	public void finish(View view) throws FileNotFoundException {

		EditText servername = (EditText) findViewById(R.id.servername);
		EditText serveradresse = (EditText) findViewById(R.id.serveradresse);

		final String str_servername = servername.getText().toString();
		final String str_serveradresse = serveradresse.getText().toString();

		ip_request = str_serveradresse;

		final AlertDialog.Builder alert_online = new AlertDialog.Builder(this)
				.setTitle("Success!")
				.setMessage(
						"The device managed to get a connection to the Server, please log in now.");

		final AlertDialog.Builder alert_offline = new AlertDialog.Builder(this)
				.setTitle("No server available!")
				.setMessage(
						"The device was not able to connect to the Server! Try to reenter it.");

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (API.isValidServer(ip_request)) {
					api.bind(ip_request);
					
					try {

						File myFile = PathManager.getAbsoluteFilePath(getApplicationContext(),
								PathManager.FILE_SERVERS);

						myFile.createNewFile();

						FileOutputStream fOut = new FileOutputStream(myFile);
						OutputStreamWriter myOutWriter = new OutputStreamWriter(
								fOut);
						myOutWriter.append(str_servername + "\n");
						myOutWriter.append(str_serveradresse + "\n");
						myOutWriter.close();
						fOut.close();

						runOnUiThread(new Runnable() {
							public void run() {
								alert_online.show();

								RelativeLayout rl = (RelativeLayout) findViewById(R.id.firstsett_lout);
								Animation pushrightout = AnimationUtils.loadAnimation(getApplicationContext(),
										R.anim.push_left_out_true);
								int childc = rl.getChildCount();
								for (int i=0; i < childc; i++){
								      View v = rl.getChildAt(i);
								      v.startAnimation(pushrightout);
								}
								
								final Handler handler = new Handler();
								handler.postDelayed(new Runnable() {
									
									public void run() {

										setContentView(R.layout.activity_login);
									}
								}, 500);
							}
						});

					} catch (Exception e) {
						Log.d("HELLOACT", "NIX GEHEN");
					}	
					
					
				} else {
					runOnUiThread(new Runnable() {
						public void run() {
							alert_offline.show();
						}
					});
				}
			}
		}).start();


	}

	public void weiter(View view) {
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.init_conf);
		Animation pushrightout = AnimationUtils.loadAnimation(this,
				R.anim.push_right_out);	
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			public void run() {

				setContentView(R.layout.activity_first_settings);
			}
		}, 500);
		int childc = rl.getChildCount();
		for (int i=0; i < childc; i++){
		      View v = rl.getChildAt(i);
		      v.startAnimation(pushrightout);
		}
		
		
	}

	public void submitclick(View view) {

		EditText etupw = (EditText) findViewById(R.id.Password);
		EditText etuun = (EditText) findViewById(R.id.Username);

		String upw = etupw.getText().toString();
		String uun = etuun.getText().toString();

		final AlertDialog.Builder alert_wronglogin = new AlertDialog.Builder(
				this)
				.setTitle("Wrong username or password")
				.setMessage(
						"The username password combination was not found on the server! Try to retype it.");

		etupw.setText("");
		api.asyncAuthenticate(new Credentials(uun, upw),
				new API.APIAuthCallback() {

					public void onError(String e) {
						runOnUiThread(new Runnable() {

							public void run() {
								alert_wronglogin.show();
							}
						});
					}

					public void onSuccess(User u, Token t) {
						try {
							File myFile = PathManager.getAbsoluteFilePath(getApplicationContext(),
									PathManager.FILE_TOKEN);
							XML.Write(t, myFile);
							Intent intent = new Intent(getApplication(),
									Main.class);
							startActivity(intent);
							finish();
						} catch (Exception e) {
							Log.d("HELLOACT", "XML WRITE ERR");
						}
					}

				});

	}

	public void changeip(View view) {
		RelativeLayout rl = (RelativeLayout) findViewById(R.id.log_lout);
		Animation pushrightout = AnimationUtils.loadAnimation(this,
				R.anim.push_right_out);

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			public void run() {

				setContentView(R.layout.activity_first_settings);
			}
		}, 500);
		
		int childc = rl.getChildCount();
		for (int i=0; i < childc; i++){
		      View v = rl.getChildAt(i);
		      v.startAnimation(pushrightout);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hello_activity, menu);
		return true;
	}

}
