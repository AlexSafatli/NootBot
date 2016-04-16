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
		String previousGame = event.getPreviousGameId();
		String newGame = user.getCurrentGame();
		if (newGame != null && previousGame == null) {
			for (Guild guild : bot.getGuildsWithUser(user)) {
				bot.sendMessageToChannel(user.getUsername() + " just started playing " + newGame + ".", guild.getPublicChannel());
			}
		}
	}

}
