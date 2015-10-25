package controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import models.event.Event;
import models.event.GrandEvent;
import models.event.tag.EventTag;
import models.geo.City;
import models.geo.Country;
import models.geo.GeoCoords;
import models.organization.Organization;
import models.user.User;

import org.hibernate.validator.constraints.URL;

import play.data.Form;
import play.data.validation.Constraints.Required;
import play.data.validation.ValidationError;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DatesInterval;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class EventController extends Controller {

	private static final Form<EventProps> EDIT_FORM = Form.form(EventProps.class);
	private static final Form<ExportForm> EXPORT_FORM = Form.form(ExportForm.class);
	private static final Form<FilterForm> FILTER = Form.form(FilterForm.class);

	private Result list(Form<DatesInterval> intervalForm) {
		DatesInterval f = intervalForm.get();
		List<Event> events = Event.find.query().where().ge("start", f.from).le("finish", f.till)
				.findList();
		return ok(views.html.event.listEvents.render(events, intervalForm));

	}

	public Result list() {
		Application.noCache(response());
		return list(DatesInterval.fill(new DatesInterval()));
	}

	public Result doChangeInterval() {
		Form<DatesInterval> filledForm = DatesInterval.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.listEvents.render(Collections.<Event> emptyList(),
					filledForm));
		}
		return list(filledForm);
	}

	@SubjectPresent
	public Result edit(Event event) {
		// List<GrandEvent> grandEvents = getActualGrandEvents();
		// if (event.parent != null && !grandEvents.contains(event.parent)) {
		// // grandEvents.
		// }
		EventProps eventProps = new EventProps(event);
		return ok(views.html.event.editEvent.render(EDIT_FORM.fill(eventProps), event,
		/* getActualGrandEvents(), */EventTag.findAllOrdered(), Organization.find.all(),
				Country.all()));
	}

	@SubjectPresent
	public Result doEdit(Event event) {
		Form<EventProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEvent.render(filledForm, event,
			/* getActualGrandEvents(), */EventTag.findAllOrdered(), Organization.find.all(),
					Country.all()));
		}
		// TODO: how will coords be populated?
		EventProps eventProps = filledForm.get();
		eventProps.updateEvent(event);
		event.update();
		// TODO: check that old coords will be deleted from DB after event
		// update
		return redirect(routes.EventController.list());
	}

	@SubjectPresent
	public Result add() {
		return ok(views.html.event.editEvent.render(EDIT_FORM, null, /*
																	 * getActualGrandEvents
																	 * (),
																	 */
				EventTag.findAllOrdered(), Organization.find.all(), Country.all()));
	}

	private static List<GrandEvent> getActualGrandEvents() {
		return GrandEvent.find.query().where().gt("endDate", LocalDate.now()).order("startDate")
				.findList();
	}

	@SubjectPresent
	public Result doAdd() {
		Form<EventProps> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEvent.render(filledForm, null,
			/* getActualGrandEvents(), */EventTag.findAllOrdered(), Organization.find.all(),
					Country.all()));
		}
		User user = ContextAugmenterAction.getLoggedUser();
		EventProps eventProps = filledForm.get();
		Event event = eventProps.createEvent();
		event.author = user;
		event.published = user.hasEnoughPowerToPublishEvents();
		// TODO: how we will populate coords?
		event.coords = null;
		// if (event.parent != null) {
		// TODO: notify creator of grand event about new event
		// TODO: take action upon GE settings: include sub-event
		// automatically, or let GE-creator review (post or pre moderation)
		// }
		event.save();
		if (!event.published) {
			// TODO: send message to whom?
		}
		return redirect(routes.EventController.list());
	}

	// TODO: let users to complain about events. each complain will temporarily
	// reduce karma, but karma reduction will be compensated after complaint
	// success check.
	public Result complain() {
		return TODO;
	}

	// TODO: form hash tags in exported text based on event tags
	public Result exportEvents() {
		Form<ExportForm> filledForm = EXPORT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			flash(Application.FLASH_ERROR_KEY, Messages.get("error.event.export.unknown.error"));
			return redirect(routes.EventController.list());
		}
		List<Event> events = filledForm.get().events;
		if (events == null || events.isEmpty()) {
			return badRequest("no events specified");
		}
		Map<DatesInterval, List<Event>> map = new TreeMap<>(new Comparator<DatesInterval>() {
			public int compare(DatesInterval i1, DatesInterval i2) {
				if (i1.from.isBefore(i2.from)) {
					return -1;
				} else if (i1.from.isAfter(i2.from)) {
					return 1;
				} else {
					assert i1.from.equals(i2.from);
					// place 1-day events at the top
					if (i1.from.equals(i1.till) && !i2.from.equals(i2.till)) {
						return -1;
					} else if (!i1.from.equals(i1.till) && i2.from.equals(i2.till)) {
						return 1;
					} else {
						return 0;
					}
				}
			}
		});
		LocalDate startDate = null;
		LocalDate finishDate = null;
		for (Event event : events) {
			DatesInterval interval = new DatesInterval(event.start.toLocalDate(),
					event.finish.toLocalDate());
			List<Event> evs = map.get(interval);
			if (evs == null) {
				evs = new ArrayList<>();
				map.put(interval, evs);
			}
			evs.add(event);
			if (startDate == null) {
				startDate = event.start.toLocalDate();
				finishDate = event.finish.toLocalDate();
				continue;
			}
			if (startDate.isAfter(event.start.toLocalDate())) {
				startDate = event.start.toLocalDate();
			}
			if (finishDate.isBefore(event.finish.toLocalDate())) {
				finishDate = event.finish.toLocalDate();
			}
		}
		return ok(views.html.event.export.render(map, startDate, finishDate, lang()));
	}

	@Transactional
	public Result removeMany() {
		return processMessages((event) -> {
			event.delete();
			return null;
		});
	}

	private Result processMessages(Function<Event, Void> handler) {
		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		}
		if (!json.isArray()) {
			return badRequest("root element must be array: " + json);
		}
		User user = ContextAugmenterAction.getLoggedUser();
		for (JsonNode elem : json) {
			long id = elem.asLong(-1);
			if (id == -1) {
				return badRequest("incorrect format. expected long, but get: " + elem);
			}
			Event event = Event.find.byId(id);
			// TODO: check user permissions to delete this event. should be
			// creator or admin or city moderator
			handler.apply(event);
		}
		return ok();
	}

	public Result getEventsJson() {
		// ArrayNode arr = Json.newObject().arrayNode();
		// for (EventDayProgram day : days) {
		// LocalDate date = day.date;
		// for (EventProgamItem item : day.items) {
		// arr.add(Json.newObject()
		// .put("start", LocalDateTime.of(date, item.start).toString())
		// .put("end", LocalDateTime.of(date, item.end).toString())
		// .put("title", item.description));
		// }
		// }
		// timetableJson = arr.toString();

		return TODO;
	}

	public Result calendar() {
		// just return UI. actual events will be fetched via ajax
		return ok(views.html.event.calendar.render(FILTER, Country.all(), /*
																		 * getActualGrandEvents
																		 * (),
																		 */
				EventTag.findAllOrdered()));
	}

	public Result map() {
		return TODO;
	}

	public Result view(Event event) {
		return ok(views.html.event.viewEvent.render(event));
	}

	public static class ExportForm {
		public List<Event> events;
	}

	public static class FilterForm {

		public Country country;

		public City city;

		// public GrandEvent parent;

		public List<EventTag> tags;

	}

	public static class EventProps {
		@Required
		public String name;

		@Required
		public String description;

		public List<EventTag> tags;

		@URL
		public String additionalInfoLink;

		public Boolean moreGeneralSettings;

		// public GrandEvent parent;

		public List<Organization> organizations;

		public Boolean useContactInfo;

		public Boolean useAuthorContactInfo;

		public String contactName;

		public String contactPhone;

		public String contactProfile;

		@Required
		public Country country;

		@Required
		public City city;

		public String address;

		public Boolean extendedGeoSettings;

		public List<GeoCoords> coords;

		@Required
		public LocalDate startDate;

		@Required
		public LocalTime startTime;

		@Required
		public LocalDate finishDate;

		@Required
		public LocalTime finishTime;

		public EventProps() {
			// no-op
		}

		public EventProps(Event event) {
			this.name = event.name;
			this.description = event.description;
			this.tags = event.tags;
			this.additionalInfoLink = event.additionalInfoLink;
			this.moreGeneralSettings = event.moreGeneralSettings;
			// this.parent = event.parent;
			this.organizations = event.organizations;
			this.useContactInfo = event.useContactInfo;
			this.useAuthorContactInfo = event.useAuthorContactInfo;
			this.contactName = event.contactName;
			this.contactPhone = event.contactPhone;
			this.contactProfile = event.contactProfile;
			this.country = event.city.country;
			this.city = event.city;
			this.address = event.address;
			this.extendedGeoSettings = event.extendedGeoSettings;
			this.coords = event.coords;
			this.startDate = event.start.toLocalDate();
			this.startTime = event.start.toLocalTime();
			this.finishDate = event.finish.toLocalDate();
			this.finishTime = event.finish.toLocalTime();
		}

		private void setFields(Event event) {
			event.name = this.name;
			event.description = this.description;
			event.tags = this.tags;
			event.additionalInfoLink = this.additionalInfoLink;
			event.moreGeneralSettings = this.moreGeneralSettings;
			// event.parent = this.parent;
			event.organizations = this.organizations;
			event.useContactInfo = this.useContactInfo;
			event.useAuthorContactInfo = this.useAuthorContactInfo;
			event.contactName = this.contactName;
			event.contactPhone = this.contactPhone;
			event.contactProfile = this.contactProfile;
			event.city = this.city;
			event.address = this.address;
			event.extendedGeoSettings = this.extendedGeoSettings;
			event.coords = this.coords;
			event.start = LocalDateTime.of(this.startDate, this.startTime);
			event.finish = LocalDateTime.of(this.finishDate, this.finishTime);
		}

		public void updateEvent(Event event) {
			setFields(event);
		}

		public Event createEvent() {
			Event event = new Event();
			setFields(event);
			return event;
		}

		// public List<ValidationError> validate() {
		// List<ValidationError> errors = null;
		// if (parent != null) {
		// if (parent.startDate.isAfter(startDate)) {
		// errors = new ArrayList<>();
		// errors.add(new ValidationError("", Messages.get(
		// "label.event.error.parentStartDateIsAfter", parent.startDate)));
		// }
		// if (parent.endDate.isBefore(finishDate)) {
		// if (errors == null) {
		// errors = new ArrayList<>();
		// }
		// errors.add(new ValidationError("", Messages.get(
		// "label.event.error.parentEndDateIsBefore", parent.endDate)));
		// }
		// }
		// return errors;
		// }

	}
}
