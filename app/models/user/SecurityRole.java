/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package models.user;

import java.text.ParseException;
import java.util.Locale;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.db.ebean.Model;
import be.objectify.deadbolt.core.models.Role;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityRole extends Model implements Role {

	private static final long serialVersionUID = 1L;

	static {
		Formatters.register(SecurityRole.class, new SecurityRoleFormatter());
	}

	@Id
	public Long id;

	public String roleName;

	public static final Finder<Long, SecurityRole> find = new Finder<>(Long.class,
			SecurityRole.class);

	@Override
	public String getName() {
		return roleName;
	}

	public static SecurityRole findByRoleName(String roleName) {
		return find.where().eq("roleName", roleName).findUnique();
	}

	@Override
	public String toString() {
		return roleName;
	}

	public static class SecurityRoleFormatter extends SimpleFormatter<SecurityRole> {
		@Override
		public SecurityRole parse(String text, Locale locale) throws ParseException {
			return SecurityRole.findByRoleName(text);
		}

		@Override
		public String print(SecurityRole t, Locale locale) {
			return t.roleName;
		}
	}
}