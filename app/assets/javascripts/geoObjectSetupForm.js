$(document).ready(function() {

	var myMap, pageModel;

	pageModel = new PageModel();
	ko.applyBindings(pageModel);

	function PageModel() {
		var self = this;
		self.latitude = ko.observable(initialLatitude);
		self.longitude = ko.observable(initialLongitude);
		self.zoom = ko.observable(initialZoom);
		var mapCenterAndZoomListener = function() {
			myMap.setCenter([self.latitude(), self.longitude()], self.zoom());
		};
		self.latitude.subscribe(mapCenterAndZoomListener);
		self.longitude.subscribe(mapCenterAndZoomListener);
		self.zoom.subscribe(mapCenterAndZoomListener);
	}
}