package views;

import helpers.IntegrationHelper;

import org.junit.Before;
import org.junit.Test;

import pages.user.AddUserPage;
import pages.user.ListUsersPage;

import com.github.javafaker.Faker;

public class UserTest extends AbstractTest {
	
	private ListUsersPage listUsersPage;
	private AddUserPage addUserPage;
	private Faker faker = new Faker();

	@Before
	public void setUp() {
		listUsersPage = browser.createPage(ListUsersPage.class);
		addUserPage = browser.createPage(AddUserPage.class);
		IntegrationHelper.loginAs("admin", browser);
	}
	
	@Test
	public void test() {
		String email = faker.letterify("??????@?????.???");
		
		// Add user
		addUserPage.go();
		addUserPage.isAt();
		addUserPage.addUser(email);
		listUsersPage.go();
		listUsersPage.hasUser(email);
	}

}
