
$(document).ready(function() {
	
	var _cal, pageModel;

	initCalendar();
	
	pageModel = new PageModel();
	ko.applyBindings(pageModel);

	function PageModel() {
		for (var i = 0; i < countriesMap.length; i++) {
			countriesMap[i].cities.sort(function(a, b) {
				if (a.weight != null && b.weight != null) {
					return b.weight - a.weight;
				} else if (a.weight != null) {
					return -1;
				} else if (b.weight != null) {
					return 1;
				} else {
					return a.name > b.name ? 1 : (a.name < b.name ? -1 : 0);
				}
			});
		}
		var self = this;
		self.availableCountries = countriesMap;
		self.selectedCountry = ko.observable();
		self.availableCities = ko.observableArray([]);
		self.selectedCity = ko.observable();
		self.selectedCountry.subscribe(function(id) {
			var start = new Date().getTime();
			if (typeof id != 'undefined') {
				for (var i = 0; i < countriesMap.length; i++) {
					var country = countriesMap[i];
					if (country.id === id) {
						self.availableCities.removeAll();
						country.cities.forEach(function(item) {
							self.availableCities.push(item);
						});
						break;
					}
				}
			} else {
				// do nothing?
			}
			console.log("selectedCountry.subscribe: " + (new Date().getTime() - start));
		});
		self.availableGrandEvents = grandEvents;
		self.selectedGrandEvent = ko.observable();
		self.availableTags = eventTags;
		self.selectedTags = ko.observableArray([]);
		if(typeof(Storage) !== "undefined") {
		    // restore initial filter settings
			if (localStorage.country && countries[localStorage.country]) {
				self.selectedCountry(localStorage.country);
				if(localStorage.city && countries[localStorage.country][localStorage.city]) {
					self.selectedCity(localStorage.city);
				}
			}
			if(localStorage.parent && grandEventNames[localStorage.parent]) {
				self.selectedGrandEvent(localStorage.parent);
			}
			if(localStorage.tags) {
				var tags = localStorage.tags.split(",");
				for (var i = 0; i < tags.length; i++) {
					if (eventTagsNames[tags[i]]) {
						self.selectedTags.push(Number(tags[i]));
					}
				}
			}
		} else {
		    // Sorry! No Web Storage support..
		}
		self.submitFilter = function(formElement){
			if(typeof(Storage) !== "undefined") {
				var $form = $(formElement);
				localStorage.country = $form.find('select[name="country"]').val();
				localStorage.city = $form.find('select[name="city"]').val();
				localStorage.parent = $form.find('select[name="parent"]').val();
				localStorage.tags = $form.find('select[name="tags[]"]').val().join();
			}
			$('#collapseOne').collapse('hide');
		};
	}

	function initCalendar() {
		var events = [];
//		if (initialTimetable.length > 0) {
//			events = JSON.parse($('<div/>').html(initialTimetable).text());
//		}
		_cal = $('#calendar').fullCalendar({
			header: {
				left: 'prev,next today',
				center: 'title',
				right: 'month,agendaWeek,agendaDay'
			},
			events: events,
			weekNumbers: true,
			eventLimit: true, // allow "more" link when too many events
			lang: 'ru',
			eventClick: function(calEvent) {
				// TODO: open viewEvent action in separate window
			}
		});
		_cal.fullCalendar( 'changeView', 'basicWeek' );
		_cal.fullCalendar( 'changeView', 'month' );
	}
});