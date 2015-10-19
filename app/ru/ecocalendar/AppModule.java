package ru.ecocalendar;

import javax.inject.Singleton;

import play.Logger;
import security.MyHandlerCache;

import com.google.inject.AbstractModule;
import be.objectify.deadbolt.java.cache.HandlerCache;

public class AppModule extends AbstractModule {

	@Override
	protected void configure() {
		Logger.info("Binding application start");
		bind(ApplicationStart.class).asEagerSingleton();
		bind(HandlerCache.class).to(MyHandlerCache.class).in(Singleton.class);
	}

}
