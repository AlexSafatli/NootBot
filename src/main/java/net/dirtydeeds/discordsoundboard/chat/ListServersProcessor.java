package net.dirtydeeds.discordsoundboard.chat;

import java.util.List;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class ListServersProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public ListServersProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
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
        	event.getChannel().sendMessageAsync("I am connected to **" + guilds.size() + " servers**:\n\n" + 
        			sb.toString(), null);
        }
	}
	
	@Override
	public String getCommandHelpString() {
		return "`" + getPrefix() + "` - list all servers connected to";
	}
	

}
