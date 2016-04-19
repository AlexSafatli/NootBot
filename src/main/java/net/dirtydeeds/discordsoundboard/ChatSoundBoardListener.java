package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.chat.ChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListNewSoundsProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListSoundsProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlayRandomProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlaySoundProcessor;
import net.dirtydeeds.discordsoundboard.chat.SoundAttachmentProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.LinkedList;
import java.util.List;

/**
 * @author dfurrer.
 *
 * This class handles listening to commands in discord text channels and responding to them.
 */
public class ChatSoundBoardListener extends ListenerAdapter {
    
    public static final SimpleLog LOG = SimpleLog.getLog("ChatListener");
    
    private SoundboardBot bot;
    private List<ChatCommandProcessor> processors;

    public ChatSoundBoardListener(SoundboardBot soundPlayer) {
        this.bot = soundPlayer;
        this.processors = new LinkedList<ChatCommandProcessor>();
        initializeProcessors();
    }
    
    private void initializeProcessors() {
    	processors.add(new PlaySoundProcessor ("?",       bot));
    	processors.add(new ListSoundsProcessor(".list",   bot));
    	processors.add(new ListNewSoundsProcessor(".new", bot));
    	processors.add(new PlayRandomProcessor(".random", bot));
    	processors.add(new SoundAttachmentProcessor(      bot));
    }

	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {      
		
		// Respond to a particular message using a processor.
        for (ChatCommandProcessor processor : processors) {
        	if (processor.isApplicableCommand(event)) {
        		processor.process(event);
        		LOG.info("Processed chat message event with processor " + processor.getClass().getSimpleName());
        		break;
        	}
        }
        
    }
}
