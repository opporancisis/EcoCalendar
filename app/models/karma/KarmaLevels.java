package models.karma;

public enum KarmaLevels {

	/**
	 * Новичок, только что зарегестрировался на сайте. Человек может всегда
	 * оставаться на этом уровне, просто следить за событиями. Если человек
	 * создает события, то они всегда премодерируются администратором или
	 * модератором данного города или модератором страны. При этом обязательно
	 * указывать контактный телефон, чтобы модераторы могли оперативно связаться
	 * и узнать дополнительную информацию о событии.
	 */
	NEWBIE(0),

	/**
	 * Разумный создатель событий. Человек уже создал несколько событий, которые
	 * прошли модерацию, и за которые получил увеличение кармы от модераторов
	 * или администраторов.
	 */
	SANE(100),

	CITY_MODERATOR(500),

	COUNTRY_MODERATOR(1000),

	;

	public final long level;

	private KarmaLevels(long level) {
		this.level = level;
	}
}
