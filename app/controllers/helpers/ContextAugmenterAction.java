package controllers.helpers;

import java.util.UUID;

import models.message.Message;
import models.standardPage.StandardPage;
import models.user.User;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

public class ContextAugmenterAction extends Action<ContextAugmenter> {

	@Override
	public Promise<Result> call(Context ctx) throws Throwable {
		fillContext(ctx);
		return delegate.call(ctx);
	}

	public static void fillContext(Context ctx) {
		if (ctx.args.containsKey("user")) {
			return;
		}
		AuthUser currentAuthUser = PlayAuthenticate.getUser(ctx.session());
		User user = User.findByAuthUserIdentity(currentAuthUser);
		ctx.args.put("user", user);
		ctx.args.put("unread", user != null ? Message.countUnread(user) : 0);
		ctx.args.put("stdPages", StandardPage.find.query().order("orderInd")
				.findList());
		ctx.args.put("uuid", UUID.randomUUID().toString());
	}

	public static User getLoggedUser() {
		return (User) Context.current().args.get("user");
	}

}
