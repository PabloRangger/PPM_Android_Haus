package com.example.cubemanagehome;

import com.example.cubemanagehome.utility.API;
import com.example.cubemanagehome.utility.API.APIUserNotSetException;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class EditUser extends Activity {
	EditText Username;
	EditText Firstname;
	EditText Lastname;
	
	API api = API.getApiInstance();
	
	
	public void Save(View view){
		if(Username.getText()!=null&&Firstname.getText()!=null&&Lastname.getText()!=null){
			
		}
		
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_user);
		
		Username=(EditText) findViewById(R.id.Username);
		Firstname=(EditText) findViewById(R.id.Firstname);
		Lastname=(EditText) findViewById(R.id.Lastname);
		
		try {
			Username.setText(api.getUser().getUsername());
			Firstname.setText(api.getUser().getFirstname());
			Lastname.setText(api.getUser().getLastname());
		} catch (APIUserNotSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_user, menu);
		return true;
	}

}
