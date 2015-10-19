package security;

import java.util.Optional;

import models.user.User;
import play.i18n.Messages;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUserIdentity;

import controllers.Application;
import controllers.helpers.ContextAugmenterAction;

public class MyDeadboltHandler extends AbstractDeadboltHandler {

	private static final DynamicResourceHandler DYNAMIC_RESOURCE_HANDLER = new MyDynamicResourceHandler();

	@Override
	public F.Promise<Optional<Result>> beforeAuthCheck(Http.Context context) {
		if (PlayAuthenticate.isLoggedIn(context.session())) {
			// user is logged in
			return F.Promise.promise(Optional::empty);
		} else {
			// user is not logged in

			// call this if you want to redirect your visitor to the page that
			// was requested before sending him to the login page
			// if you don't call this, the user will get redirected to the page
			// defined by your resolver
			String originalUrl = PlayAuthenticate.storeOriginalUrl(context);

			context.flash().put(Application.FLASH_ERROR_KEY,
					Messages.get("error.you.need.to.log.in.first.to.view", originalUrl));
			return F.Promise.promise(() -> Optional.of(redirect(PlayAuthenticate.getResolver()
					.login())));
		}
	}

	@Override
	public F.Promise<Optional<Subject>> getSubject(Http.Context context) {
		AuthUserIdentity u = PlayAuthenticate.getUser(context);
		// Caching might be a good idea here
		return F.Promise.promise(() -> Optional.ofNullable(User.findByAuthUserIdentity(u)));
	}

	@Override
	public F.Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(
			Http.Context context) {
		return F.Promise.promise(() -> Optional.of(DYNAMIC_RESOURCE_HANDLER));
	}

	@Override
	public F.Promise<Result> onAuthFailure(final Http.Context context, final String content) {
		ContextAugmenterAction.fillContext(context);
		// if the user has a cookie with a valid user and the local user has
		// been deactivated/deleted in between, it is possible that this gets
		// shown. You might want to consider to sign the user out in this case.
		return F.Promise.promise(() -> Application.forbiddenPage());
	}
}