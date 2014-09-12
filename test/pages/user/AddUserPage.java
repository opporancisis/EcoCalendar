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
		return routes.UsersController.create().url();
	}
	
	@Override
	public void isAt() {
		assertThat(title()).contains("Создание пользователя");
	}
	
	public void addUser(String email) {
		fill("#email").with(email);
		select("#roles", "пользователь");
		await().until("#flatNumber").areDisplayed();
		fill("#surname").with(faker.lastName());
		fill("#name").with(faker.firstName());
		fill("#patronymic").with(faker.firstName());
		fill("#flatNumber").with(faker.numerify("##"));
		fill("#accountNumber").with(faker.numerify("########"));
		fill("#certificate").with(faker.numerify("########"));
		executeScript("$('#membershipStartDate').datepicker('remove');");	// disable datepicker to make the test more reliable
		fill("#membershipStartDate").with(faker.numerify("1#-0#-201#"));
		fill("#tenants").with(faker.numerify("#"));
		fill("#flatArea").with(faker.numerify("##"));
		fill("#note").with(faker.sentence());
		findFirst("input[type='submit']").click();
		assertNoErrorsInForm();
	}
	
}
