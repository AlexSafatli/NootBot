package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;

public class GameStartProcessor extends AbstractGameUpdateProcessor {

	public GameStartProcessor(SoundboardBot bot) {
		super(bot);
	}

	protected void handleEvent(UserGameUpdateEvent event, User user) {
		if (!event.getPreviousGameId().equals(user.getCurrentGame())) {
			for (Guild guild : bot.getGuildsWithUser(user)) {
				bot.sendMessageToChannel(user.getUsername() + " just started playing " + user.getCurrentGame() + ".", guild.getPublicChannel());
			}
		}
	}

}
