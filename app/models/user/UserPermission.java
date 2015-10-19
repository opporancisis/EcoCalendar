package models.user;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.Model;
import be.objectify.deadbolt.core.models.Permission;

/**
 * Initial version based on work by Steve Chaloner (steve@objectify.be) for
 * Deadbolt2
 */
@Entity
public class UserPermission extends Model implements Permission {

	@Id
	public Long id;

	public String value;

	public static final Find<Long, UserPermission> find = new Find<Long, UserPermission>(){
	};

	public String getValue() {
		return value;
	}

	public static UserPermission findByValue(String value) {
		return find.where().eq("value", value).findUnique();
	}
}