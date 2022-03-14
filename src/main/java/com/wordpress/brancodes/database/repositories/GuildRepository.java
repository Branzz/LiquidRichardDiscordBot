package com.wordpress.brancodes.database.repositories;

import com.wordpress.brancodes.database.model.Guild;
import com.wordpress.brancodes.database.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildRepository extends JpaRepository<Guild, Long> {

}
