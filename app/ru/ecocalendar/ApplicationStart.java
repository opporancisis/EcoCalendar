package ru.ecocalendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Environment;
import play.Logger;
import play.data.format.Formats;
import play.data.format.Formatters;
import play.mvc.Call;
import utils.formatter.LocalDateFormatter;
import utils.formatter.LocalDateTimeFormatter;
import utils.formatter.LocalTimeFormatter;
import utils.formatter.LongFormatter;
import utils.formatter.YearMonthFormatter;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.PlayAuthenticate.Resolver;
import com.feth.play.module.pa.exceptions.AccessDeniedException;
import com.feth.play.module.pa.exceptions.AuthException;

import controllers.routes;

@Singleton
public class ApplicationStart {

	@Inject
	public ApplicationStart(Environment environment) {
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
			public Call auth(String provider) {
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
			public Call onException(AuthException e) {
				if (e instanceof AccessDeniedException) {
					// return routes.Signup.oAuthDenied(((AccessDeniedException)
					// e).getProviderKey());
					Logger.warn("what???", e);
					return routes.Application.login();
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

		if (environment.isProd() || environment.isDev()) {
			InitialData.initData("initial-data.yml");
		}

	}
}
