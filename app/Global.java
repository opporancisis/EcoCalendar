import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Date;

import play.Application;
import play.Configuration;
import play.GlobalSettings;
import play.Logger;
import play.Logger.ALogger;
import play.api.mvc.EssentialFilter;
import play.data.format.Formats;
import play.data.format.Formatters;
import play.libs.Akka;
import play.mvc.Call;
import utils.formatter.LocalDateFormatter;
import utils.formatter.LocalDateTimeFormatter;
import utils.formatter.LocalTimeFormatter;
import utils.formatter.LongFormatter;
import utils.formatter.YearMonthFormatter;
import akka.actor.Props;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import controllers.routes;
import events.AlertPublisher;
import filters.AccessLog;

public class Global extends GlobalSettings {

	private static final ALogger log = Logger.of(Global.class);

	@Override
	public Configuration onLoadConfig(Configuration config, File path, ClassLoader classloader) {
		Config baseConfig = ConfigFactory.load();
		// Probably want error checking here.
		File localConf = utils.Config.getMainLocalConfig();
		if (!localConf.exists()) {
			return null;
		}
		Config testConfig = ConfigFactory.parseFile(localConf);
		testConfig.resolve();
		Config finalConfig = testConfig.withFallback(baseConfig);
		return new Configuration(finalConfig);
	}

	@Override
	public <T extends EssentialFilter> Class<T>[] filters() {
		return new Class[] { AccessLog.class };
	}

	@Override
	public void onStart(Application app) {
		events.AlertPublisher.ref = Akka.system().actorOf(Props.create(AlertPublisher.class));
		PlayAuthenticate.setResolver(new Resolver() {

			@Override
			public Call login() {
				// Your login page
				return routes.Application.login();
			}

			@Override
			public Call afterAuth() {
				// The user will be redirected to this page after authentication
				// if no original URL was saved
				return routes.HomePageController.index();
			}

			@Override
			public Call afterLogout() {
				return routes.HomePageController.index();
			}

			@Override
			public Call auth(final String provider) {
				// You can provide your own authentication implementation,
				// however the default should be sufficient for most cases
				return com.feth.play.module.pa.controllers.routes.Authenticate
						.authenticate(provider);
			}

			@Override
			public Call askMerge() {
				return routes.Account.askMerge();
			}

			@Override
			public Call askLink() {
				return routes.Account.askLink();
			}

			@Override
			public Call onException(final AuthException e) {
				if (e instanceof AccessDeniedException) {
					return routes.Signup.oAuthDenied(((AccessDeniedException) e).getProviderKey());
				}

				// more custom problem handling here...
				return super.onException(e);
			}
		});

		Formatters.register(Date.class, new Formats.DateFormatter(
				LocalDateTimeFormatter.FULL_DATE_FORMAT));
		Formatters.register(Long.class, new LongFormatter());
		Formatters.register(LocalDateTime.class, new LocalDateTimeFormatter());
		Formatters.register(LocalDate.class, new LocalDateFormatter());
		Formatters.register(LocalTime.class, new LocalTimeFormatter());
		Formatters.register(YearMonth.class, new YearMonthFormatter());

		InitialData.initData();

	}

}