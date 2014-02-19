package com.example.cubemanagehome.utility;

import java.io.File;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

public class XML {
	public static void Write(Object o, File f) throws Exception {
		if (!f.exists())
			f.createNewFile();
		Serializer s = new Persister();
		s.write(o, f);
	}

	public static Object Read(Class<?> cl, File f) throws Exception {
		Serializer s = new Persister();
		return s.read(cl, f);
	}

}
