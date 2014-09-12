package models.user;

import java.util.Arrays;
import java.util.List;

public class RoleName {

	public static final String ADMIN = "admin";
	public static final String USER = "user";

	public static List<String> asList() {
		return Arrays.asList(ADMIN, USER);
	}
}
