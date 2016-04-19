package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;

public class GameStartProcessor extends AbstractGameUpdateProcessor {

	public GameStartProcessor(SoundboardBot bot) {
		super(bot);
	}

	protected void handleEvent(UserGameUpdateEvent event, User user) {
		//TODO
	}

}
