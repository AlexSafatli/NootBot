package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class ListServersProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public ListServersProcessor(String prefix, SoundboardBot bot) {
		super(prefix, "Servers", bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
        List<Guild> guilds = bot.getGuilds();
        if (guilds.size() > 0) {
        	StringBuilder sb = new StringBuilder();
        	int i = 0;
        	for (Guild guild : guilds) {
        		sb.append("**" + guild.getName() + "** ");
        		++i;
        		if (i != guilds.size()) sb.append(" / ");
        	}
        	m(event, "I am connected to **" + guilds.size() + " servers**:\n\n" + 
        			sb.toString());
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return getPrefix() + " - list all servers connected to";
	}
	

}
