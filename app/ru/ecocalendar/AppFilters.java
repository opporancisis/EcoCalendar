package ru.ecocalendar;

import javax.inject.Inject;

import play.api.mvc.EssentialFilter;
import play.http.HttpFilters;
import filters.AccessLog;

public class AppFilters implements HttpFilters {

	private AccessLog accessLog;

	@Inject
	public AppFilters(AccessLog accessLog) {
		this.accessLog = accessLog;
	}
	
	@Override
	public EssentialFilter[] filters() {
		return new EssentialFilter[] { accessLog };
	}

}
