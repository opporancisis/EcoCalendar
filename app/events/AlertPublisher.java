package events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import models.message.Message;
import models.user.User;

import org.apache.commons.lang3.tuple.Pair;

import play.Logger;
import play.Logger.ALogger;
import play.libs.Akka;
import play.libs.Json;
import play.mvc.WebSocket.Out;
import actors.message.CloseConnectionEvent;
import actors.message.NewConnectionEvent;
import actors.message.UserEvent;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AlertPublisher extends UntypedActor {

	private static final ALogger LOG = Logger.of(AlertPublisher.class);

	public static ActorRef ref; // = Akka.system().actorOf(Props.create(AlertPublisher.class));

	private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	// Pair instead of HashMap - cause one user can has more than one openned
	// window
	private Map<String, Pair<User, Out<JsonNode>>> connections = new HashMap<>();
	private Map<User, Set<String>> userToUuids = new HashMap<>();

	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof NewConnectionEvent) {
			NewConnectionEvent nce = (NewConnectionEvent) message;
			lock.writeLock().lock();
			try {
				Set<String> uuids = userToUuids.get(nce.user());
				if (uuids == null) {
					uuids = new HashSet<>();
					userToUuids.put(nce.user(), uuids);
				} else if (uuids.contains(nce.uuid())) {
					Pair<User, Out<JsonNode>> pair = connections
							.get(nce.uuid());
					assert pair != null : "programming error!";
					pair.getValue().close();
				}
				uuids.add(nce.uuid());
				connections.put(nce.uuid(), Pair.of(nce.user(), nce.out()));
			} finally {
				lock.writeLock().unlock();
			}
			LOG.info("New browser connected (user {}): {}", nce.user(),
					nce.uuid());
		} else if (message instanceof CloseConnectionEvent) {
			CloseConnectionEvent cce = (CloseConnectionEvent) message;
			lock.writeLock().lock();
			try {
				Pair<User, Out<JsonNode>> pair = connections.remove(cce.uuid());
				if (pair != null) {
					Set<String> uuids = userToUuids.get(pair.getKey());
					assert uuids != null : "programming error!";
					uuids.remove(cce.uuid());
					if (uuids.isEmpty()) {
						userToUuids.remove(pair.getKey());
					}
					LOG.info("Browser is disconnected (user {}): {}",
							pair.getKey(), cce.uuid());
				}
			} finally {
				lock.writeLock().unlock();
			}
		} else if (message instanceof UserEvent) {
			broadcastEvent((UserEvent) message);
			Logger.info("Message broadcasted: {}", message.getClass()
					.getSimpleName());
		} else {
			Logger.warn("unknown message: {}", message.getClass()
					.getSimpleName());
			unhandled(message);
		}
	}

	private void broadcastEvent(UserEvent message) {
		Set<User> coveredUsers = new HashSet<>();
		for (Pair<User, Out<JsonNode>> pair : connections.values()) {
			User user = pair.getKey();
			boolean notCovered = coveredUsers.add(user);
			if (!message.isRecepient(user)) {
				continue;
			}
			ObjectNode json = message.json(user);
			if (message.isGeneratesMessage()) {
				Message uiMess = message.message(user);
				ObjectNode payload = (ObjectNode) json.get("payload");
				if (payload == null) {
					payload = Json.newObject();
					json.put("payload", payload);
				}
				payload.put("body", uiMess.body);
				payload.put("subject", uiMess.subject);
				payload.put("severity", uiMess.severity.name());
				if (notCovered) {
					uiMess.send(user);
				}
				// count unread after send! cause send may do +1
				payload.put("messagesCounter", Message.countUnread(user));
			}
			pair.getValue().write(json);
		}
		message.doAdditionalAlerts();
		if (message.isGeneratesMessage()) {
			for (User user : User.find.all()) {
				if (!message.isRecepient(user) || coveredUsers.contains(user)) {
					continue;
				}
				Message uiMess = message.message(user);
				uiMess.send(user);
			}
		}
	}
}
