package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.games.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
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
    
    public static final SimpleLog LOG = SimpleLog.getLog("GameListener");
    
    private SoundboardBot bot;
    private List<GameUpdateProcessor> processors;

    public GameListener(SoundboardBot bot) {
        this.bot = bot;
        this.processors = new LinkedList<>();
        initializeProcessors();
    }
    
    private void initializeProcessors() {
    }

	public void onUserGameUpdate(UserGameUpdateEvent event) {      
		
        for (GameUpdateProcessor processor : processors) {
        	processor.process(event);
        	LOG.info("Processed game update event with processor " + processor.getClass().getSimpleName());
        }
        
    }
}
