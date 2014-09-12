package controllers;

import static org.slf4j.LoggerFactory.getLogger;
import models.user.User;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.WebSocket;
import actors.message.CloseConnectionEvent;
import actors.message.NewConnectionEvent;
import akka.actor.ActorRef;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.feth.play.module.pa.PlayAuthenticate;
import com.feth.play.module.pa.user.AuthUser;

import events.AlertPublisher;

public class CommunicationController extends Controller {

	private static final Logger LOG = getLogger(CommunicationController.class);
	
	private static final ObjectNode FORBIDDEN = Json.newObject().put("forbidden", true);

	public static WebSocket<JsonNode> alerts(final String uuid) {
		AuthUser currentAuthUser = PlayAuthenticate.getUser(session());
		final User user;
		final MutableBoolean doClose = new MutableBoolean(false);
		if (currentAuthUser == null) {
			LOG.debug("no user info in session, will close connection");
			doClose.setValue(true);
			user = null;
		} else {
			user = User.findByAuthUserIdentity(currentAuthUser);
		}
		return new WebSocket<JsonNode>() {

			@Override
			public void onReady(play.mvc.WebSocket.In<JsonNode> in,
					play.mvc.WebSocket.Out<JsonNode> out) {
				if (doClose.booleanValue()) {
					LOG.debug("sending Forbidden command to client");
	                out.write(FORBIDDEN);
	                out.close();
	                return;
				}
				AlertPublisher.ref.tell(
						new NewConnectionEvent(uuid, out, user),
						ActorRef.noSender());

				// For each event received on the socket,
				in.onMessage(new Callback<JsonNode>() {
					public void invoke(JsonNode event) {
						LOG.debug("event received from client: " + event);
					}
				});

				// When the socket is closed.
				in.onClose(new Callback0() {
					public void invoke() {
						AlertPublisher.ref.tell(new CloseConnectionEvent(uuid),
								ActorRef.noSender());
					}
				});

			}

		};
	}

}
