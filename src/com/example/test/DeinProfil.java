package com.example.test;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class DeinProfil extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dein_profil);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dein_profil, menu);
		return true;
	}

}
