package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.games.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.Game;
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
public class GameListener extends AbstractListener {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Game");
    
    private List<GameUpdateProcessor> processors;

    public GameListener(SoundboardBot bot) {
        this.bot = bot;
        this.processors = new LinkedList<>();
        initializeProcessors();
    }
    
    private void initializeProcessors() {
    	processors.add(new GenericGameStartProcessor(bot));
    }

	public void onUserGameUpdate(UserGameUpdateEvent event) {      
		
		User user = event.getUser();
		String name = user.getUsername();
		Game previousGame = event.getPreviousGame();
		if (user.getCurrentGame() == null && previousGame != null)
			LOG.info(name + " stopped playing " + previousGame.getName() + ".");
		else if (previousGame == null)
			LOG.info(name + " started playing " + user.getCurrentGame().getName() + ".");
		else
			LOG.info(name + " changed to " + user.getCurrentGame().getName() + " from " + previousGame.getName() + ".");
        
		for (GameUpdateProcessor processor : processors) {
        	if (processor.isApplicableUpdateEvent(event, user)) {
        		processor.process(event);
            	LOG.info("Processed game update event with processor " + processor.getClass().getSimpleName());
            	if (processor.isMutuallyExclusive()) {
            		LOG.info("That processor cannot be run with others. Stopping.");
            		return;
            	}
        	}
        }
        
    }
}
