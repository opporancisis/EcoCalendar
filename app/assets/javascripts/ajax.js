// Unobtrusive handler for AJAX requests with optional confirmation.
//
// Data attributes:
//   method - HTTP method
//   action - action to perform e.g. remove, etc. (non-optional for click event)
//   url - request URI
//   confirm - confirmation message to show
//
// Usage:
// <a class="js-ajax" data-action="remove" data-method="POST"
//     data-url="/posts/1/remove" data-confirm="Are you sure?">Remove</a>

$(document).ready(function() {
	var preloader = $("<div/>")
		.addClass("modal-backdrop fade in")
		// spinner would be invisible if background is pure black
		.css({"background-color": "rgba(0,0,0,.6)", "opacity": ".9"})
		.hide()
		.appendTo("body");
	var spinner = new Spinner();
	
	$(document).on({
		ajaxStart: function() {
			preloader.show();
			spinner.spin(preloader.get(0));
		},
		ajaxSend: function(event, jqxhr, settings) {
			console.debug("[" + event.type + "] " + settings.type + " " + settings.url);
		},
		ajaxError: function(event, jqxhr, settings, error) {
			var message = "Ошибка при выполнении запроса";
			if (~jqxhr.getResponseHeader("Content-Type").indexOf("text/plain")) {
				message = jqxhr.responseText;
			}
			console.error("[" + event.type + "] " + error);
			console.error(jqxhr);
			console.error(settings);
			$.bootstrapGrowl(message, {type: "danger"});
		},
		ajaxStop: function() {
			spinner.stop();
			preloader.hide();
		}
	});
	
	$(window).on("ajaxRequest.app", function(e, options) {
		console.warn(e.type + " event is deprecated. Use direct calls.");
		options.context = e.target;
		$.ajax(options);
	});
	
	$(document).on("click", ".js-ajax", function(e) {
		var $this = $(this);
		var $item = $this.closest("tr");
		var action = $this.data("action");
		var confirm = $this.data("confirm");
		
		var options = {
			url: $this.data("url"),
			type: $this.data("method") || "POST",
			context: e.target
		};
		
		function doRequest() {
			$.ajax(options).done(function() {
				$this.triggerHandler("ajaxSuccess.app", arguments);
				if (action === "remove" && $item.length > 0) {
					var $table = $item.closest("table");
					if ($item.is("[data-id]")) {
						$item.siblings("[data-id='" + $item.data("id") + "']").addBack().remove();
					} else {
						$item.remove();
					}
					if ($table.length > 0 && $table.is(".tablesorter")) {
						$table.trigger("updateRows", [false]);
					}
				}
			});
		}

		if (!options.url) throw Error("No URL specified for this request");
		
		// Implicit call requires action to be defined
		if (!action && e.type === "click") return;

		if (confirm !== undefined) {
			bootbox.confirm(confirm, function(result) { if (!result) return; doRequest(); });
		} else {
			doRequest();
		}
		
		e.preventDefault();
	});
});