package models.user;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import models.geo.City;
import models.geo.Country;
import models.karma.KarmaChange;
import models.message.Message;
import models.sys.Setting;
import models.sys.SettingName;

import org.apache.commons.lang3.StringUtils;

import play.db.ebean.Transactional;
import providers.MyResetPasswordAuthUser;
import utils.IdPathBindable;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.Model;
import com.feth.play.module.pa.providers.password.UsernamePasswordAuthUser;
import com.feth.play.module.pa.user.AuthUser;
import com.feth.play.module.pa.user.AuthUserIdentity;
import com.feth.play.module.pa.user.EmailIdentity;
import com.feth.play.module.pa.user.FirstLastNameIdentity;
import com.feth.play.module.pa.user.NameIdentity;

import controllers.Application;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "NICK", "EMAIL" }))
public class User extends Model implements Subject, IdPathBindable<User> {

	private static final String NICK_AUTO_GENERATED_PREFIX = "auto-generated-nick-";

	public static final String NICK_PAT = "[\\w\\s\\-_\\d]+";

	@Id
	public Long id;

	// if you make this unique, keep in mind that users *must* merge/link their
	// accounts then on signup with additional providers
	// @Column(unique = true)
	public String email;

	// TODO: what's the workflow?
	public Boolean emailPublic;

	public String nick;

	public String name;

	public String phone;

	public String profileLink;

	/**
	 * При создании пользователя (первой авторизации) карма = 0. С кармой < 50
	 * события пользователя проходят премодерацию (администратором). При этом
	 * модерируя администратор может прибавить кармы от 0 до 10. Например, ему
	 * нравится событие, оно корректно оформленно, тогда он делает мероприятие
	 * публичным (одобряет его) и прибавляет +10 в карму создателю. Если в
	 * описании мероприятия есть неточности, но сделать его публичным уже можно,
	 * то +5 в карму. Это для примера, но в конечном итоге все на субъективный
	 * взгляд администратора. Если он считает ценным вклад создателя (созданное
	 * мероприятие) и хочет, чтобы создатель быстрее набрал пороговую карму
	 * (50), то он увеличивает ее на большое значение. Иначе - на меньшее.
	 */
	public Long karma;

	@OneToMany(cascade = CascadeType.ALL)
	public List<KarmaChange> karmaHistory;

	// TODO: let user specify his city in profile settings
	@ManyToOne
	public City city;

	// TODO: let user specify his country in profile settings
	@ManyToOne
	public Country countryForUnknownCity;

	// TODO: let user specify unknown for portal city as his own
	/**
	 * Если при регистрации человек не находит в списке своего города, то он(а)
	 * заполняет это поле. В случае появления модератора для этого города (т.е.
	 * город появляется в базе City), для человека выставляется автоматом city
	 * на основе unknownCity (если он правильно заполнил это поле). И также
	 * приходит нотификация об изменении.
	 */
	public String unknownCity;

	public String note;

	public LocalDateTime lastLogin;

	public boolean blocked;

	public boolean emailValidated;

	@ManyToMany(cascade = CascadeType.ALL)
	public List<SecurityRole> roles;

	@OneToMany(cascade = CascadeType.ALL)
	public List<LinkedAccount> linkedAccounts;

	public static final Find<Long, User> find = new Find<Long, User>() {
	};

	@Override
	public String getIdentifier() {
		return Long.toString(id);
	}

	public String somename() {
		if (StringUtils.isNotBlank(name)) {
			return name;
		} else {
			return nick;
		}
	}

	@Override
	public List<? extends Role> getRoles() {
		return roles;
	}

	public boolean hasRole(String... aRoles) {
		Set<String> aRolesSet = new HashSet<>(Arrays.asList(aRoles));
		return hasRole(aRolesSet);
	}

