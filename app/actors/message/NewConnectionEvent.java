package actors.message;

import models.user.User;
import play.mvc.WebSocket.Out;

import com.fasterxml.jackson.databind.JsonNode;

public class NewConnectionEvent {

	private String uuid;
	private Out<JsonNode> out;
	private User user;

	public NewConnectionEvent(String uuid, Out<JsonNode> out, User user) {
		this.uuid = uuid;
		this.out = out;
		this.user = user;
	}

	public String uuid() {
		return uuid;
	}

	public Out<JsonNode> out() {
		return out;
	}

	public User user() {
		return user;
	}

}
