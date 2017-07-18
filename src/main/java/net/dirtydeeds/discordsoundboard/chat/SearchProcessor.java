package net.dirtydeeds.discordsoundboard.chat;

import java.util.LinkedList;
import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.MessageBuilder;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

// Naive search function.
public class SearchProcessor extends SingleArgumentChatCommandProcessor {

	public static final SimpleLog LOG = SimpleLog.getLog("SearchProcessor");
	
	public SearchProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Search Sounds", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		User user = event.getAuthor();
    String query = getArgument();
    List<String> possibilities = new LinkedList<>();
		LOG.info(String.format("%s wants to search for \"%s\" in %s.", user.getName(), query, event.getGuild()));
    if (StringUtils.containsAny(query, '?')) {
    	w(event, "No sound file contains question marks `?`. *Baka*."); return;
    }
    // Leverage trie first.
		String possibleName = bot.getClosestMatchingSoundName(query);
		if (possibleName != null) {
			LOG.info("A close matching sound name is: " + possibleName);
			possibilities.add(possibleName);
		}
		// Naive iteration through all sound names.
		for (String name : bot.getSoundMap().keySet()) {
			if (name.contains(query) && !name.equals(possibleName)) possibilities.add(name);
		}
		MessageBuilder mb = new MessageBuilder();
		mb.append("Found **" + possibilities.size() + "** possible sound files for query `" + query + "` " + 
				user.getAsMention() + ":\n\n");
		LOG.info("Query produced " + possibilities.size() + " possibilities.");
		if (!possibilities.isEmpty()) {
			for (String possibility : possibilities)
				mb.append("`?" + possibility + "` ");
			for (String m : mb)
				m(event, m);
		} else {
			w(event, "No results found for query `" + query + "` " + user.getAsMention() + ".");
		}
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` `<keyword>` \u2014 search for sounds by keyword";
	}

}
