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
				.done(function(data, status, jqxhr) {
					var doc = $($.parseHTML(data));
					var title = doc.find("h1").text();
					var dialogContent = doc.find(".form-horizontal");
					var dialog = bootbox.dialog({
						title: title,
						message: dialogContent
					});
					
					if (setup(dialog) === false) return;
					
					dialog.find("form").ajaxForm(options);
				});
		}
	};
})();