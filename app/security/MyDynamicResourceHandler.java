package security;

import java.lang.reflect.Field;

import models.user.RoleName;
import models.user.User;
import play.db.ebean.Model.Finder;
import play.mvc.Http.Context;
import be.objectify.deadbolt.core.DeadboltAnalyzer;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;

public class MyDynamicResourceHandler implements DynamicResourceHandler {

	public static final String DELIM = "-";

	public static final String CHECK_AUTHORSHIP = "checkAuthorship" + DELIM;
	public static final String AUTHORED_OBJECT = "authoredObject";

	@Override
	public boolean isAllowed(String name, String meta,
			DeadboltHandler deadboltHandler, Context context) {
		Subject subject = deadboltHandler.getSubject(context);

		if (name.startsWith(CHECK_AUTHORSHIP)) {
			return checkAuthorship(name.substring(CHECK_AUTHORSHIP.length()),
					context, subject);
		}

		return false;
	}

	private boolean checkAuthorship(String subName, Context context,
			Subject subject) {
		try {
			int ind = subName.indexOf(DELIM);
			String idStr;
			if (ind != -1) {
				subName = subName.substring(0, ind);
				idStr = subName.substring(ind + 1);
			} else {
				idStr = context
						.request()
						.uri()
						.substring(context.request().uri().lastIndexOf('/') + 1);
			}
			long id = Long.parseLong(idStr);
			Class<?> clazz = Class.forName(subName);
			Field f = clazz.getDeclaredField("find");
			@SuppressWarnings("unchecked")
			Object obj = ((Finder<Long, ?>) f.get(null)).byId(id);
			context.args.put(AUTHORED_OBJECT, obj);
			if (DeadboltAnalyzer.hasRole(subject, RoleName.ADMIN)) {
				return true;
			}
			Field authorField = obj.getClass().getDeclaredField("owner");
			User author = (User) authorField.get(obj);
			return subject.getIdentifier().equals(author.getIdentifier());
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getAuthoredObject(Class<? extends T> clazz) {
		return (T) Context.current().args.get(AUTHORED_OBJECT);
	}

	@Override
	public boolean checkPermission(String permissionValue,
			DeadboltHandler deadboltHandler, Context ctx) {
		// TODO Auto-generated method stub
		return false;
	}

}
