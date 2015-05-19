$(function() {
	$(".table").on("click", "a[data-action='remove']", function(e) {
		$(this).one("ajaxSuccess.app", function() {
			$.each(["gray", "brown", "yellow", "green"], function(i, color) {
				if ($(e.delegateTarget)
						.find("tr")
						.not("[data-id='" + $(e.target).closest("tr").data("id") + "']")
						.find(".alert-" + color).length <= 0) {
					$(".legend .alert-" + color).hide();
				}
			});
			if ($(".legend .list-group-item:visible").length <= 0) {
				$(".legend").hide();
			}
		});
	});
});