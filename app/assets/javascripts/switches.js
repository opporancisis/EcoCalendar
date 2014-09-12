$(document).ready(function() {
	$("input.js-switch").bootstrapSwitch({
		onColor: "danger",
		offColor: "default",
		onText: "ВКЛ",
		offText: "ВЫКЛ",
		onSwitchChange: function(e, state) {
			var $this = $(e.target);
			$.ajax({type: "POST", url: $this.data("url" + (state ? "On" : "Off")), context: e.target})
				.fail(function() {
					var $this = $(this);
					var state = $this.bootstrapSwitch("state");
					$this.bootstrapSwitch("state", !state, true);
				});
		}
	});
});