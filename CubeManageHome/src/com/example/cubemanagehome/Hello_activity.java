package com.example.cubemanagehome;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.OutputStreamWriter;



import org.json.JSONObject;

import com.example.cubemanagehome.utility.API;
import com.example.cubemanagehome.utility.Utility;
import com.example.cubemanagehome.utility.Utility.URIType;

import android.os.Bundle;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class Hello_activity extends Activity {

	boolean online = false;
	String ip_request = "192.168.1.1";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		this.onStart();
		super.onCreate(savedInstanceState);
		
		

		File mPath = new File(("/storage/emulated/0/cube/cube.txt"));

		if (!mPath.exists()) {
			setContentView(R.layout.initial_configuration_dialog);
		} else {
			setContentView(R.layout.activity_login);
		}
	}

	public void finish(View view) throws FileNotFoundException {

		EditText servername = (EditText) findViewById(R.id.servername);
		EditText serveradresse = (EditText) findViewById(R.id.serveradresse);

		String str_servername = servername.getText().toString();
		String str_serveradresse = serveradresse.getText().toString();

		ip_request = str_serveradresse;

		

		final AlertDialog.Builder alert_offline = new AlertDialog.Builder(this)
				.setTitle("No server available!")
				.setMessage(
						"The device was not able to connect to the Server! Try to reenter it.");

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					JSONObject response = (JSONObject) API.Request(
							API.API_STATUS,
							Utility.getURI(URIType.URI_HTTP, ip_request));
					
//					String status = response.getString("status");
					
//					if(status.equals("online")){
//						
//						
//						
//					}
					
				} catch (Exception e){
					runOnUiThread(new Runnable() {
						public void run() {
							alert_offline.show();
						}
					});
				}
			}
		}).start();

		
		try {

			File myFile = new File("/storage/emulated/0/cube/cube.txt");

			myFile.createNewFile(); // TODO: Ordner erstellen

			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(str_servername + "\n");
			myOutWriter.append(str_serveradresse + "\n");
			myOutWriter.close();
			fOut.close();
				
			setContentView(R.layout.activity_login);
				
				
		} catch (Exception e) {
				Log.d("HELLOACT", "NIX GEHEN");
			}
		}
	

	public void weiter(View view) {
		setContentView(R.layout.activity_first_settings);
	}

	public void submitclick(View view) {
		EditText etupw = (EditText) findViewById(R.id.Password);
		EditText etuun = (EditText) findViewById(R.id.Username);

		String upw = etupw.getText().toString();
		String uun = etuun.getText().toString();

		String un = "admin";
		String pw = "pw";

		if (pw.equals(upw) && un.equals(uun)) {
			Intent intent = new Intent(this, Main.class);
			startActivity(intent);
			overridePendingTransition(R.anim.push_down_in,R.anim.push_down_out);
		}

	}

	public void changeip(View view) {
		setContentView(R.layout.activity_first_settings);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hello_activity, menu);
		return true;
	}

}
