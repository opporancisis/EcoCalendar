package actors.message;

import models.message.Message;
import models.user.User;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

public abstract class BaseUserEvent implements UserEvent {

	protected static final String LINK = "link";

	private boolean generatesMessage;

	public BaseUserEvent(boolean generatesMessage) {
		this.generatesMessage = generatesMessage;
	}

	public BaseUserEvent() {
		this(false);
	}

	@Override
	public ObjectNode json(User user) {
		ObjectNode result = Json.newObject();
		result.put("message", getClass().getSimpleName());
		ObjectNode payload = payload(user);
		if (payload != null) {
			result.put("payload", payload);
		}
		return result;
	}

	protected ObjectNode payload(User user) {
		// no payload by default. message will be generated in that case
		return null;
	}

	@Override
	public void doAdditionalAlerts() {
		// do nothing by default
	}

	@Override
	public boolean isGeneratesMessage() {
		return generatesMessage;
	}

	@Override
	public Message message(User user) {
		if (generatesMessage) {
			// that means subclass must provide implementation
			throw new UnsupportedOperationException();
		} else {
			return null;
		}
	}

}
