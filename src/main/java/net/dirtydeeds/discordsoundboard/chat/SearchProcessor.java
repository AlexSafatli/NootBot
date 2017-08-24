package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.*;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

public class SearchProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("Search");

	public SearchProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Search Sounds", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
		String query = getArgument();
		if (query == null) {
			w(event, "I need something to search for. *Baka*.");
			return;
		}

		List<String> possibilities = new LinkedList<>();

		if (StringUtils.containsAny(query, '?')) {
			w(event, lookupString(Strings.SOUND_NO_QUESTION_MARKS));
			return;
		} else if (query.length() <= 1) {
			w(event, "`" + query + "` is too short. *Baka*.");
			return;
		}

		// Leverage trie first.
		String possibleName = bot.getClosestMatchingSoundName(query);
		if (possibleName != null) {
			LOG.info("Closest matching sound name from trie: " + possibleName);
			possibilities.add(possibleName);
		}

		// Naive iteration through all sound names.
		for (String name : bot.getSoundMap().keySet()) {
			if (name.contains(query) && !name.equals(possibleName))
				possibilities.add(name);
		}

		// Display results.
		MessageBuilder mb = new MessageBuilder();
		mb.append("Found **" + possibilities.size() +
		          "** possible sounds for query `" + query + "` \u2014 " +
		          user.getAsMention() + ".\n\n");
		LOG.info("Found " + possibilities.size() + " possible sounds.");
		if (!possibilities.isEmpty()) {
			for (String possibility : possibilities)
				mb.append("`?" + possibility + "` ");
			for (String m : mb)
				m(event, m);
		} else {
			w(event, "No results found for query `" + query + "` \u2014 " +
			  user.getAsMention() + ".");
		}
	}

	@Override
	public String getCommandHelpString() {
		return getPrefix() + " <keyword> - search for sounds by keyword";
	}
}
