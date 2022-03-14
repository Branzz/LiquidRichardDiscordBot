package com.wordpress.brancodes.database.model;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {

	@Id
	@SequenceGenerator(
			name = "user_sequence",
			sequenceName = "user_sequence",
			allocationSize = 1
	)
	@GeneratedValue(
			strategy = GenerationType.SEQUENCE,
			generator = "user_sequence"
	)
	@Column(name = "id", nullable = false)
	private long id;

	@Column(unique = true)
	private long disc_id;

	// @JoinTable(name = "mods",
	// 		joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
	// 		inverseJoinColumns = @JoinColumn(name = "guild_id", referencedColumnName = "id"))
	@ManyToMany(mappedBy = "mods", fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
	private Set<Guild> moddedGuilds = new LinkedHashSet<>();

	public User() {
	}

	public User(long disc_id) {
		this.disc_id = disc_id;
	}

	public Set<Guild> getModdedGuilds() {
		return moddedGuilds;
	}

	public void addGuild(Guild guild) {
		moddedGuilds.add(guild);
	}

	public long getID() {
		return id;
	}

	private long getDiscId() {
		return disc_id;
	}

	@Override
	public String toString() {
		return "User#" + getID() + ":" + getDiscId();
	}

}
