package com.example.test;

import android.os.Bundle;
import android.view.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}
	
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//	    // Handle presses on the action bar items
//	    switch (item.getItemId()) {
//	        case R.id.submit:
//	            if(checkUNPW()){
//	            	
//	            	setContentView(R.layout.activity_testaferlogin);
//	            	
//	           };
//	            return true;
//	        default:
//	            return super.onOptionsItemSelected(item);
//	    }
//	}
	
	public void submitclick(View view){
		
//		EditText pw = new EditText(getBaseContext());
//		pw.setText("asdf");
//		EditText un = new EditText(getBaseContext());
//		un.setText("admin");
		
		EditText etupw = (EditText) findViewById(R.id.Password);
		EditText etuun = (EditText) findViewById(R.id.Username);
		
		String upw = etupw.getText().toString();
		String uun = etuun.getText().toString();
		
		String un = "admin";
		String pw = "pw";
		
		if(pw.equals(upw) && un.equals(uun)){
			Intent intent = new Intent(this, Testaferlogin.class);
			startActivity(intent);
		}
	
		
	}
	

		


}
