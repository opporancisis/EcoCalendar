package views;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.testBrowser;
import helpers.TestHelper;

import org.openqa.selenium.WebDriver;

import play.test.FakeApplication;
import play.test.TestBrowser;
import play.test.WithBrowser;

import com.typesafe.config.Config;

abstract class AbstractTest extends WithBrowser {
	
	protected Config config = TestHelper.getConfig();
	protected Class<? extends WebDriver> webDriverClass;
	
	protected AbstractTest() {
		System.setProperty("webdriver.chrome.driver",
				config.getString("webdriver.chrome.driver"));
		webDriverClass = TestHelper.loadWebDriver(config.getString("webdriver.class"));
	}
	
	@Override
	protected TestBrowser provideBrowser(int port) {
		return testBrowser(webDriverClass, port);
	}
	
	@Override
	protected FakeApplication provideFakeApplication() {
		return fakeApplication(inMemoryDatabase());
	}

}
