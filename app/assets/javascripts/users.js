(function() {
	function UserModel(chosenRoles) {
		var self = this;
		self.assignedRoles = ko.observableArray(chosenRoles);
		
		self.hasRoles = function(/* role... */) {
			for (var i = 0; i < arguments.length; i++) {
				if (~self.assignedRoles.indexOf(arguments[i])) return true;
			}
			return false;
		};
	}
	
	$(document).ready(function() {
		// Dynamically set bindings
		var select = $("select#roles").attr("data-bind", "selectedOptions: assignedRoles");
		var chosenRoles = select.find("option:selected").map(function(i, el) { return $(el).val(); });
		
		// Kick off
		ko.applyBindings(new UserModel(chosenRoles));
	});
})();