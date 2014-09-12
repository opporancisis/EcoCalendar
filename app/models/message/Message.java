package models.message;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.user.User;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import play.db.ebean.Model;

import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;

@Entity
public class Message extends Model {

	private static final long serialVersionUID = 1L;

	// for annotation Dynamic
	public static final String CLAZZ = "models.message.Message";

	@Id
	public Long id;

	public DateTime time;

	@ManyToOne
	public User owner;

	public String subject;

	@Lob
	public String body;

	public MessageSeverity severity;

	public Boolean unread;

	public static final Finder<Long, Message> find = new Finder<>(Long.class,
			Message.class);

	public static int countUnread(User user) {
		return Message.find.query().where().eq("owner", user)
				.eq("unread", true).findRowCount();
	}

	public void send(User user) {
		time = new DateTime();
		owner = user;
		unread = true;
		save();
		if (user.emailValidated && StringUtils.isNotBlank(user.email)) {
			Mailer.getDefaultMailer().sendMail(subject, new Body(body),
					user.email);
		}

	}
}
