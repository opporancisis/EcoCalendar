package pages.authentication;

import static org.fest.assertions.Assertions.assertThat;

import org.fluentlenium.core.FluentPage;

import pages.AbstractPage;
import controllers.routes;

public class LoginPage extends AbstractPage {
	
	@Override
	public String getUrl() {
		return routes.Application.login().url();
	}
	
	@Override
	public void isAt() {
		assertThat(url()).isEqualTo(getUrl());
		assertThat(title()).isEqualTo("Login");
		assertThat(find(".navbar .nav li.active a", 0).getAttribute("href")).contains("/login");
	}
	
	public void doLogin(String email, String password) {
		assertThat(email).isNotEmpty();
		assertThat(password).isNotEmpty();
		fill("#email").with(email);
        fill("#password").with(password);
        submit("form[action='/login']");
	}
	
}
