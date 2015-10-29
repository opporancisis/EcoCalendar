package pages.user;

import static org.fest.assertions.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.withText;

import org.fluentlenium.core.FluentPage;

import pages.AbstractPage;

import com.github.javafaker.Faker;

import controllers.routes;

public class AddUserPage extends AbstractPage {
	
	Faker faker = new Faker();

	@Override
	public String getUrl() {
		return routes.UserController.add().url();
	}
	
	@Override
	public void isAt() {
		assertThat(title()).contains("Создание пользователя");
	}
	
	public void addUser(String email) {
		fill("#email").with(email);
		select("#roles", "пользователь");
		fill("#name").with(faker.firstName());


		findFirst("input[type='submit']").click();
		assertNoErrorsInForm();
	}
	
}
