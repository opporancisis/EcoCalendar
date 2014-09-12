package pages;

import static org.fest.assertions.Assertions.assertThat;
import static org.fluentlenium.core.filter.FilterConstructor.withText;

import java.util.concurrent.TimeUnit;

import org.fluentlenium.core.FluentPage;
import org.fluentlenium.core.domain.FluentList;
import org.fluentlenium.core.domain.FluentWebElement;

import play.Logger;

public abstract class AbstractPage extends FluentPage {

	public void assertErrorsInForm() {
		FluentList<FluentWebElement> errors = find("form .has-error");
		if (errors.isEmpty()) {
			Logger.error("expecting the form to have errors, but none is present");
			await().atMost(5, TimeUnit.MINUTES).until("form .has-error").isPresent();
		}
	}
	
	public void assertNoErrorsInForm() {
		FluentList<FluentWebElement> errors = find("form .has-error");
		if (!errors.isEmpty()) {
			Logger.error("expecting the form not to have errors, but at least one is present");
			await().atMost(5, TimeUnit.MINUTES).until("form .has-error").isNotPresent();
		}
	}
	
	public void select(String selector, String option) {
		findFirst(selector + "+.btn-group>.multiselect").click();
		findFirst(selector + "+.btn-group>.multiselect-container li", withText().contains(option))
				.findFirst("input").click();
	}
	
	public void contains(String expected) {
		assertThat(pageSource()).contains(expected);
	}
	
}
