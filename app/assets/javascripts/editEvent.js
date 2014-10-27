
$(document).ready(function() {

	var myMap, point, _cal, pageModel;

	pageModel = new PageModel();
	ko.applyBindings(pageModel);

	if (typeof ymaps == 'undefined') {
		bootbox.alert(Messages("error.yandex.maps"));
	} else {
		ymaps.ready(initMap);
	}

	initCalendar();
	
	$('input[type="submit"]').click(function() {
		var data = _cal.fullCalendar('clientEvents');
		if (!data.length) {
			bootbox.alert(Messages("error.need.program.items"));
			return false;
		}
		if (!point) {
			bootbox.alert(Messages("error.need.map.point"));
			return false;
		}
		var events = [];
		for (var i in data) {
			var d = data[i];
			events[i] = {
				start: d.start.format(),
				end: d.end.format(),
				title: d.title
			};
		}
		pageModel.timetable(JSON.stringify(events));
	});

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
		self.country = ko.observable();
		self.city = ko.observable();
		self.availableCountries = ko.observableArray(countriesMap);
		self.selectedCountry = ko.observable(initialCountry);
		self.availableCities = ko.observableArray([]);
		self.selectedCity = ko.observable(initialCity);
		self.selectedCountry.subscribe(function(id) {
			if (typeof id != 'undefined') {
				for (var i = 0; i < countriesMap.length; i++) {
					var country = countriesMap[i];
					if (country.id === id) {
						self.availableCities.removeAll();
						country.cities.forEach(function(item) {
							self.availableCities.push(item);
						});
						if (myMap) {
							myMap.setCenter([country.centerLatitude, country.centerLongitude],
									country.defaultZoom);
						}
						break;
					}
				}
			} else {
				// do nothing?
			}
		});
		self.selectedCity.subscribe(function(id) {
			if (!myMap) {
				return;
			}
			if (typeof id != 'undefined') {
				var country = countriesMap[self.selectedCountry() - 1];
				for (var i = 0; i < country.cities.length; i++) {
					var city = country.cities[i];
					if (city.id === id) {
						myMap.setCenter([city.centerLatitude, city.centerLongitude],
								city.defaultZoom);
						break;
					}
				}
			}
		});
		self.latitude = ko.observable(initialLatitude);
		self.longitude = ko.observable(initialLongitude);
		self.timetable = ko.observable();
		self.useAuthorNameAndPhone = ko.observable(true);
		
	}

	function newItemDialog(start, end) {
		bootbox.dialog({
			title: "Создание пункта программы",
			message: '<div class="row">  ' +
            '<div class="col-md-12"> ' +
            '<form class="form-horizontal" onsubmit="return false;"> ' +
            '<div class="form-group"> ' +
            '<label class="col-md-4 control-label" for="itemTitle">Заголовок</label> ' +
            '<div class="col-md-4"> ' +
            '<input id="itemTitle" name="itemTitle" type="text" placeholder="Заголовок" class="form-control input-md"> ' +
            '<span class="help-block">заголовок пункта программы</span> </div> ' +
            '</div> </form> </div>  </div>',
			buttons: {
				cancel: {
					label: "Отмена"
				},
				success: {
					className: "btn-success",
					label: "Применить",
					callback: function() {
						var title = $('#itemTitle').val();
						if (title === "") return false;
						var eventData = {
								title: title,
								start: start,
								end: end
						};
						_cal.fullCalendar('renderEvent', eventData, true); // stick? = true
					}
				}
			}
		});
		$('#itemTitle').focus();
	}
	
	function changeItemDialog(calEvent) {
		bootbox.dialog({
			title: "Изменение пункта программы",
			message: '<div class="row">  ' +
            '<div class="col-md-12"> ' +
            '<form class="form-horizontal"> ' +
            '<div class="form-group"> ' +
            '<label class="col-md-4 control-label" for="itemTitle">Заголовок</label> ' +
            '<div class="col-md-4"> ' +
            '<input id="itemTitle" name="itemTitle" type="text" value="' + calEvent.title +
            '" placeholder="Заголовок" class="form-control input-md"> ' +
            '<span class="help-block">заголовок пункта программы</span> </div> ' +
            '</div> </form> </div>  </div>',
			buttons: {
				cancel: {
					label: "Отмена"
				},
				remove: {
					className: "btn-danger",
					label: "Удалить",
					callback: function() {
						_cal.fullCalendar('removeEvents', calEvent.id);
					}
				},
				success: {
					className: "btn-success",
					label: "Применить",
					callback: function() {
						var title = $('#itemTitle').val();
						if (title === "") return false;
						calEvent.title = title;
						_cal.fullCalendar('rerenderEvents');
					}
				}
			}
		});
		$('#itemTitle').focus();
	}
	
	function initCalendar() {
		var events = [];
		if (initialTimetable.length > 0) {
			events = JSON.parse($('<div/>').html(initialTimetable).text());
		}
		_cal = $('#calendar').fullCalendar({
			header: {
				left: 'prev,next today',
				center: 'title',
				right: 'month,agendaWeek,agendaDay'
			},
			events: events,
			weekNumbers: true,
			editable: true,
			eventLimit: true, // allow "more" link when too many events
			lang: 'ru',
			selectable: true,
			select: function(start, end, jsEvent, view) {
				if (view.name === "month") {
					if (86400000 < end - start) {
						_cal.fullCalendar( 'changeView', 'agendaWeek');
					} else {
						_cal.fullCalendar( 'changeView', 'agendaDay');
					}
					_cal.fullCalendar( 'gotoDate', moment.unix(start / 1000));
					return;
				} else if (view.name === "agendaWeek") {
					var startMoment = moment.unix(start / 1000);
					var endMoment = moment.unix(end / 1000);
					if (startMoment.format('DD') != endMoment.format('DD')) {
						_cal.fullCalendar('unselect');
						return;
					}
				}
				newItemDialog(start, end);
			},
			eventClick: function(calEvent) {
				changeItemDialog(calEvent);
			}
		});
		
	}
	function initMap() {
		var zoom = 10;
		if (navigator.geolocation) {
	        navigator.geolocation.getCurrentPosition(function(position) {
	        	realInit(position.coords.latitude, position.coords.longitude);
	        });
	    } else {
        	realInit(55.76, 37.64);
	    }

		function realInit(latitude, longitude) {
    	    myMap = new ymaps.Map("map", {
    	        center: [latitude, longitude],
    	        zoom: zoom
    	    });
    	    var lat = pageModel.latitude(), lng = pageModel.longitude();
    	    if (lat && lng) {
    		    point = new ymaps.Placemark([lat, lng], {}, {
    	    	            draggable: true
    		            });
    		    myMap.geoObjects.add(point);
				myMap.setCenter([lat, lng], zoom);
    	    }
    		myMap.events.add(['click'], function (e) {
    		    // Получение координат щелчка
    		    var coords = e.get('coords');
    		    console.log('coords: ' + coords.join(', '));
    		    if (e.get('type') == 'click') {
	    		    if (point) {
	    		    	myMap.geoObjects.remove(point);
	    		    }
	    		    point = new ymaps.Placemark(coords, {}, {
		    	        // TODO: how to properly handle point movement?    
	    		    	//draggable: true
		            });
	    		    myMap.geoObjects.add(point);
    		    }
    		    pageModel.latitude(coords[0]);
    		    pageModel.longitude(coords[1])
    		});
		}
	}
});
