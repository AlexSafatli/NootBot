package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.chat.AllowUserProcessor;
import net.dirtydeeds.discordsoundboard.chat.ChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.chat.ClearChatMessagesProcessor;
import net.dirtydeeds.discordsoundboard.chat.DeleteSoundProcessor;
import net.dirtydeeds.discordsoundboard.chat.DescribeSoundProcessor;
import net.dirtydeeds.discordsoundboard.chat.DisallowUserProcessor;
import net.dirtydeeds.discordsoundboard.chat.DownloadSoundProcessor;
import net.dirtydeeds.discordsoundboard.chat.HelpProcessor;
import net.dirtydeeds.discordsoundboard.chat.LeaveServerProcessor;
import net.dirtydeeds.discordsoundboard.chat.LimitUserProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListCategoriesProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListNewSoundsProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListServersProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListSoundsProcessor;
import net.dirtydeeds.discordsoundboard.chat.ListTopSoundsProcessor;
import net.dirtydeeds.discordsoundboard.chat.ModifyAllSoundPlayCountProcessor;
import net.dirtydeeds.discordsoundboard.chat.ModifySoundPlayCountProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlayRandomProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlaySoundForUserProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlaySoundLoopedProcessor;
import net.dirtydeeds.discordsoundboard.chat.PlaySoundProcessor;
import net.dirtydeeds.discordsoundboard.chat.RecategorizeSoundProcessor;
import net.dirtydeeds.discordsoundboard.chat.RemoveLimitUserProcessor;
import net.dirtydeeds.discordsoundboard.chat.RenameSoundProcessor;
import net.dirtydeeds.discordsoundboard.chat.RestartBotProcessor;
import net.dirtydeeds.discordsoundboard.chat.ServerMessageProcessor;
import net.dirtydeeds.discordsoundboard.chat.SetEntranceForUserProcessor;
import net.dirtydeeds.discordsoundboard.chat.SetEntranceProcessor;
import net.dirtydeeds.discordsoundboard.chat.SetSoundDescriptionProcessor;
import net.dirtydeeds.discordsoundboard.chat.SoundAttachmentProcessor;
import net.dirtydeeds.discordsoundboard.chat.StatsProcessor;
import net.dirtydeeds.discordsoundboard.chat.UserInfoProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.hooks.ListenerAdapter;
import net.dv8tion.jda.utils.SimpleLog;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author asafatli
 * Listens to commands in Discord text channels and responds to them if a processors
 * exists to handle a particular message.
 */
public class ChatListener extends AbstractListener {
    
    public static final SimpleLog LOG = SimpleLog.getLog("Chat");
    
    private static final int THROTTLE_TIME_IN_MINUTES = 5;
    private static final int MAX_NUMBER_OF_REQUESTS_PER_TIME = 5;
    private static final int EXCESSIVE_NUMBER_OF_REQUESTS_PER_TIME = 75;
    
    private Date tick;
    private Map<User,Integer> requests;
    private List<ChatCommandProcessor> processors;
    private HelpProcessor helpProcessor;

    public ChatListener(SoundboardBot soundPlayer) {
        this.bot = soundPlayer;
        this.tick = new Date(System.currentTimeMillis());
        this.requests = new HashMap<>();
        this.processors = new LinkedList<ChatCommandProcessor>();
        initializeProcessors();
    }
    
