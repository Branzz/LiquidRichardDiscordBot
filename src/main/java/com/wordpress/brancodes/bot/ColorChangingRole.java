package com.wordpress.brancodes.bot;

import com.wordpress.brancodes.main.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.managers.RoleManager;

import java.awt.*;
import java.util.concurrent.TimeUnit;
public class ColorChangingRole {

	private final RoleManager roleManager;
	private final Color[] colors;
	private final long delay;
	private int index;

	/**
	 * @param delay in milliseconds
	 */
	public ColorChangingRole(long roleID, long delay, Color[] colors) {
		this(Main.getBot().getJDA(), roleID, delay, colors);
	}

	public ColorChangingRole(JDA jda, long roleID, long delay, Color[] colors) {
		this.colors = colors;
		this.delay = delay;
		roleManager = jda.getRoleById(roleID).getManager();
		index = 0;
	}

	public void start() {
		iterate();
	}

	private void iterate() {
		roleManager.setColor(colors[index = (index + 1) % colors.length]).queueAfter(delay, TimeUnit.MILLISECONDS, s -> iterate());
	}

}
