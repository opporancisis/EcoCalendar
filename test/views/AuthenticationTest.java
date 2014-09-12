package views;

import helpers.IntegrationHelper;

import org.junit.Before;
import org.junit.Test;

import pages.IndexPage;

public class AuthenticationTest extends AbstractTest {
	
	private IndexPage indexPage;

	@Before
	public void setUp() {
		indexPage = browser.createPage(IndexPage.class);
	}

	@Test
	public void test() {
		indexPage.go();
		indexPage.isAt();
		indexPage.isLoggedOut();
		IntegrationHelper.loginAs("admin", browser);
		indexPage.isAt();
		indexPage.isLoggedIn();
		indexPage.doLogout();
		indexPage.isAt();
		indexPage.isLoggedOut();
	}

}
