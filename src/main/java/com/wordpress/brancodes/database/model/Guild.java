package com.wordpress.brancodes.database.model;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;

@Entity
@Table(name = "guilds")
public class Guild {

/*
add fields?:
server nickname and pfp
 */
	@Id
	@Column(name = "id", nullable = false)
	private long id;

	@Column(unique = true)
	private long disc_id;

	@JoinTable(name = "mods",
			joinColumns = @JoinColumn(name = "guild_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private Set<User> mods = new java.util.LinkedHashSet<>();

	public Guild() {
	}

	public Guild(final long disc_id) {
		this.disc_id = disc_id;
	}

	public Set<User> getMods() {
		return mods;
	}

	@Override
	public String toString() {
		return "Guild#" + id + ":" + disc_id;
	}

	// public Set<String> getCensoredWords() { TODO
	//
	// }

	// public Set<String> getCustomCommands() { TODO
	//
	// }

}
