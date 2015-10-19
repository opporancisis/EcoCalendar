package utils;

import java.io.File;

public class Config {

	public static File getConfig(String name) {
		return new File(System.getProperty("user.home") + "/.ecoportal/" + name);
	}

	public static File getMainLocalConfig() {
		return getConfig("ecocalendar.conf");
	}

}
