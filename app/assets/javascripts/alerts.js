var _updatePageElements;

$(document).ready(function() {
	if (window.WebSocket === undefined) {
		bootbox.alert("Технология WebSocket не поддерживается. Пожалуйста обновите ваш браузер.");
		return;
	}
	
	var loc = window.location, ws_uri;
	if (loc.protocol === "https:") {
		ws_uri = "wss:";
	} else {
		ws_uri = "ws:";
	}
	ws_uri += "//" + loc.host;
	ws_uri += $("#ws-url").val();
	
	var pinger, socket = new ReconnectingWebSocket(ws_uri);
	var reconnects = 0;
	
	socket.reconnectInterval = 5000;
	socket.timeoutInterval = 8000;

	socket.onmessage = function(event) {
		var data = JSON.parse(event.data);
		if (data.forbidden) {
			console.log("Connection is closed due to Forbidden command");
			socket.close();
			return;
		}
		var $message, $alert;
		console.log(moment().format("L LT:ss") + ": [Event] " + data.message);
		if (_updatePageElements) {
			_updatePageElements(data);
		}
		var payload = data.payload;
		if (payload.subject) {
			/* jshint scripturl: true */
			$message = $("<a/>").attr("href", "javascript:void(0)").addClass("message")
					.css("color", "inherit").text(payload.subject);
			$alert = $.bootstrapGrowl($message, {
				type: payload.severity ? payload.severity.toLowerCase() : "info",
				delay: 0
			});
			$alert.data("payload", payload);
			
			// Show details in a modal box
			$alert.one("click", ".message", function(e) {
				var payload = $(this).parent().data("payload");
				if (payload.body) {
					$alert.trigger("bg.close");
					bootbox.alert(payload.body);
				} else if (payload.link) {
					window.location.href = payload.link;
				}
				e.preventDefault();
				// We can postpone closing the alert until receiving the response from the server
				// setTimeout(function() { $this.alert("close"); }, 3000);
				// e.stopPropagation();
			});
		}
		if (typeof payload.messagesCounter != 'undefined') {
			var $counters = $('.unreadMessagesCounter');
			$counters.text(payload.messagesCounter);
			if (payload.messagesCounter > 0) {
				$counters.show();
			} else {
				$counters.hide();
			}
		}
		if (data.message == "ReloadPage" && $('div.needReloadPageAlert').length === 0) {
			for (var i in payload.links) {
				if (payload.links[i] == location.pathname) {
					$("<div class='alert alert-warning needReloadPageAlert'/>")
						.html("Необходимо <a href='javascript:location.reload()'>перезагрузить страницу</a>")
						.prependTo(".main");
					_updatePageElements = null;
					socket.onmessage = null;
				}
			}
		}
	};
	
	socket.onopen = function() {
		console.log(moment().format("L LT:ss") + ": Connection is open");
		if (reconnects > 0) {
			$.bootstrapGrowl(moment().format("L LT:ss") + ": Соединение установлено", {type: "info"});
		}
		reconnects++;
		if (pinger) {
			clearInterval(pinger);
			pinger = null;
		}
		pinger = setInterval(function() {
			if (socket.readyState !== WebSocket.OPEN) return;
			socket.send(JSON.stringify("Ping"));
		}, 5000);
	};
	socket.onerror = function() {
		console.log(moment().format("L LT:ss") + ": Connection error");
		$.bootstrapGrowl(moment().format("L LT:ss") + ": Ошибка подключения", {type: "danger"});
	};
	socket.onclose = function() {
		console.log(moment().format("L LT:ss") + ": Connection is closed");
		$.bootstrapGrowl(moment().format("L LT:ss") + ": Соединение потеряно", {type: "warning"});
	};
	
	// Close the connection when a user leaves the page
	$(window).unload(function() {
		socket.close();
	});
});