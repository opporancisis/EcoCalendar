
$(document).ready(function() {

	var myMap, pageModel;

	pageModel = new PageModel();
	ko.applyBindings(pageModel);

	$("form.form-horizontal").bootstrapValidator(app.settings.bootstrapValidator);

	function PageModel() {
		var self = this;
		self.countryCity = new CountryCityModel();
		self.countryCity.selectedCountry.subscribe(function(id) {
			var start = new Date().getTime();
			if (typeof id != 'undefined') {
				for (var i = 0; i < self.countryCity.availableCountries.length; i++) {
					var country = self.countryCity.availableCountries[i];
					if (country.id === id) {
						self.countryCity.availableCities.removeAll();
						country.cities.forEach(function(item) {
							self.countryCity.availableCities.push(item);
						});
						if (myMap) {
							myMap.setCenter([country.centerLatitude, country.centerLongitude],
									country.defaultZoom);
						}
						break;
					}
				}
			} else {
				// TODO: destroy map?
			}
			console.log("selectedCountry.subscribe: " + (new Date().getTime() - start));
		});
		self.countryCity.selectedCity.subscribe(function(id) {
			var start = new Date().getTime();
			if (!myMap) {
				return;
			}
			if (typeof id != 'undefined') {
				var country = self.countryCity.availableCountries[self.countryCity.selectedCountry() - 1];
				for (var i = 0; i < country.cities.length; i++) {
					var city = country.cities[i];
					if (city.id === id) {
						myMap.setCenter([city.centerLatitude, city.centerLongitude],
								city.defaultZoom);
						break;
					}
				}
			}
			console.log("selectedCity.subscribe: " + (new Date().getTime() - start));
		});
		self.useAuthorContactInfo = ko.observable(useAuthorContactInfo);
		self.moreGeneralSettings = ko.observable(moreGeneralSettings);
		self.useContactInfo = ko.observable(useContactInfo);
		self.extendedGeoSettings = ko.observable(extendedGeoSettings);
		self.extendedGeoSettings.subscribe(function(extendedOn) {
			var start = new Date().getTime();
			if (extendedOn) {
				if (typeof ymaps == 'undefined') {
					bootbox.alert(Messages("error.yandex.maps"));
				} else {
					ymaps.ready(self.initMap);
				}
			} else {
				myMap.destroy();
			}
			console.log("extendedGeoSettings.subscribe: " + (new Date().getTime() - start));
		});
		self.points = ko.observableArray(geoPoints);
		self.initMap = function() {
//		if (navigator.geolocation) {
//	        navigator.geolocation.getCurrentPosition(function(position) {
//	        	realInit(position.coords.latitude, position.coords.longitude);
//	        });
//	    } else {
//        	realInit(55.76, 37.64);
//	    }
			
			var cityGeoData = self.countryCity.countriesById[pageModel.countryCity.selectedCountry()][pageModel.countryCity.selectedCity()];
			realInit(cityGeoData.la, cityGeoData.lo, cityGeoData.z);
			function realInit(latitude, longitude, zoom) {
				myMap = new ymaps.Map("map", {
					center: [latitude, longitude],
					zoom: zoom
				});
				
				if (geoPoints.length == 1) {
					myMap.setCenter([geoPoints[0], getPoints[1]], zoom);
				} else if (geoPoints.length > 1) {
					var maxLo = -180, minLo = 180, maxLa = -90, minLa = 90;
					for (var i = 0; i < geoPoints.length; i++) {
						var obj = geoPoints[i];
						var point = new ymaps.Placemark([obj.latitude, obj.longitude], {}, {
							draggable: true
						});
						myMap.geoObjects.add(point);
						if (maxLa < obj.latitude) {
							maxLa = obj.latitude;
						}
						if (minLa > obj.latitude) {
							minLa = obj.latitude;
						}
						if (maxLo < obj.longitude) {
							maxLo = obj.longitude;
						}
						if (minLo > obj.longitude) {
							minLo = obj.longitude;
						}
					}
					myMap.setBounds([maxLa, minLo], [minLa, maxLo], {
						checkZoomRange: true,
						callback: function(err) {
							if (err) {
								console.log(err);
								// Не удалось показать заданный регион
								// ...
							}
						}
					});
				}
				myMap.events.add(['click'], function (e) {
					// Получение координат щелчка
					var coords = e.get('coords');
					console.log('coords: ' + coords.join(', '));
					if (e.get('type') == 'click') {
//	    		    if (point) {
//	    		    	myMap.geoObjects.remove(point);
//	    		    }
						newPointDialog(coords[0], coords[1]);
						var point = new ymaps.Placemark(coords, {}, {
							draggable: true
						});
						point.events.add("click", function(event) {
							console.log(event);
						});
						myMap.geoObjects.add(point);
					}
//    		    pageModel.latitude(coords[0]);
//    		    pageModel.longitude(coords[1])
				});
			}
			
			function newPointDialog(latitude, longitude) {
				var box = bootbox.dialog({
					title: Messages("label.event.new.map.point.adding"),
					message: '<div class="row">  ' +
					'<div class="col-md-12"> ' +
					'<form class="form-horizontal" onsubmit="return false;"> ' +
					'<div class="form-group"> ' +
					'<label class="col-md-2 control-label" for="pointComment">' +
					Messages('label.comment') + '</label> ' +
					'<div class="col-md-10"> ' +
					'<input id="pointComment" name="pointComment" type="text" placeholder="' +
					Messages('label.comment') + '" class="form-control input-md"> ' +
					'<span class="help-block">' + Messages("label.event.map.point.comment.tip") +
					'</span> </div> </div> </form> </div>  </div>',
					buttons: {
						cancel: {
							label: Messages("label.do.cancel")
						},
						success: {
							className: "btn-success",
							label: Messages("label.do.add"),
							callback: function() {
								var comment = $('#pointComment').val();
								if (comment === "") return false;
								var pointData = {
										comment: comment,
										latitude: latitude,
										longitude: longitude
								};
								pageModel.points.push(pointData);
							}
						}
					}
				});
				box.bind('shown.bs.modal', function(){
					box.find("input").focus();
				});
			}
			
			function changePointDialog(title) {
				var box = bootbox.dialog({
					title: Messages("label.event.map.point.changing"),
					message: '<div class="row">  ' +
					'<div class="col-md-12"> ' +
					'<form class="form-horizontal"> ' +
					'<div class="form-group"> ' +
					'<label class="col-md-2 control-label" for="pointComment">' +
					Messages('label.comment') + '</label> ' +
					'<div class="col-md-10"> ' +
					'<input id="pointComment" name="pointComment" type="text" value="' + title +
					'" placeholder="' +
					Messages('label.comment') + '" class="form-control input-md"> ' +
					'<span class="help-block">' + Messages("label.event.map.point.comment.tip") +
					'</span> </div> ' +
					'</div> </form> </div>  </div>',
					buttons: {
						cancel: {
							label: Messages("label.do.cancel")
						},
						remove: {
							className: "btn-danger",
							label: Messages("label.do.remove"),
							callback: function() {
								console.err('here must go remove action!');
							}
						},
						success: {
							className: "btn-success",
							label: Messages("label.do.apply"),
							callback: function() {
								var comment = $('#pointComment').val();
								if (comment === "") return false;
								console.err('here must go change comment action');
							}
						}
					}
				});
				box.bind('shown.bs.modal', function(){
					box.find("input").focus();
				});
			}
		}
	}


});
