(function() {
	var app = window.app = {};
	
	app.settings = {
		bootstrapValidator: {
			feedbackIcons: {
				valid: 'glyphicon glyphicon-ok',
				invalid: 'glyphicon glyphicon-remove',
				validating: 'glyphicon glyphicon-refresh'
			},
			excluded: [':disabled', ':hidden', ':not(:visible)'],
			submitButtons: 'input[type="submit"]',
			message: "Некорректное значение"
		},
		bootstrapDatepicker: {
			format : "dd.mm.yyyy",
			todayBtn : "linked",
			language : "ru",
			autoclose : true,
			todayHighlight : true
		}
	};
	
	app.ui = {
		dialogForm: function(url, setup, options) {
			options = $.isPlainObject(setup) ? setup : options || {}; 
			setup = $.isFunction(setup) ? setup : $.noop;
			
			return $.get(url)
				.done(function(data) {
					var doc = $($.parseHTML(data));
					var title = doc.find("h1").text();
					var dialogContent = doc.find(".form-horizontal");
					var dialog = bootbox.dialog({
						title: title,
						message: dialogContent
					});
					
					if (setup(dialog) === false) return;
					
					if (!options.error) {
						options.error = function(xhr) {
							try {
								var data = JSON.parse(xhr.responseText);
								$("form").find(".has-error").removeClass("has-error");
								$("form").find(".help-block").remove();
								for (var field in data.errors) {
									var messages = data.errors[field];
									var input = $("[name='" + field + "']");
									input.closest(".form-group").addClass("has-error");
									/* jshint loopfunc: true */
									$("<div class='help-block'/>").html(messages.map(function(m){
										return "<span class='glyphicon glyphicon-exclamation-sign'></span>&nbsp;" + m;
									}).join("<br/>")).insertAfter(input);
								}
							} catch (e) {
								console.log("Could not handle ajax form error");
								console.error(e);
							}
						};
					}
					dialog.find("form").ajaxForm(options);
				});
		},
		dynamicOptions: function() {
			var select = $(this);
			var button = select.siblings(".add-new-item");
			var createOption = function(data) {
				return $("<option/>").text(data.name).val(data.id);
			};
			
			button.click(function(event) {
				event.preventDefault();
				app.ui.dialogForm(button.data("form-url"), function(dialog) {
					dialog.find("select").multiselect().each(app.ui.dynamicOptions);
				}, {
					url: button.data("submit-url"),
					dataType: "json",
					success: function(data, status, xhr, form) {
						createOption(data).appendTo(select);
						select.multiselect("rebuild").multiselect("select", data.id);
						form.closest(".modal").modal("hide");
					}
				});
			});
		}
	};
})();