	public boolean hasRole(Set<String> aRoles) {
		for (SecurityRole role : roles) {
			if (aRoles.contains(role.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean isAdmin() {
		for (SecurityRole role : roles) {
			if (role.roleName.equals(RoleName.ADMIN)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<? extends Permission> getPermissions() {
		return null;
	}

	public static boolean existsByAuthUserIdentity(final AuthUserIdentity identity) {
		final ExpressionList<User> exp;
		if (identity instanceof UsernamePasswordAuthUser) {
			exp = getUsernamePasswordAuthUserFind((UsernamePasswordAuthUser) identity);
		} else {
			exp = getAuthUserFind(identity);
		}
		return exp.findRowCount() > 0;
	}

	private static ExpressionList<User> getAuthUserFind(final AuthUserIdentity identity) {
		return find.where().eq("blocked", false)
				.eq("linkedAccounts.providerUserId", identity.getId())
				.eq("linkedAccounts.providerKey", identity.getProvider());
	}

	public static User findByAuthUserIdentity(final AuthUserIdentity identity) {
		if (identity == null) {
			return null;
		}
		if (identity instanceof UsernamePasswordAuthUser) {
			return findByUsernamePasswordIdentity((UsernamePasswordAuthUser) identity);
		} else {
			return getAuthUserFind(identity).findUnique();
		}
	}

	public static User findByUsernamePasswordIdentity(final UsernamePasswordAuthUser identity) {
		return getUsernamePasswordAuthUserFind(identity).findUnique();
	}

	private static ExpressionList<User> getUsernamePasswordAuthUserFind(
			final UsernamePasswordAuthUser identity) {
		return getEmailUserFind(identity.getEmail()).eq("linkedAccounts.providerKey",
				identity.getProvider());
	}

	public void merge(final User otherUser) {
		for (final LinkedAccount acc : otherUser.linkedAccounts) {
			this.linkedAccounts.add(LinkedAccount.create(acc));
		}
		// do all other merging stuff here - like resources, etc.

		// deactivate the merged user that got added to this one
		otherUser.blocked = true;
		Ebean.save(Arrays.asList(new User[] { otherUser, this }));
	}

	@Transactional
	public static User create(final AuthUser authUser) {
		User user = new User();
		user.roles = Collections.singletonList(SecurityRole
				.findByRoleName(models.user.RoleName.USER));
		// user.permissions = new ArrayList<>();
		// user.permissions.add(UserPermission.findByValue("printers.edit"));
		user.blocked = false;
		user.lastLogin = Application.now();
		user.linkedAccounts = Collections.singletonList(LinkedAccount
				.create(authUser));

		if (authUser instanceof EmailIdentity) {
			EmailIdentity identity = (EmailIdentity) authUser;
			// Remember, even when getting them from FB & Co., emails should be
			// verified within the application as a security breach there might
			// break your security as well!
			user.email = identity.getEmail();
			user.emailValidated = false;
		}

		if (authUser instanceof NameIdentity) {
			NameIdentity identity = (NameIdentity) authUser;
			String name = identity.getName();
			if (name != null) {
				user.nick = name;
			}
		}

		if (authUser instanceof FirstLastNameIdentity) {
			FirstLastNameIdentity identity = (FirstLastNameIdentity) authUser;
			String firstName = identity.getFirstName();
			String lastName = identity.getLastName();
			if (lastName != null) {
				user.nick = lastName;
			}
			if (firstName != null) {
				if (lastName != null) {
					user.nick += " ";
				}
				user.nick += firstName;
			}
		}
//		user.updateNick();
		user.save();
		Ebean.saveManyToManyAssociations(user, "roles");
		// user.saveManyToManyAssociations("permissions");
		return user;
	}

	public static void merge(final AuthUser oldUser, final AuthUser newUser) {
		User.findByAuthUserIdentity(oldUser).merge(User.findByAuthUserIdentity(newUser));
	}
	
	public static String makeUnconflictedNick(String initialNick) {
		if (StringUtils.isBlank(initialNick) || initialNick.startsWith(NICK_AUTO_GENERATED_PREFIX)) {
			initialNick = NICK_AUTO_GENERATED_PREFIX + "1";
		}
		String newNick = initialNick;
		int i = 1;
		while (User.find.query().where().eq("nick", newNick).findRowCount() > 0) {
			newNick = NICK_AUTO_GENERATED_PREFIX + ++i;
		}
		return newNick;
	}

	public Set<String> getProviders() {
		final Set<String> providerKeys = new HashSet<>(linkedAccounts.size());
		for (final LinkedAccount acc : linkedAccounts) {
			providerKeys.add(acc.providerKey);
		}
		return providerKeys;
	}

	public static void addLinkedAccount(final AuthUser oldUser, final AuthUser newUser) {
		final User u = User.findByAuthUserIdentity(oldUser);
		u.linkedAccounts.add(LinkedAccount.create(newUser));
		u.save();
	}

	public static void setLastLoginDate(final AuthUser knownUser) {
		final User u = User.findByAuthUserIdentity(knownUser);
		u.lastLogin = Application.now();
		u.save();
	}

	public static User findByEmail(final String email) {
		return getEmailUserFind(email).findUnique();
	}

	private static ExpressionList<User> getEmailUserFind(final String email) {
		return find.where().eq("blocked", false).eq("email", email);
	}

	public LinkedAccount getAccountByProvider(final String providerKey) {
		return LinkedAccount.findByProviderKey(this, providerKey);
	}

	@Transactional
	public static void verify(final User unverified) {
		unverified.emailValidated = true;
		unverified.save();
		TokenAction.deleteByUser(unverified, TokenAction.Type.EMAIL_VERIFICATION);
	}

	public void changePassword(String newPassword, boolean create) {
		UsernamePasswordAuthUser authUser = new MyResetPasswordAuthUser(newPassword);
		LinkedAccount a = this.getAccountByProvider(authUser.getProvider());
		if (a == null) {
			if (create) {
				a = LinkedAccount.create(authUser);
				a.user = this;
			} else {
				throw new RuntimeException("Account not enabled for password usage");
			}
		}
		a.providerUserId = authUser.getHashedPassword();
		a.save();
	}

	public void resetPassword(String newPassword, boolean create) {
		// You might want to wrap this into a transaction
		this.changePassword(newPassword, create);
		TokenAction.deleteByUser(this, TokenAction.Type.PASSWORD_RESET);
	}

	@Transactional
	public boolean deleteGracefully() {
		User deletedAccount = User.find.byId(Setting.getLong(SettingName.DELETED_USER_ID));
		if (deletedAccount == null) {
			return false;
		}
		List<Message> messages = Message.find.query().where().eq("owner", this).findList();
		for (Message message : messages) {
			message.delete();
		}
		delete();
		return true;
	}

	public boolean hasEnoughPowerToPublishEvents() {
		return isAdmin() || (karma != null && karma >= 50);
	}
}