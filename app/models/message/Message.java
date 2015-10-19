package models.message;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import models.user.User;

import org.apache.commons.lang3.StringUtils;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.CreatedTimestamp;
import com.feth.play.module.mail.Mailer;
import com.feth.play.module.mail.Mailer.Mail.Body;

@Entity
public class Message extends Model {

	// for annotation Dynamic
	public static final String CLAZZ = "models.message.Message";

	@Id
	public Long id;

	@CreatedTimestamp
	public Date created;

	@ManyToOne
	public User owner;

	public String subject;

	@Lob
	public String body;

	public MessageSeverity severity;

	public Boolean unread;

	public static final Find<Long, Message> find = new Find<Long, Message>() {
	};

	public Message() {
		// do nothing
	}

	public Message(String subject, String body, MessageSeverity severity) {
		this.subject = subject;
		this.body = body;
		this.severity = severity;
	}

	public static int countUnread(User user) {
		return Message.find.query().where().eq("owner", user).eq("unread", true).findRowCount();
	}

	public void send(User user) {
		owner = user;
		unread = true;
		save();
		if (user.emailValidated && StringUtils.isNotBlank(user.email)) {
			Mailer.getDefaultMailer().sendMail(subject, new Body(null, body), user.email);
		}

	}
}
