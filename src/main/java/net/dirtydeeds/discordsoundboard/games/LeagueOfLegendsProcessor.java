package net.dirtydeeds.discordsoundboard.games;

import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;

public class LeagueOfLegendsProcessor extends AbstractGameUpdateProcessor {

	public LeagueOfLegendsProcessor(SoundboardBot bot) {
		super(bot);
	}

	protected void handleEvent(UserGameUpdateEvent event, User user) {
		
	}

}
