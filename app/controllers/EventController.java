package controllers;

import java.time.LocalDate;
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
import models.geo.Country;
import models.geo.GeoCoords;
import models.message.Message;
import models.organization.Organization;
import models.user.User;
import play.data.Form;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import utils.DatesInterval;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.fasterxml.jackson.databind.JsonNode;

import controllers.EventController.ExportForm;
import controllers.helpers.ContextAugmenter;
import controllers.helpers.ContextAugmenterAction;

@ContextAugmenter
public class EventController extends Controller {

	private static final Form<Event> EDIT_FORM = Form.form(Event.class);
	private static final Form<GeoCoords> COORDS_FORM = Form.form(GeoCoords.class);
	private static final Form<ExportForm> EXPORT_FORM = Form.form(ExportForm.class);

	private static Result list(Form<DatesInterval> intervalForm) {
		DatesInterval f = intervalForm.get();
		List<Event> events = Event.find.query().where().ge("days.date", f.from)
				.le("days.date", f.till).findList();
		return ok(views.html.event.listEvents.render(events, intervalForm));

	}

	public static Result list() {
		Application.noCache(response());
		return list(DatesInterval.fill(new DatesInterval()));
	}

	public static Result doChangeInterval() {
		Form<DatesInterval> filledForm = DatesInterval.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.listEvents.render(Collections.<Event> emptyList(),
					filledForm));
		}
		return list(filledForm);
	}

	@SubjectPresent
	public static Result edit(long id) {
		Event event = Event.find.byId(id);
		if (event == null) {
			return Application.notFoundObject(Event.class, id);
		}
		event.initTimetableJson();
		List<GrandEvent> grandEvents = getActualGrandEvents();
		if (event.parent != null && !grandEvents.contains(event.parent)) {
			// grandEvents.
		}
		event.coords.country = event.coords.city.country;
		return ok(views.html.event.editEvent.render(EDIT_FORM.fill(event),
				COORDS_FORM.fill(event.coords), event, getActualGrandEvents(),
				EventTag.findAllOrdered(), Organization.find.all(), Country.all()));
	}

	@SubjectPresent
	public static Result doEdit(long id) {
		Form<Event> filledForm = EDIT_FORM.bindFromRequest();
		Form<GeoCoords> filledCoords = COORDS_FORM.bindFromRequest();
		if (filledForm.hasErrors() || filledCoords.hasErrors()) {
			Event event = Event.find.byId(id);
			if (event == null) {
				return Application.notFoundObject(Event.class, id);
			}
			return badRequest(views.html.event.editEvent.render(filledForm, filledCoords, event,
					getActualGrandEvents(), EventTag.findAllOrdered(), Organization.find.all(),
					Country.all()));
		}
		Event event = filledForm.get();
		GeoCoords coords = filledCoords.get();
		event.updateWithTimetableAndCoords(id, coords);
		return redirect(routes.EventController.list());
	}

	@SubjectPresent
	public static Result create() {
		return ok(views.html.event.editEvent.render(EDIT_FORM, COORDS_FORM, null,
				getActualGrandEvents(), EventTag.findAllOrdered(), Organization.find.all(),
				Country.all()));
	}

	private static List<GrandEvent> getActualGrandEvents() {
		return GrandEvent.find.query().where().gt("endDate", LocalDate.now()).order("startDate")
				.findList();
	}

	@SubjectPresent
	public static Result doCreate() {
		Form<Event> filledForm = EDIT_FORM.bindFromRequest();
		Form<GeoCoords> filledCoords = COORDS_FORM.bindFromRequest();
		if (filledForm.hasErrors() || filledCoords.hasErrors()) {
			return badRequest(views.html.event.editEvent.render(filledForm, filledCoords, null,
					getActualGrandEvents(), EventTag.findAllOrdered(), Organization.find.all(),
					Country.all()));
		}
		User user = ContextAugmenterAction.getLoggedUser();
		Event event = filledForm.get();
		event.author = user;
		event.published = user.hasEnoughPowerToPublishEvents();
		event.coords = filledCoords.get();
		if (event.parent != null) {
			// TODO: notify creator of grand event about new event
			// TODO: take action upon GE settings: include sub-event
			// automatically, or let GE-creator review (post or pre moderation)
		}
		event.save();
		if (!event.published) {
			// TODO: send message to whom?
		}
		return redirect(routes.EventController.list());
	}

	// TODO: let users to complain about events. each complain will temporarily
	// reduce karma, but karma reduction will be compensated after complaint
	// success check.
	public static Result complain() {
		return TODO;
	}

	public static Result exportEvents() {
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
		LocalDate endDate = null;
		for (Event event : events) {
			LocalDate efd = event.firstDay().date;
			LocalDate eld = event.lastDay().date;
			DatesInterval interval = new DatesInterval(efd, eld);
			List<Event> evs = map.get(interval);
			if (evs == null) {
				evs = new ArrayList<>();
				map.put(interval, evs);
			}
			evs.add(event);
			if (startDate == null) {
				startDate = efd;
				endDate = eld;
				continue;
			}
			if (startDate.isAfter(efd)) {
				startDate = efd;
			}
			if (endDate.isBefore(eld)) {
				endDate = eld;
			}
		}
		return ok(views.html.event.export.render(map, startDate, endDate, lang()));
	}

	@Transactional
	public static Result removeMany() {
		return processMessages((event) -> {
			event.delete();
			return null;
		});
	}

	private static Result processMessages(Function<Event, Void> handler) {
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

	public static class ExportForm {
		public List<Event> events;
	}
}
