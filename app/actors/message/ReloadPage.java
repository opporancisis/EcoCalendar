package actors.message;

import java.util.Set;

import models.user.User;
import play.libs.Json;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ReloadPage extends BaseUserEvent {

	private String[] links;

	private Set<String> recievers;

	public ReloadPage(Set<String> recievers, String... links) {
		this.links = links;
		this.recievers = recievers;
	}

	@Override
	public ObjectNode payload(User user) {
		ObjectNode obj = Json.newObject();
		ArrayNode arr = obj.putArray("links");
		for (String link : links) {
			arr.add(link);
		}
		return obj;
	}

	@Override
	public boolean isRecepient(User user) {
		return user.hasRole(recievers);
	}

}
