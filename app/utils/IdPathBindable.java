package utils;

import play.db.ebean.Model.Finder;
import play.i18n.Messages;
import play.mvc.PathBindable;

public interface IdPathBindable<T extends PathBindable<T>> extends
		PathBindable<T> {

	default T bind(String key, String txt) {
		T obj;

		try {
			obj = ((Finder<Long, T>) getClass().getDeclaredField("find").get(
					null)).byId(Long.parseLong(txt));
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("programming error", e);
		}
		if (obj != null) {
			return obj;
		} else {
			throw new IllegalArgumentException(Messages.get(
					"error.not.found.object",
					Messages.get("label.entity."
							+ this.getClass().getSimpleName()), txt));
		}
	}

	default String unbind(String key) {
		try {
			return "" + getClass().getDeclaredField("id").get(this);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException("programming error", e);
		}
	}

	default String javascriptUnbind() {
		return null;
	}
}
