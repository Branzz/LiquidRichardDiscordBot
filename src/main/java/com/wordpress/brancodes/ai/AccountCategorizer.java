package com.wordpress.brancodes.ai;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class AccountCategorizer {

    public double getPrediction(User user) {
        user.getAvatarUrl(); //
        user.getName(); // 2-32 chars includes non-ascii
        return 0.0;
    }

}
