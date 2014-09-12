package models.karma;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import models.user.User;
import play.db.ebean.Model;

/**
 * При создании пользователя (первой авторизации) карма = 0. С кармой < 50
 * события пользователя проходят премодерацию (администратором). При этом
 * модерируя администратор может прибавить кармы от 0 до 10. Например, ему
 * нравится событие, оно корректно оформленно, тогда он делает мероприятие
 * публичным (одобряет его) и прибавляет +10 в карму создателю. Если в описании
 * мероприятия есть неточности, но сделать его публичным уже можно, то +5 в
 * карму. Это для примера, но в конечном итоге все на субъективный взгляд
 * администратора. Если он считает ценным вклад создателя (созданное
 * мероприятие) и хочет, чтобы создатель быстрее набрал пороговую карму (50), то
 * он увеличивает ее на большое значение. Иначе - на меньшее.
 */
@Entity
public class Karma extends Model {

	private static final long serialVersionUID = 1L;

	@Id
	public Long id;

	public Long value;

	@OneToOne
	public User user;

	@OneToOne
	public KarmaHistory history;
}
