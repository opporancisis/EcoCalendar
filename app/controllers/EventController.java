package controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import models.event.Event;
import models.event.EventDayProgram;
import models.event.EventProgamItem;
import models.event.GrandEvent;
import models.event.tag.EventTag;
import models.geo.City;
import models.geo.Country;
import models.organization.Organization;
import play.data.Form;
import play.data.format.Formatters;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.ExpressionList;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import controllers.helpers.ContextAugmenter;

@ContextAugmenter
public class EventController extends Controller {

	private static final Form<Event> EDIT_FORM = Form.form(Event.class);
	private static final Form<EventsFilter> FILTER = Form.form(EventsFilter.class);

	public static Result list() {
		return ok(views.html.event.listEvents.render());
	}

	public static Result getEvents() {
		Form<EventsFilter> filledForm = FILTER.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(filledForm.errorsAsJson());
		}
		EventsFilter filter = filledForm.get();
		ExpressionList<Event> query = Event.find.query().where();
		if (filter.start != null) {
			query.ge("days.date", filter.start);
		}
		if (filter.end != null) {
			query.le("days.date", filter.end);
		}
		if (filter.city != null) {
			query.eq("coords.city", filter.city);
		} else if (filter.country != null) {
			query.eq("coords.city.country", filter.city);
		}

		if (!filter.organizations.isEmpty()) {
			query.disjunction();
			for (Organization org : filter.organizations) {
				query.eq("organizations", org);
			}
			query.endJunction();
		}
		if (!filter.tags.isEmpty()) {
			query.disjunction();
			for (EventTag tag : filter.tags) {
				query.eq("tags", tag);
			}
			query.endJunction();
		}
		List<Event> events = query.findList();
		ObjectNode res = Json.newObject();
		ArrayNode arr = res.putArray("events");
		for (Event event : events) {
			EventDayProgram firstDay = event.days.get(0);
			EventDayProgram lastDay = event.days.get(event.days.size() - 1);
			EventProgamItem firstItemFirstDay = firstDay.items.get(0);
			EventProgamItem lastItemLastDay = lastDay.items.get(lastDay.items.size() - 1);
			ObjectNode o = arr.addObject().put("id", event.id).put("name", event.name)
					.put("city", event.coords.city.name)
					.put("startDate", Formatters.print(firstDay.date))
					.put("endDate", Formatters.print(lastDay.date))
					.put("startTime", Formatters.print(firstItemFirstDay.start))
					.put("endTime", Formatters.print(lastItemLastDay.end));
			o.putObject("author").put("id", event.author.id).put("name", event.author.nick);
			o.putObject("parent").put("id", event.parent.id).put("name", event.parent.name);
			if (!event.organizations.isEmpty()) {
				ArrayNode orgs = o.putArray("organizations");
				for (Organization org : event.organizations) {
					orgs.addObject().put("id", org.id).put("name", org.name);
				}
			}
			if (!event.tags.isEmpty()) {
				ArrayNode tags = o.putArray("tags");
				for (EventTag tag : event.tags) {
					tags.addObject().put("id", tag.id).put("name", tag.name);
				}
			}
		}
		return ok(res);
	}

	public static Result edit(long id) {
		Event event = Event.find.byId(id);
		if (event == null) {
			return Application.notFoundObject(Event.class, id);
		}
		List<GrandEvent> grandEvents = getActualGrandEvents();
		if (event.parent != null && !grandEvents.contains(event.parent)) {
			// grandEvents.
		}
		return ok(views.html.event.editEvent.render(EDIT_FORM.fill(event), event,
				getActualGrandEvents(), EventTag.find.all(), Organization.find.all()));
	}

	public static Result doEdit(long id) {
		Form<Event> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			Event event = Event.find.byId(id);
			if (event == null) {
				return Application.notFoundObject(Event.class, id);
			}
			return badRequest(views.html.event.editEvent.render(filledForm, event,
					getActualGrandEvents(), EventTag.find.all(), Organization.find.all()));
		}
		Event event = filledForm.get();
		event.update(id);
		return redirect(routes.EventController.list());
	}

	public static Result create() {
		return ok(views.html.event.editEvent.render(EDIT_FORM, null, getActualGrandEvents(),
				EventTag.find.all(), Organization.find.all()));
	}

	private static List<GrandEvent> getActualGrandEvents() {
		return GrandEvent.find.query().where().gt("endDate", LocalDate.now()).order("startDate")
				.findList();
	}

	public static Result doCreate() {
		Form<Event> filledForm = EDIT_FORM.bindFromRequest();
		if (filledForm.hasErrors()) {
			return badRequest(views.html.event.editEvent.render(filledForm, null,
					getActualGrandEvents(), EventTag.find.all(), Organization.find.all()));
		}
		Event event = filledForm.get();
		event.save();
		return redirect(routes.EventController.list());
	}

	public static Result remove(long id) {
		Event event = Event.find.byId(id);
		if (event == null) {
			return Application.notFoundObject(Event.class, id);
		}
		event.delete();
		return ok();
	}

	public static class EventsFilter {

		public LocalDate start;

		public LocalDate end;

		public Country country;

		public City city;

		public List<Organization> organizations = Collections.emptyList();

		public List<EventTag> tags = Collections.emptyList();
	}
}
