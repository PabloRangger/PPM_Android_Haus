package com.example.cubemanagehome.utility;

import org.json.JSONObject;
import org.simpleframework.xml.Root;

@Root(name = "User")
public class User {
	private String username;
	private String email;
	private String firstname;
	private String lastname;
	private int userlevel;

	public User(JSONObject userData) throws Exception {
		this.username = userData.getString("username");
		this.email = userData.getString("email");
		this.firstname = userData.getString("firstname");
		this.lastname = userData.getString("lastname");
		this.userlevel = Integer.parseInt(userData.getString("userlevel"));
	}

	public User(String username, String email, String firstname,
			String lastname, int userlevel) {
		super();
		this.username = username;
		this.email = email;
		this.firstname = firstname;
		this.lastname = lastname;
		this.userlevel = userlevel;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public int getUserlevel() {
		return userlevel;
	}
}
