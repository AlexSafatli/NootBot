package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.games.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

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
    	processors.add(new LeagueOfLegendsGameStartProcessor(bot));
    }

	public void onUserGameUpdate(UserGameUpdateEvent event) {      
		
		User user = event.getUser();
		Member member = event.getGuild().getMemberById(user.getId());
		if (user.isBot()) return; // Ignore bots.
		
		String name = user.getName();
		Game previousGame = event.getPreviousGame();
		
		if (member.getGame() == null && previousGame != null)
			LOG.info(name + " stopped playing " + previousGame.getName() + ".");
		else if (previousGame == null)
			LOG.info(name + " started playing " + member.getGame().getName() + ".");
		else
			LOG.info(name + " changed to " + member.getGame().getName() + " from " + previousGame.getName() + ".");
        
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
