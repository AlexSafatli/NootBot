package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;

public class SpecificGameStartProcessor extends GenericGameStartProcessor {

	private final String gameName;
	
	public SpecificGameStartProcessor(SoundboardBot bot, String game) {
		super(bot);
		gameName = game;
	}

	public boolean isApplicableUpdateEvent(UserGameUpdateEvent event, User user) {
		Game currentGame = event.getGuild().getMemberById(user.getId()).getGame();
		if (currentGame == null) return false;
		String game = currentGame.getName();
		return super.isApplicableUpdateEvent(event, user) && game.equals(gameName);
	}

	@Override
	public String toString() {
		return super.toString() + "[" + gameName + "]";
	}
	
}
