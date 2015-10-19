package utils;

import play.libs.Json;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonResponse {

	public static ObjectNode err() {
		return Json.newObject().put("status", "error");
	}

	public static ObjectNode ok() {
		return Json.newObject().put("status", "ok");
	}

}
