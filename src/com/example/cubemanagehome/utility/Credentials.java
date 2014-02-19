package com.example.cubemanagehome.utility;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "Credentials")
public class Credentials {
	@Element(name = "Username")
	private String username;

	@Element(name = "Password")
	private String password;
	
	public Credentials(@Element(name = "Username") String username,
			@Element(name = "Password") String password) {
		this.username = username;
		this.password = password;
	}

	public String setUsername(String username) {
		return this.username = username;
	}

	public String setPassword(String password) {
		return this.password = password;
	}

	public String getUsername() {
		return this.username;
	}

	public String getPassword() {
		return this.password;
	}

	@Override
	public String toString() {
		return "Credentials [username=" + username + ", password=" + password
				+ "]";
	}
}
