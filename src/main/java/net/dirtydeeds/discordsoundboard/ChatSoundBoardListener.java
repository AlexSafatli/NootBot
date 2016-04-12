package net.dirtydeeds.discordsoundboard;

import net.dirtydeeds.discordsoundboard.chat.ChatSoundBoardProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListSoundsProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlayRandomProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlaySoundProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundPlayerImpl;
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
    
    private SoundPlayerImpl soundPlayer;
    private List<ChatSoundBoardProcessor> processors;

    public ChatSoundBoardListener(SoundPlayerImpl soundPlayer) {
        this.soundPlayer = soundPlayer;
        this.processors = new LinkedList<ChatSoundBoardProcessor>();
        initializeProcessors();
    }
    
    private void initializeProcessors() {
    	processors.add(new ListSoundsProcessor("?list",   soundPlayer));
    	processors.add(new PlaySoundProcessor ("?",       soundPlayer));
    	processors.add(new PlayRandomProcessor(".random", soundPlayer));
    }

	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {      
    	
		// Respond
        for (ChatSoundBoardProcessor processor : processors) {
        	if (processor.isApplicableCommand(event)) {
        		processor.process(event);
        		LOG.info("Processed chat message event with processor " + processor.getClass().getName());
        		break;
        	}
        }
        
    }
}
