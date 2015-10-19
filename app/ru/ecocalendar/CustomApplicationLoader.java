package ru.ecocalendar;

import java.io.File;

import play.ApplicationLoader;
import play.Configuration;
import play.inject.guice.GuiceApplicationBuilder;
import play.inject.guice.GuiceApplicationLoader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class CustomApplicationLoader extends GuiceApplicationLoader {

	@Override
	public GuiceApplicationBuilder builder(ApplicationLoader.Context context) {
		File localConf = utils.Config.getMainLocalConfig();
		if (!localConf.exists()) {
			return super.builder(context);
		}
		Config testConfig = ConfigFactory.parseFile(localConf);
		testConfig.resolve();
		Configuration finalConf = new Configuration(testConfig).withFallback(context
				.initialConfiguration());
		return initialBuilder.in(context.environment()).loadConfig(finalConf)
				.overrides(overrides(context));
	}

}
