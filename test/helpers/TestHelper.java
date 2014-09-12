package helpers;

import java.io.File;

import org.openqa.selenium.WebDriver;

import play.Logger;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class TestHelper {

	private static Config config = loadConfig().getConfig("test");

	private static Config loadConfig() {
		File file = utils.Config.getMainLocalConfig();
		if (!file.exists()) {
			Logger.error("Configuration not found");
		}
		return ConfigFactory.parseFile(file);
	}

	public static Config getConfig() {
		return config;
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends WebDriver> loadWebDriver(String className) {
		try {
			return (Class<? extends WebDriver>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			Logger.error("Could not find webdriver class", e);
			return null;
		}
	}

}
