$(document).ready(function() {
	$.extend($.tablesorter.themes.bootstrap, {
		// these classes are added to the table. To see other table classes available,
		// look here: http://twitter.github.com/bootstrap/base-css.html#tables
		table      : 'table table-bordered',
		caption    : 'caption',
		header     : 'bootstrap-header', // give the header a gradient background
		footerRow  : '',
		footerCells: '',
		icons      : '', // add "icon-white" to make them white; this icon class is added to the <i> in the header
		sortNone   : 'bootstrap-icon-unsorted',
		sortAsc    : 'icon-chevron-up glyphicon glyphicon-chevron-up',     // includes classes for Bootstrap v2 & v3
		sortDesc   : 'icon-chevron-down glyphicon glyphicon-chevron-down', // includes classes for Bootstrap v2 & v3
		active     : '', // applied when column is sorted
		hover      : '', // use custom css here - bootstrap class may not override it
		filterRow  : '', // filter row class
		even       : '', // odd row zebra striping
		odd        : ''  // even row zebra striping
	});

	var table = $("table.js-tablesorter");
	var theaders = $("thead>tr>th", table);

	table.on("tablesorter-initialized", function(event, table) {
		// fix filter inputs appearance
		$(table).find(".tablesorter-filter").addClass("form-control");
	});

	var options = {
		// this will apply the bootstrap theme if "uitheme" widget is included
		// the widgetOptions.uitheme is no longer required to be set
		theme : "bootstrap",
		widthFixed : false,
		headerTemplate : '{content} {icon}',

		// widget code contained in the jquery.tablesorter.widgets.js file
		// use the zebra stripe widget if you plan on hiding any rows (filter
		// widget)
		widgets : [ "uitheme", "filter", "zebra", "columnSelector" ],
		widgetOptions : {
			// using the default zebra striping class name, so it actually isn't
			// included in the theme variable above
			// this is ONLY needed for bootstrap theming if you are using the
			// filter widget, because rows are hidden
			zebra : [ "even", "odd" ],

			// reset filters button
			filter_reset : ".reset",
			filter_childRows : true,

			// add custom selector elements to the filter row
			filter_formatter : {},
			// option added in v2.16.0
			filter_selectSource : {},
			
			filter_saveFilters: true,
			
			// column status, true = display, false = hide
			// disable = do not display on list
			columnSelector_columns : {},
			
			// toggle checkbox name
			columnSelector_mediaqueryName: "[Все]"
		}
	};

	$("thead th.js-select-filter", table).each(function(i, th) {
		// index of the column we setup select filter for
		var index = theaders.index(th);

		// formatting of the filter row
		options.widgetOptions.filter_formatter[index] = function($cell, indx){
			var input = $.tablesorter.filterFormatter.select2( $cell, indx, {
				match : true, // adds "filter-match" to header
				containerCssClass: "form-control"
				// cellText : 'Match: ',
				// width: '85%'
			});
			
			var optionsString = $(th).data("init-selection");
			if (optionsString.length > 0) {
				input.siblings("input.select2").val(optionsString);
				var options = optionsString.split(/,\s*/);
				input.siblings("input.select2").select2("val", options, true);
			}
			
			return input;
		};

		// dynamically extract source elements from the column (for an user to choose from)
		options.widgetOptions.filter_selectSource[index] = function(table, column) {
			var options = [];
			$("tbody>tr", table).each(function(i, el) {
				var item = $(el).children().eq(column).text().trim();
				if (item) options.push(item);
			});
			return options;
		};
	});
	
	$("thead th.unselectable", table).each(function() {
		var i = theaders.index(this);
		options.widgetOptions.columnSelector_columns[i] = "disable";
	});

	var tablesorter = table.tablesorter(options);

	tablesorter.tablesorterPager({
		// target the pager markup - see the HTML block below
		container : $(".ts-pager"),

		// target the pager page select dropdown - choose a page
		cssGoto : ".pagenum",

		// remove rows from the table to speed up the sort of large tables.
		// setting this to false, only hides the non-visible rows; needed if you
		// plan to add/remove rows with the pager enabled.
		removeRows : false,

		// output string - default is '{page}/{totalPages}';
		// possible variables: {page}, {totalPages}, {filteredPages},
		// {startRow}, {endRow}, {filteredRows} and {totalRows}
		output : '{startRow} - {endRow} / {filteredRows} ({totalRows})'
	});
	
	$(".column-selector")
		// bootstrap popover event triggered when the popover opens
		.on("shown.bs.popover", function () {
			// call this function to copy the column selection code into the popover
			$.tablesorter.columnSelector.attachTo( table, ".column-selector-popover");

			// popover reposition
			var offset = $(this).offset();
			offset.top += $(this).outerHeight() + $(".popover>.arrow").outerHeight();
			offset.left -= 40;
			$(".popover").offset(offset);
			
			// auto-cancel
			$(".column-selector").on("click.app.cs", function() {
				$(document).off(".app.cs");
			});
			$(".popover").on("click.app.cs", function(e) {
				e.stopImmediatePropagation();
			});
			$(document).on("click.app.cs", function(e) {
				if ($(e.target).is(".column-selector") ||
						$(e.target).is(".popover") ||
						$(e.target).closest(".column-selector").length > 0 ||
						$(e.target).closest(".popover").length > 0) return;
				e.stopPropagation();
				$(document).off(".app.cs");
				$(".column-selector").click();
			});
			$(document).on("keydown.app.cs", function(e) {
				if (e.keyCode !== 27) return;
				$(document).off(".app.cs");
				$(".column-selector").click();
			});
		})
		.popover({
			placement: "bottom",
			html: true, // required if content has HTML
			content: "<div class=\"column-selector-popover\"></div>",
			trigger: "click"
		});
});
