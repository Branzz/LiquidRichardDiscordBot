package com.wordpress.brancodes.main;

import com.wordpress.brancodes.messaging.reactions.ReactionManager;
import com.wordpress.brancodes.messaging.reactions.message.commands.Command;
import com.wordpress.brancodes.messaging.reactions.users.UserCategoryType;
import com.wordpress.brancodes.util.Resourcer;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
public class DocsGenerator {

	public static void main(String[] args) {
		generateMdDocs();
	}

	public static void generateMdDocs() {
		try (BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(Resourcer.getURL("docs.md").toURI())), StandardCharsets.UTF_8))) {
			fileWriter.flush();
			fileWriter.append("## Reactions and Commands Docs\n"
							  + "Reaction: something that can be checked and triggered if matched,"
							  + "it is not necessarily a message, so one could have commands that require voice activation theoretically.\n\n"
							  + "Message Reaction: a reaction explicitly for messages, but without command features like being on the help panel.\n\n"
							  + "The docs beyond this template were auto-generated and omits internal Owner commands\n\n"
							  + "### Template Command\nHelp Panel Description (if there's none, I won't be on the help panel)\n"
							  + "#### User\nThe user category that can use this command\n"
							  + "#### Location\nWhere this command can be used\n"
							  + "#### RegEx\nWhen this regular expression matches a message, the command runs (with possible extra checks)\n"
							  + "#### Cooldown\nThe amount of time a certain thing is locked. "
							  + "3 Seconds For Message Channels means this command can't be ran in each message channel until after 3 seconds, but two could have it ran at once\n"
							  + "#### Example\nThis is an example message that would call the command\n");
			ReactionManager.reactions.forEach(r -> {
				if (r.getUserCategory() == UserCategoryType.OWNER
						&& (r.getDocs() == null || (r instanceof Command && ((Command) r).visibleDescription())))
					return;
				MessageEmbed e = r.toFullString();
				try {
					fileWriter.append("### ").append(e.getTitle()).append('\n');
					if (e.getDescription() != null)
						fileWriter.append(e.getDescription()).write('\n');
					e.getFields().forEach(f -> {
					try { fileWriter.append("#### ").append(f.getName()).append('\n').append(f.getValue()).append('\n');
						} catch (IOException ex) { throw new RuntimeException(ex); }
					});
				} catch (IOException ex) { throw new RuntimeException(ex); }
			});
		}
		catch (URISyntaxException | IOException e) {
			throw new RuntimeException("could not generate docs: " + e);
		}
	}

}
