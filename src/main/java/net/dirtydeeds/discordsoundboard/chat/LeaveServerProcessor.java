package net.dirtydeeds.discordsoundboard.chat;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

public class LeaveServerProcessor extends AuthenticatedSingleArgumentChatCommandProcessor {

	public LeaveServerProcessor(String prefix, SoundboardBot bot) {
		super(prefix, bot);
	}

	protected void handleEvent(MessageReceivedEvent event, String message) {
		if (getArgument() == null) {
			pm(event, "You need to provide an argument containing the server name.");
		} else {
			String serverName = getArgument();
			Guild toLeave = null;
			for (Guild guild : bot.getGuilds()) {
				if (guild.getName().equalsIgnoreCase(serverName)) {
					toLeave = guild; break;
				}
			}
			if (toLeave != null) {
				pm(event, "Leaving server **" + toLeave.getName() + "**.");
				bot.leaveServer(toLeave);
			} else pm(event, "No server found with name `" + serverName + "`.");
		}
	}
	
	@Override
	public String getCommandHelpString() {
		return super.getCommandHelpString() + " - leaves a joined server by name";
	}

}