    private void initializeProcessors() {
    	
    	processors.add(new PlaySoundProcessor ("?",                       bot));
    	processors.add(new ListSoundsProcessor(".list",                   bot));
    	processors.add(new DescribeSoundProcessor(".whatis",              bot));
    	processors.add(new ListCategoriesProcessor(".categories",         bot));
    	processors.add(new ListNewSoundsProcessor(".new",                 bot));
    	processors.add(new ListTopSoundsProcessor(".top",                 bot));
    	processors.add(new PlayRandomProcessor(".random",                 bot));
    	processors.add(new ClearChatMessagesProcessor(".clear",           bot));
    	processors.add(new DisallowUserProcessor(".disallow",             bot));
    	processors.add(new AllowUserProcessor(".allow",                   bot));
    	processors.add(new LimitUserProcessor(".throttle",                bot));
    	processors.add(new RemoveLimitUserProcessor(".unthrottle",        bot));
    	processors.add(new SetSoundDescriptionProcessor(".setinfo",       bot));
    	processors.add(new DeleteSoundProcessor(".rm",                    bot));
    	processors.add(new RenameSoundProcessor(".rename",                bot));
    	processors.add(new RecategorizeSoundProcessor(".mv",              bot));
    	processors.add(new DownloadSoundProcessor(".dl",                  bot));
    	processors.add(new ModifyAllSoundPlayCountProcessor(".countall",  bot));
    	processors.add(new ModifySoundPlayCountProcessor(".count",        bot));
    	processors.add(new SetEntranceForUserProcessor(".setentrancefor", bot));
    	processors.add(new SetEntranceProcessor(".setentrance",           bot));
    	processors.add(new PlaySoundForUserProcessor(".playfor",          bot));
    	processors.add(new PlaySoundLoopedProcessor(".loop",              bot));
    	processors.add(new ServerMessageProcessor(".all",                 bot));
    	processors.add(new LeaveServerProcessor(".leave",                 bot));
    	processors.add(new RestartBotProcessor(".restart",                bot));
    	processors.add(new ListServersProcessor(".servers",               bot));
    	processors.add(new UserInfoProcessor(".user",                     bot));
    	processors.add(new StatsProcessor(".stats",                       bot));
    	processors.add(new SoundAttachmentProcessor(                      bot));
    	
    	this.helpProcessor = new HelpProcessor(".help", bot, processors);
    	
    }

    private void processTick() {
    	Date now = new Date(System.currentTimeMillis());
    	long minutesSince = (now.getTime() - tick.getTime())/(1000*60);
    	if (minutesSince >= THROTTLE_TIME_IN_MINUTES) {
    		this.tick = now;
    		if (requests.size() > 0) {
    			LOG.info(minutesSince + " minutes have passed since last clear. Clearing request counts.");
    		}
    		requests.clear();
    	}
    }
    
	public void onMessageReceived(MessageReceivedEvent event) {      
		
		processTick();
		
		// Get number of requests for this user.
		Integer numRequests = requests.get(event.getAuthor());
		if (numRequests == null) numRequests = 0;
		
		// See if a help command first. Process it here if that is the case. This does not count against requests.
		if (helpProcessor.isApplicableCommand(event)) {
			helpProcessor.process(event);
			return;
		}
		
		// Respond to a particular message using a processor otherwise.
        for (ChatCommandProcessor processor : processors) {
        	if (processor.isApplicableCommand(event)) {
        		if (numRequests >= MAX_NUMBER_OF_REQUESTS_PER_TIME && bot.isThrottled(event.getAuthor())) {
        			LOG.info("Throttled user " + event.getAuthor() + " trying to send too many requests.");
        			event.getAuthor().getPrivateChannel().sendMessage("You have been throttled "
        					+ "from sending too many requests. Please wait a little before sending "
        					+ "another command.");
        		} else if (numRequests >= EXCESSIVE_NUMBER_OF_REQUESTS_PER_TIME && !bot.isOwner(event.getAuthor())) {
        			LOG.info("Throttling user " + event.getAuthor() + " because they sent too many requests.");
        			bot.throttleUser(event.getAuthor());
        			bot.sendMessageToUser("Throttling **" + event.getAuthor().getUsername() + 
        					"** automatically because of too many requests.", bot.getOwner());
        		} else {
        			processor.process(event);
	        		requests.put(event.getAuthor(), numRequests + 1); // Increment number of requests.
	        		LOG.info("Processed chat message event with processor " + processor.getClass().getSimpleName());
        		}
        		return;
        	}
        }
        
    }
}
