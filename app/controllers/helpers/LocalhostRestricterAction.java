package controllers.helpers;

import java.util.regex.Pattern;

import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;

public class LocalhostRestricterAction extends Action<LocalhostRestricter> {

	private static final Pattern LOCAL_HOST_PAT = Pattern
			.compile("localhost|127\\.0\\.0\\.1");

	@Override
	public Promise<Result> call(Context ctx) throws Throwable {
		if (!LOCAL_HOST_PAT.matcher(ctx.request().remoteAddress()).matches()) {
			return F.Promise.pure((Result) forbidden());
		}
		return delegate.call(ctx);
	}

}
