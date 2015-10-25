package providers;

import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;

public class MyResetPasswordAuthUser extends UsernamePasswordAuthUser {

	private static final long serialVersionUID = 1L;

	public MyResetPasswordAuthUser(String password) {
		super(password, null);
	}

}