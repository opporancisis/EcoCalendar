(function(global) {
	//
	// Plug-ins specific configuration
	//
	
	// Bootbox.js (http://bootboxjs.com/)
	if (global.bootbox) {
		bootbox.setDefaults({locale: "ru"});
	}
	
	// Bootstrap-growl (http://ifightcrime.github.io/bootstrap-growl/)
	if ($.bootstrapGrowl) {
		$.extend($.bootstrapGrowl.default_options, {
			ele: "body",	// which element to append to
			type: "info",	// (null, 'info', 'danger', 'success')
			offset: {from: "top", amount: 20},	// 'top', or 'bottom'
			align: "right",	// ('left', 'right', or 'center')
			width: 350,		// (integer, or 'auto')
			delay: 4000,	// Time while the message will be displayed. It's not equivalent to the *demo* timeOut!
			allow_dismiss: true,	// If true then will display a cross to close the popup.
			stackup_spacing: 10,	// spacing between consecutively stacked growls.
			autoshrink: true		// whether to shrink empty space after closing a message
		});
	}
	
	// Bootstrap Multiselect (http://davidstutz.github.io/bootstrap-multiselect/)
	if ($.fn.multiselect) {
		$.extend($.fn.multiselect.Constructor.prototype.defaults, {
			numberDisplayed: 1,
			selectAllText: 'Выбрать всё',
			filterPlaceholder: 'Поиск',
			nonSelectedText: 'Ничего не выбрано',
			nSelectedText: 'пункт(ов) выбрано'
		});
	}

	//
	// Global initialization for every page
	//
	
	$(document).ready(function() {
		
		// Custom select
		$("select.form-control").multiselect();
		
		// Bootstrap tooltips
		$("[title]").tooltip();
	});
	
	// Close bootbox dialog on Esc
	$(document).on("escape.close.bb", function(event) {
		$(event.target).modal("hide");
	});
	
	// Monkey patch ajax call to support content negotiation in Play
	(function() {
		var _ajax = $.ajax;
		$.ajax = function(url, options) {
			options = $.isPlainObject(url) ? url : options || {};
			if (options.dataType === "json") {
				options.accepts = { "*": "application/json" };
				delete options.dataType;
			}
			return _ajax.apply($, arguments);
		};
	})();
})(window);