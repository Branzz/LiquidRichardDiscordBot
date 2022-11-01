package com.wordpress.brancodes.main;

import com.wordpress.brancodes.messaging.reactions.ReactionManager;
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
			fileWriter.append("## Reactions and Command docs\n");
			ReactionManager.reactions.forEach(r -> {
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
