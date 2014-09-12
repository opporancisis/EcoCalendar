$(document).ready(function() {

	$('#calendar').fullCalendar({
		header: {
			left: 'prev,next today',
			center: 'title',
			right: 'month,agendaWeek,agendaDay'
		},
		defaultDate: '2014-09-12',
		editable: true,
		eventLimit: true, // allow "more" link when too many events
		lang: 'ru',
		loading: function(bool) {
			$('#loading').toggle(bool);
		}
	});
	
});
