package com.example.cubemanagehome;


import java.io.InputStream;
import java.net.URL;

import org.json.JSONObject;

import com.example.cubemanagehome.utility.API;
import com.example.cubemanagehome.utility.API.APIErrorCallback;
import com.example.cubemanagehome.utility.API.APIImageCallback;
import com.example.cubemanagehome.utility.API.APIUserNotSetException;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;



public class YourProfile extends Activity {
	static Bitmap mIcon_val;
	ImageView yimg;
	ImageView edit;
	TextView benutzername;
	TextView FirstLastName;
	TextView Email;
	
	API api = API.getApiInstance();
	
	
	public void Edit(View view){
		Intent intent = new Intent(getApplication(), EditUser.class);
		startActivity(intent);
	}

	
	protected void onCreate(Bundle savedInstanceState) {
		ActionBar actionBar = this.getActionBar();
		actionBar.hide();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_your_profile);
		
		yimg = (ImageView) findViewById(R.id.yprof_img);
		benutzername=(TextView) findViewById(R.id.Benutzername);
		FirstLastName=(TextView) findViewById(R.id.FirstLastName);
		Email=(TextView) findViewById(R.id.Email);
		
		try {
			String flname1=api.getUser().getFirstname()+" "+api.getUser().getLastname();
			benutzername.setText(api.getUser().getUsername());
			FirstLastName.setText(flname1);
			Email.setText(api.getUser().getEmail());
		} catch (APIUserNotSetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		api.asyncImageRequest(API.API_USER_AVATAR, "cube", new APIImageCallback() {
			public void onResult(final Bitmap b) {
				runOnUiThread(new Runnable() {
					 public void run() {
						 yimg.setImageBitmap(b);
						 
					 }
				});
			}
		}, null);
		
		
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.your_profile, menu);
		return true;
	}
	
	
}
