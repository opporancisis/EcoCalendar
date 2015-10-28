package providers.oauth2.vk;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthInfo;
import com.feth.play.module.pa.providers.oauth2.OAuth2AuthProvider;

public class VkAuthInfo extends OAuth2AuthInfo {

	private static final long serialVersionUID = 1L;

	private static final String EMAIL = "email";
	private static final String USER_ID = "user_id";

	private String userId;

	private String email;

	public VkAuthInfo(JsonNode node) {
		super(node.get(OAuth2AuthProvider.Constants.ACCESS_TOKEN).asText(), new Date().getTime()
				+ node.get(OAuth2AuthProvider.Constants.EXPIRES_IN).asLong() * 1000);

		if (node.has(USER_ID)) {
			this.userId = node.get(USER_ID).asText();
		}

		if (node.has(EMAIL)) {
			this.email = node.get(EMAIL).asText();
		}
	}

	public String getUserId() {
		return userId;
	}

	public String getEmail() {
		return email;
	}
}
