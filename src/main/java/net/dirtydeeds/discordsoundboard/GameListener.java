package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.games.*;
import net.dirtydeeds.discordsoundboard.games.leagueoflegends.LeagueOfLegendsGameStartProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.LinkedList;
import java.util.List;

/**
 * @author asafatli.
 *
 * This class handles listening to game events.
 */
public class GameListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Game");
    
    private SoundboardBot bot;
    private List<GameUpdateProcessor> processors;

    public GameListener(SoundboardBot bot) {
        this.bot = bot;
        this.processors = new LinkedList<>();
        initializeProcessors();
    }
    
    private void initializeProcessors() {
    	processors.add(new LeagueOfLegendsGameStartProcessor(bot, bot.getDispatcher()));
    }

	public void onUserGameUpdate(UserGameUpdateEvent event) {      
		
		User user = event.getUser();
		String name = user.getUsername();
		String previousGame = event.getPreviousGameId();
		if (user.getCurrentGame() == null)
			LOG.info(name + " stopped playing game " + previousGame + ".");
		else if (previousGame == null)
			LOG.info(name + " started playing game " + user.getCurrentGame() + ".");
		else
			LOG.info(name + " changed games to " + user.getCurrentGame() + " from " + previousGame + ".");
        
		for (GameUpdateProcessor processor : processors) {
        	if (processor.isApplicableUpdateEvent(event, user)) {
        		processor.process(event);
            	LOG.info("Processed game update event with processor " + processor.getClass().getSimpleName());
        	}
        }
        
    }
}
