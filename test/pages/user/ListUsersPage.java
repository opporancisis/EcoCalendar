package pages.user;

import static org.fest.assertions.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.withText;

import org.fluentlenium.core.FluentPage;

import pages.AbstractPage;
import controllers.routes;

public class ListUsersPage extends AbstractPage {
	
	@Override
	public String getUrl() {
		return routes.UsersController.list().url();
	}
	
	@Override
	public void isAt() {
		assertThat(title()).contains("Пользователи");
	}

	public void hasUser(String email) {
		assertThat(pageSource()).contains(email);
	}
	
}
