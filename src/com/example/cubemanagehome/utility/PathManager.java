package com.example.cubemanagehome.utility;

import java.io.File;

import android.content.Context;

public class PathManager {
	public static final String FILE_SERVERS = "servers.txt";
	public static final String FILE_CONFIG = "config.xml";
	public static final String FILE_TOKEN = "token.xml";
	public static final String FILE_UUID = "uuid.xml";

	public static File getAbsoluteFilePath(Context c, String filename) {
		return new File(c.getFilesDir().getPath(), filename);
	}
}
