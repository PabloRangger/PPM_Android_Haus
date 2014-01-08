package com.example.cubemanagehome;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class FirstSettings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first_settings);
	}

	public void finish(View view) throws FileNotFoundException
	{
		
		EditText servername = (EditText) findViewById(R.id.servername);
		EditText serveradresse = (EditText) findViewById(R.id.serveradresse);
		
		String str_servername = servername.getText().toString();
		String str_serveradresse = serveradresse.getText().toString();

		Log.d("HELLOACT", "P1");
		try {
			Log.d("HELLOACT", "P2");
			File myFile = new File("/storage/emulated/0/cube/cube.txt");
			
			myFile.createNewFile();		//TODO: Ordner erstellen
			Log.d("HELLOACT", "P3");
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = 
									new OutputStreamWriter(fOut);
			myOutWriter.append(str_serveradresse + "\n");
			myOutWriter.append(str_servername + "\n");
			myOutWriter.close();
			fOut.close();
		
			finish();
			
		} catch (Exception e) {
			Log.d("HELLOACT", "NIX GEHEN");
		}

	}	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.first_settings, menu);
		return true;
	}

}
