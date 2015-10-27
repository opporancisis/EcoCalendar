package service;

import javax.inject.Inject;

import models.user.User;
import play.Application;

import com.feth.play.module.pa.service.UserServicePlugin;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;

public class MyUserServicePlugin extends UserServicePlugin {

	@Inject
	public MyUserServicePlugin(Application app) {
		super(app);
	}

	@Override
	public Object save(AuthUser authUser) {
		// We don't allow users to singup automatically by login in via oauth
		// for example
		// return null;
		boolean isLinked = User.existsByAuthUserIdentity(authUser);
		if (!isLinked) {
			return User.create(authUser).id;
		} else {
			// we have this user already, so return null
			return null;
		}
	}

	@Override
	public Object getLocalIdentity(AuthUserIdentity identity) {
		// For production: Caching might be a good idea here...
		// ...and dont forget to sync the cache when users get
		// deactivated/deleted
		User u = User.findByAuthUserIdentity(identity);
		if (u != null) {
			return u.id;
		} else {
			return null;
		}
	}

	@Override
	public AuthUser merge(AuthUser newUser, AuthUser oldUser) {
		if (!oldUser.equals(newUser)) {
			User.merge(oldUser, newUser);
		}
		return oldUser;
	}

	@Override
	public AuthUser link(AuthUser oldUser, AuthUser newUser) {
		User.addLinkedAccount(oldUser, newUser);
		return newUser;
	}

	@Override
	public AuthUser update(AuthUser knownUser) {
		// User logged in again, bump last login date
		User.setLastLoginDate(knownUser);
		return knownUser;
	}

}