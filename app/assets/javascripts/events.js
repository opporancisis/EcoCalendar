$(document).ready(function() {
	ko.applyBindings(new PageModel());

	function PageModel() {
		var self = this;
		self.events = ids;
		self.selectedEvents = ko.observableArray([]);
		self.allSelected = ko.computed(function() {
			return self.selectedEvents().length == self.events.length;
		});
		self.remove = function() {
			bootbox.confirm("Вы уверены что хотите удалить " +
					self.selectedEvents().length + " событие(ий)?", function(ok) {
				if (!ok) return;
				$.ajax({
						type: "POST",
						url: jsRoutes.controllers.EventController.removeMany().url,
						data: JSON.stringify(self.selectedEvents()),
						dataType: "json",
						contentType: "application/json"})
					.then(function() {	
						$(".table.events")
							.find("[data-id]:has(:checked)").remove()
							.trigger("updateRows", [false, null]);
					});
			});
		}
		self.toggleAll = function() {
			if (self.allSelected()) {
				self.selectedEvents.removeAll();
			} else {
				self.selectedEvents.removeAll();
				self.events.forEach(function(item) {
					self.selectedEvents.push(item);
				});
			}
		}
		self.exportEvents = function() {
			$('#exportForm').submit();
		}
	}
	
	function getSelectedMessages() {
		var table = $(".table.events>tbody");

		return {
			size: function() {
				return this.getElements().length;
			},
			getElements: function() {
				return table.find("[data-id]:has(:checked)");
			},
			getIdentifiers: function() {
				return this.getElements().map(function() {
					return $(this).data("id");
				}).toArray();
			}
		};
	}
	
});
