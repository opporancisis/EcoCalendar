package actors.message;

public abstract class MessageableEvent extends BaseUserEvent {

	public MessageableEvent() {
		// no-op
	}

	public MessageableEvent(boolean generatesMessage) {
		super(generatesMessage);
	}

	public abstract String subject();

	public abstract String body();

}
