package actors.message;

import models.message.Message;
import models.user.User;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface UserEvent {

	public ObjectNode json(User user);

	public boolean isRecepient(User user);

	public void doAdditionalAlerts();

	Message message(User user);

	boolean isGeneratesMessage();
}
