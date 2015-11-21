package utils;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonResponse {

	private static final String ERROR = "error";
	private static final String SUCCESS = "success";

	public static ObjectNode err() {
		return Json.newObject().put("status", ERROR);
	}

	public static ObjectNode ok() {
		return Json.newObject().put("status", "ok");
	}

	public static JsonNode build(String type, String message) {
		return Json.newObject().set(type, Json.newObject().put("message", message));
	}

	public static JsonNode buildSuccess(String message) {
		return build(SUCCESS, message);
	}

	public static JsonNode buildError(String message) {
		return build(ERROR, message);
	}

}
