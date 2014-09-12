package helpers;

import com.typesafe.config.Config;

import pages.IndexPage;
import pages.authentication.LoginPage;
import play.test.TestBrowser;

public class IntegrationHelper {

	public static void loginAs(String role, TestBrowser browser) {
		Config config = TestHelper.getConfig();
		LoginPage loginPage = browser.createPage(LoginPage.class);
		IndexPage indexPage = browser.createPage(IndexPage.class);
		loginPage.go();
		loginPage.doLogin(
				config.getString("auth." + role + ".user"),
				config.getString("auth." + role + ".password"));
		indexPage.isLoggedIn();
	}
	
}
