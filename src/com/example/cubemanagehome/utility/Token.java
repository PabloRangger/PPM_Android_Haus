package com.example.cubemanagehome.utility;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name = "Token")
public class Token {
	@Element(name = "AuthToken")
	private String t;

	public Token(@Element(name = "AuthToken") String t) {
		this.t = t;
	}
	
	public String getToken(){
		return this.t;
	}
	
	public String toString(){
		return this.t;
	}
}
