package pages;

import static org.fest.assertions.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.with;

import org.fluentlenium.core.FluentPage;

import controllers.routes;

public class IndexPage extends AbstractPage {
	
	@Override
	public String getUrl() {
		return routes.Application.index().url();
	}
	
	@Override
	public void isAt() {
		assertThat(url()).isEqualTo(getUrl());
        assertThat(title()).contains("Добро пожаловать!");
	}
	
	public void isLoggedIn() {
		assertThat(find(".navbar-right .dropdown li>a[href='/logout']")).hasSize(1);
	}
	
	public void isLoggedOut() {
		assertThat(find(".navbar-nav li.active a", with("href").contains("/login"))).hasSize(1);
	}
	
	public void doLogout() {
		// TODO: Get URL via reverse routing
		goTo("/logout");
	}
	
}
