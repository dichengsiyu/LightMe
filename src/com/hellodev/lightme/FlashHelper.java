package com.hellodev.lightme;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FlashHelper {
	static {
		System.loadLibrary("hellodev");
	}
	
	protected FlashHelper() {
	}
	
	protected native void changeFlashLight(boolean increasing);
	
	public String getCurrentFlashLevel() {
		ProcessBuilder cmd;
		String result = "";
		try {
			String[] args = { "/system/bin/cat",
					"/sys/class/leds/torch_led/brightness" };
			cmd = new ProcessBuilder(args);

			Process process = cmd.start();
			InputStream in = process.getInputStream();
			DataInputStream di = new DataInputStream(in);
			result += di.read();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
