$(document).ready(function() {
	var table = $("#messages");
	var checkboxes = table.find("td:nth-child(1)>:checkbox");
	
	$("#checkbox-toggle").click(function() {
		var state = checkboxes.filter(":checked").length > 0 ? false : true;
		checkboxes.prop("checked", state).trigger("change");
	});
	
	$("td:nth-child(1)", table).change(function() {
		var rm = $("#remove");
		if (checkboxes.filter(":checked").length > 0) rm.show(); 
		else rm.hide();
	});
	
	$("#remove").click(function() {
		var selected = checkboxes.filter(":checked");
		
		bootbox.confirm("Вы уверены что хотите удалить " +
				selected.length + " сообщение(ий)?", function(ok) {
			if (!ok) return;
			
			var data = selected.map(function() {
				return $(this).closest("[data-id]").data("id");
			}).toArray();
			$("#messages").trigger("updateRows", [false, null]);
			
			$.ajax({
					type: "POST",
					url: "/account/message/removeMany",
					data: JSON.stringify(data),
					dataType: "json",
					contentType: "application/json"})
				.then(function() {	
					$.each(data, function(i, id) {
						table.find("[data-id='" + id + "']").remove();
					});
				});
		});
	});
});