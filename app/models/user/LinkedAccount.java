package models.user;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Model;
import com.feth.play.module.pa.user.AuthUser;

@Entity
public class LinkedAccount extends Model {

	@Id
	public Long id;

	@ManyToOne
	public User user;

	public String providerUserId;
	public String providerKey;

	public static final Find<Long, LinkedAccount> find = new Find<Long, LinkedAccount>() {
	};

	public static LinkedAccount findByProviderKey(User user, String key) {
		return find.where().eq("user", user).eq("providerKey", key).findUnique();
	}

	public static LinkedAccount create(AuthUser authUser) {
		LinkedAccount ret = new LinkedAccount();
		ret.update(authUser);
		return ret;
	}

	public void update(AuthUser authUser) {
		this.providerKey = authUser.getProvider();
		this.providerUserId = authUser.getId();
	}

	public static LinkedAccount create(LinkedAccount acc) {
		LinkedAccount ret = new LinkedAccount();
		ret.providerKey = acc.providerKey;
		ret.providerUserId = acc.providerUserId;
		return ret;
	}
}