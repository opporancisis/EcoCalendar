$(document).ready(function() {
	var select = $("#attachments");
	var addFile = $("<button/>")
		.addClass("btn btn-success add-new-item")
		.append($("<span/>").addClass("glyphicon glyphicon-plus"))
		.click(function(event) {
			event.preventDefault();
			app.ui.dialogForm(jsRoutes.controllers.FileController.doAdd().url, {
				dataType: "json",
				success: function(data, status, xhr, form) {
					$("<option/>").text(data.name).val(data.id).appendTo(select);
					select.multiselect("rebuild").multiselect("select", data.id);
					form.closest(".modal").modal("hide");
				}
			});
		});
	
	select.after(addFile);
});