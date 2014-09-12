$(document).ready(function() {
	function reloadEvents() {
		jsRoutes.controllers.EventController
				.getEvents().ajax({
			success: function(responseData) {
				var data = '';
				for (var r in responseData.events) {
					var e = responseData.events[r];
					data += '<tr><td>' + e.name + '</td><td>' +
						e.startDay + ' ' + e.startTime + '</td><td>' +
						e.endDay + ' ' + e.endTime + '</td><td><a href="' +
						jsRoutes.controllers.UserController.details(e.author.id).absoluteURL() +
						'">' + e.author.name + '</a></td><td>';
					if (e.parent) {
						data += '<a href="' +
						jsRoutes.controllers.GrandEventController.details(e.parent.id).absoluteURL() +
						'">' + e.parent.name + '</a>';
					}
					data += '</td><td>' + e.city + '</td><td>';
					if (e.organizations) {
						for (var orgInd = 0; orgInd < e.organizations.length; orgInd++) {
							var org = e.organizations[orgInd];
							if (orgInd > 0) {
								data += ', ';
							}
							data += '<a href="' +
							jsRoutes.controllers.OrganizationController.details(org.id).absoluteURL() +
							'">' + org.name + '</a>';
						}
					}
					data += '</td><td>';
					if (e.tags) {
						for (var tagInd = 0; tagInd < e.tags.length; tagInd++) {
							var tag = e.tags[tagInd];
							if (tagInd > 0) {
								data += ', ';
							}
							data += '<a href="javascript:void(0);" onclick="alert(\'What should be shown after click on tag?\')">' +
								tag.name + '</a>';
						}
					}
					data += '</td><td><a class="btn btn-default btn-xs" href="' +
						jsRoutes.controllers.EventController.edit(event.id).absoluteURL() +
						'" title="' + Messages("label.do.edit") +
						'"><span class="glyphicon glyphicon-pencil"></span></a><a class="btn btn-default btn-xs js-ajax" title="' +
						Messages("label.do.remove") +
						'" data-action="remove" data-method="POST" data-url="' +
						jsRoutes.controllers.EventController.remove(event.id).absoluteURL() +
						'"data-confirm="' + Messages("label.are.you.sure.you.want.to.delete", event.name) +
						'"><span class="glyphicon glyphicon-remove"></span></a></td></tr>';
				}
				$(".events .js-tablesorter>tbody").append(data)
						.trigger('updateRows', [true, null]);
			},
			error: function(err) {
				$.error("Error: " + err);
			}
		});
	}

	reloadEvents();
});
