package net.dirtydeeds.discordsoundboard.service;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.listeners.ChatListener;
import net.dirtydeeds.discordsoundboard.listeners.EntranceListener;
import net.dirtydeeds.discordsoundboard.listeners.ExitListener;
import net.dirtydeeds.discordsoundboard.listeners.GameListener;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.MessageHistory;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.audio.player.FilePlayer;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.VoiceStatus;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import net.dv8tion.jda.events.voice.VoiceJoinEvent;
import net.dv8tion.jda.managers.AudioManager;
import net.dv8tion.jda.utils.SimpleLog;

import javax.security.auth.login.LoginException;
import javax.sound.sampled.UnsupportedAudioFileException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author dfurrer.
 *
 * This class handles moving into channels and playing sounds. Also, it loads the available sound files.
 */
public class SoundboardBot {

    public static final SimpleLog LOG = SimpleLog.getLog("Bot");
    private static int CHANNEL_CONNECTION_TIMEOUT = 5000;
    private static int TOP_PLAYED_SOUND_THRESHOLD = 32;
    private static String NOT_IN_VOICE_CHANNEL_MESSAGE = "Are you in a voice channel? Could not find you!";
    
    private long startTime;
    private JDA bot;
    private ChatListener chatListener;
    private String owner;
    private SoundboardDispatcher dispatcher;
    private float playerVolume = (float) .75;

    public SoundboardBot(String token, String owner, SoundboardDispatcher dispatcher) {
    	this.startTime = System.currentTimeMillis();
        this.owner = owner;
    	this.dispatcher = dispatcher;
    	initializeDiscordBot(token);
    }
    
    protected void addListener(Object listener) {
        bot.addEventListener(listener);
    }
    
    public long getUptimeInMinutes() {
    	Date now = new Date(System.currentTimeMillis()), then = new Date(startTime);
    	long minutesSince = (now.getTime() - then.getTime())/(1000*60);
    	return minutesSince;
    }
    
    /**
     * Gets a Map of the loaded sound files.
     * @return Map of sound files that have been loaded.
     */
    public Map<String, SoundFile> getSoundMap() {
        return dispatcher.getAvailableSoundFiles();
    }
    
    public SoundboardDispatcher getDispatcher() {
    	return this.dispatcher;
    }
    
    public String getClosestMatchingSoundName(String str) {
    	return dispatcher.getSoundNameTrie().getWordWithPrefix(str);
    }
    
    public String getRandomTopPlayedSoundName() {
    	List<SoundFile> sounds = dispatcher.getSoundFilesOrderedByNumberOfPlays();
    	if (sounds == null || sounds.isEmpty()) return null;
    	Random rng = new Random();
    	int index = rng.nextInt(Math.min(TOP_PLAYED_SOUND_THRESHOLD,sounds.size()));
    	return sounds.get(index).getSoundFileId();
    }
    
    public List<String> getSoundCategories() {
    	LinkedList<String> categories = new LinkedList<String>();
    	for (SoundFile soundFile : dispatcher.getAvailableSoundFiles().values()) {
    		if (!categories.contains(soundFile.getCategory())) categories.add(soundFile.getCategory());
    	}
    	return categories;
    }
    
    public User getUser(net.dv8tion.jda.entities.User user) {
		List<User> users = dispatcher.getUserById(user.getId());
		if (users != null && !users.isEmpty()) {
			return users.get(0);
		} else {
			User u = new User(user.getId(), user.getUsername());
			dispatcher.saveUser(u);
			return u;
		}
    }
    
    public String getOwner() {
    	return this.owner;
    }
    
    public boolean isAuthenticated(net.dv8tion.jda.entities.User user, Guild guild) {
    	if (guild != null) {
	    	List<Role> roles = guild.getRolesForUser(user);
	    	for (Role role : roles) {
	    		if (role.hasPermission(Permission.ADMINISTRATOR) || role.hasPermission(Permission.MANAGE_SERVER))
	    			return true;
	    	}
	    	if (user.getUsername().equals(getOwner())) return true;
    	} else {
    		for (Guild guild_ : getGuildsWithUser(user)) {
    			if (isAuthenticated(user, guild_)) return true;
    		}
    	}
    	return false;
    }
    
    public String getBotName() {
    	return this.bot.getSelfInfo().getUsername();
    }
    
    public VoiceChannel getConnectedChannel(Guild guild) {
    	return (bot.getAudioManager(guild).isConnected()) ? bot.getAudioManager(guild).getConnectedChannel() : null;
    }
    
    public void setVolume(float volume) {
    	if (volume <= 1 && volume >= 0) this.playerVolume = volume;
    }
    
	public net.dv8tion.jda.entities.User getUserByName(String username) {
    	for (net.dv8tion.jda.entities.User user : bot.getUsers()) {
    		if (user.getUsername().equalsIgnoreCase(username)) {
    			return user;
    		}
    	}
    	return null;
	}

	public String getEntranceForUser(net.dv8tion.jda.entities.User user) {
		List<User> users = dispatcher.getUserById(user.getId());
		return (users != null && !users.isEmpty()) ? users.get(0).getEntrance() : null;
	}
	
	public void setEntranceForUser(net.dv8tion.jda.entities.User user, String filename) {
		if (filename != null)
			LOG.info("New entrance \"" + filename + "\" set for " + user);
		else
			LOG.info("Cleared entrance associated with " + user);
		List<User> users = dispatcher.getUserById(user.getId());
		if (users != null && !users.isEmpty()) {
			users.get(0).setEntrance(filename);
			dispatcher.saveUser(users.get(0));
		} else {
			User u = new User(user.getId(), user.getUsername());
			u.setEntrance(filename);
			dispatcher.saveUser(u);
		}
	}
    
    public boolean disallowUser(net.dv8tion.jda.entities.User user) {
    	List<User> users = dispatcher.getUserById(user.getId());
    	if (users != null && !users.isEmpty() && users.get(0).isDisallowed()) {
    		return true; // Already disallowed.
    	} else {
    		LOG.info("User " + user + " disallowed from playing sounds.");
    		if (users == null || users.isEmpty()) { // Not already in records.
    			dispatcher.registerUser(user, true, false);
    		} else { // Already in records.
    			users.get(0).setDisallowed(true);
    			dispatcher.saveUser(users.get(0));
    		}
    		return true;
    	}
    }
    
    public boolean disallowUser(String username) {
    	net.dv8tion.jda.entities.User user = getUserByName(username);
    	return (user != null) ? disallowUser(user) : false;
    }
    
    public boolean allowUser(net.dv8tion.jda.entities.User user) {
    	List<User> users = dispatcher.getUserById(user.getId());
    	if (users != null && !users.isEmpty()) {
    		users.get(0).setDisallowed(false);
    		dispatcher.saveUser(users.get(0));
    		return true;
    	} return false;
    }
    
    public boolean allowUser(String username) {
    	net.dv8tion.jda.entities.User user = getUserByName(username);
    	return (user != null) ? allowUser(user) : false;
    }
    
    public boolean throttleUser(net.dv8tion.jda.entities.User user) {
    	List<User> users = dispatcher.getUserById(user.getId());
    	if (users != null && !users.isEmpty() && users.get(0).isThrottled()) {
    		return true; // Already throttled.
    	} else {
    		LOG.info("User " + user + " throttled from playing sounds.");
    		if (users == null || users.isEmpty()) {
    			dispatcher.registerUser(user, false, true);
    		} else {
    			users.get(0).setThrottled(true);
    			dispatcher.saveUser(users.get(0));
    		}
    		if (user.getOnlineStatus().equals(OnlineStatus.ONLINE))
	    		sendMessageToUser("You have been **throttled** from sending"
	    				+ " me commands. You will only be able to send me limited requests in a"
	    				+ " period of time. Contact the bot owner to dispute this action.", user);
    		return true;
    	}
    }
    
	public boolean throttleUser(String username) {
    	net.dv8tion.jda.entities.User user = getUserByName(username);
    	return (user != null) ? throttleUser(user) : false;
	}
	
    public boolean unthrottleUser(net.dv8tion.jda.entities.User user) {
    	List<User> users = dispatcher.getUserById(user.getId());
    	if (users != null && !users.isEmpty()) {
    		if (users.get(0).isThrottled() && user.getOnlineStatus().equals(OnlineStatus.ONLINE)) {
        		sendMessageToUser("You are no longer "
        				+ "**throttled** from sending me commands. You have "
        				+ "no more chat restriction.", user);
    		}
    		users.get(0).setThrottled(false);
    		dispatcher.saveUser(users.get(0));
    		return true;
    	} return false;
    }
    
	public boolean unthrottleUser(String username) {
    	net.dv8tion.jda.entities.User user = getUserByName(username);
    	return (user != null) ? unthrottleUser(user) : false;
	}
    
    public void leaveServer(Guild guild) {
    	if (guild != null && getGuilds().contains(guild)) {
    		guild.getAudioManager().closeAudioConnection();
    		guild.getManager().leave();
    	}
    }
    
    public void sendMessageToUser(String msg, net.dv8tion.jda.entities.User user) {
		user.getPrivateChannel().sendMessageAsync(msg, null);
	}
    

	public void sendMessageToUser(String msg, String username) {
    	net.dv8tion.jda.entities.User user = getUserByName(username);
    	if (user != null) sendMessageToUser(msg, user);
    	else LOG.warn("Tried to send message \"" + msg + "\" to username " + username + " but could not find user.");
	}
    
    public void sendMessageToAllGuilds(String msg) {
    	for (Guild guild : bot.getGuilds()) {
    		guild.getPublicChannel().sendMessageAsync(msg, null);
    	}
    }
    
    /**
     * Plays a random sound file.
     */
    public String playRandomFile(net.dv8tion.jda.entities.User user) throws Exception {
        if (user != null && isAllowedToPlaySound(user)) {
        	Object[] fileNames = dispatcher.getAvailableSoundFiles().keySet().toArray();
        	String toPlay = (String)fileNames[new Random().nextInt(fileNames.length)];
        	VoiceChannel toJoin = getUsersVoiceChannel(user);
        	if (toJoin == null) {
                sendMessageToUser(NOT_IN_VOICE_CHANNEL_MESSAGE, user);
        	} else {
        		moveToChannel(toJoin);
        		playFile(toPlay, toJoin.getGuild());
        		return toPlay;
        	}
        }
        return null;
    }
    
    /**
     * Plays a random sound file from a category.
     */
    public String playRandomFileForCategory(net.dv8tion.jda.entities.User user, String category) throws Exception {
        if (user != null && isAllowedToPlaySound(user)) {
        	if (!isASoundCategory(category)) {
        		LOG.info("Category " + category + " not found when playing random file.");
        		return null;
        	}
        	VoiceChannel toJoin = getUsersVoiceChannel(user);
        	if (toJoin == null) {
                sendMessageToUser(NOT_IN_VOICE_CHANNEL_MESSAGE, user);
        	} else {
            	Random rng = new Random();
            	SoundFile file = null;
            	Object[] files = dispatcher.getAvailableSoundFiles().values().toArray();
            	while (file == null || !file.getCategory().equalsIgnoreCase(category)) {
            		file = (SoundFile)files[rng.nextInt(files.length)];
            		LOG.debug("Randomed file " + file.getSoundFileId() + 
            				" with category " + file.getCategory());
            	}
            	String toPlay = file.getSoundFileId();
        		moveToChannel(toJoin);
        		playFile(toPlay, toJoin.getGuild());
        		return toPlay;
        	}
        }
        return null;
    }

	/**
     * Plays the fileName requested.
     * @param fileName - The name of the file to play.
     * @param event -  The even that triggered the sound playing request. The event is used to find the channel to play
     *              the sound back in.
     * @throws Exception
     */
    public void playFileForChatCommand(String fileName, MessageReceivedEvent event) throws Exception {
        if (event != null && !fileName.isEmpty() && isAllowedToPlaySound(event.getAuthor())) {
        	if (dispatcher.getAvailableSoundFiles().get(fileName) != null) {
            	VoiceChannel toJoin = getUsersVoiceChannel(event.getAuthor());
            	if (toJoin == null) {
                    sendMessageToUser(NOT_IN_VOICE_CHANNEL_MESSAGE, event.getAuthor());
            	} else if (event.getGuild() != null && toJoin.getGuild() != event.getGuild()) {
            		sendMessageToUser("You are in a voice channel that is not in "
            				+ "the server you sent the message in!", event.getAuthor());
            	} else {
            		moveToChannel(toJoin);
            		playFile(fileName, toJoin.getGuild());
            		LOG.info("Played sound \"" + fileName + "\" in server " + toJoin.getGuild().getName());
            	}
        	}
        }
    }
    
	/**
     * Plays the fileName requested for a specific user.
     * @param fileName - The name of the file to play.
     * @param user
	 * @return 
     * @throws Exception
     */
	public String playFileForUser(String fileName, net.dv8tion.jda.entities.User user) {
        if (user != null && !fileName.isEmpty()) {
        	if (dispatcher.getAvailableSoundFiles().get(fileName) != null) {
        		VoiceChannel channel = null;
        		try { 
        			channel = getUsersVoiceChannel(user);
        		} catch (Exception e) {
        			LOG.fatal("Failed to search for user " + user + " in a voice channel.");
        		}
        		if (channel == null) {
        			LOG.warn("Problem moving to user " + user);
        			return null;
        		}
        		else {
        			moveToChannel(channel);
        			playFile(fileName, channel.getGuild());
        			return dispatcher.getAvailableSoundFiles().get(fileName).getSoundFileId();
        		}
        	}
        }
        return null;
	}

	/**
     * Plays the fileName requested for a voice channel entrance.
     * @param fileName - The name of the file to play.
     * @param event -  The event that triggered the sound playing request. The event is used to find the channel to play
     *              the sound back in.
     * @throws Exception
     */
    public boolean playFileForEntrance(String fileName, VoiceJoinEvent event) throws Exception {
    	if (event == null || fileName == null) return false;
    	AudioManager voice = bot.getAudioManager(event.getGuild());
    	VoiceChannel connected = voice.getConnectedChannel();
        if ((voice.isConnected() || voice.isAttemptingToConnect()) && connected != null && 
        		(connected.equals(event.getChannel()) || connected.getUsers().size() == 1) || 
        		!voice.isConnected()) {
        	if (event.getChannel().getId().equals(event.getGuild().getAfkChannelId())) {
        		LOG.info("User joined AFK channel so will not follow him to play entrance.");
        		return false;
        	}
        	LOG.info("Playing entrance for user " + event.getUser().getUsername() + 
        			" with filename " + fileName);
        	if (connected == null || !connected.equals(event.getChannel()))
        		moveToChannel(event.getChannel());
        	SoundFile fileToPlay = dispatcher.getAvailableSoundFiles().get(fileName);
            playFile(fileToPlay, event.getGuild(), false); // Do not add to count for entrances.
        	return true;
        }
        return false;
    }
    
    
    public boolean isAllowedToPlaySound(String username) {
    	net.dv8tion.jda.entities.User user = getUserByName(username);
    	return (user != null) ? isAllowedToPlaySound(user) : true;
	}
    
    public boolean isAllowedToPlaySound(net.dv8tion.jda.entities.User user) {
		List<User> users = dispatcher.getUserById(user.getId());
		return !(users != null && !users.isEmpty() && users.get(0).isDisallowed());
	}
    
    public boolean isThrottled(String username) {
    	net.dv8tion.jda.entities.User user = getUserByName(username);
    	return (user != null) ? isThrottled(user) : false;
	}
    
    public boolean isThrottled(net.dv8tion.jda.entities.User user) {
		List<User> users = dispatcher.getUserById(user.getId());
		return (users != null && !users.isEmpty() && users.get(0).isThrottled());
	}
    
    public boolean isOwner(net.dv8tion.jda.entities.User user) {
    	return (user.getUsername().equals(owner));
    }
    
    public boolean hasPermissionInChannel(TextChannel channel, Permission permission) {
    	return channel.checkPermission(bot.getSelfInfo(), permission);
    }

    /**
     * Moves to the specified voice channel.
     * @param channel - The channel specified.
     */
    public void moveToChannel(VoiceChannel channel) {
    	if (channel == null) {
    		LOG.warn("Cannot move to a null voice channel."); return;
    	}
    	AudioManager voice = bot.getAudioManager(channel.getGuild());
    	voice.setSendingHandler(null);
    	try {
	        if (voice.isConnected()) voice.moveAudioConnection(channel);
	        else if (voice.isAttemptingToConnect())
	        	LOG.info("Still waiting to connect to channel " + voice.getQueuedAudioConnection().getName());
	        else {
	        	voice.openAudioConnection(channel);
	        	voice.setConnectTimeout(CHANNEL_CONNECTION_TIMEOUT);
	        }
    	} catch (IllegalStateException e) {
    		e.printStackTrace();
    		channel.getGuild().getPublicChannel().sendMessageAsync("*This is a little awkward, guys.* I'm having problems joining a channel.", null);
    		voice.closeAudioConnection();
    	}
    }
    
    /**
     * Gets a user's current voice channel.
     * @throws Exception
     */
    public VoiceChannel getUsersVoiceChannel(net.dv8tion.jda.entities.User user) throws Exception {
        
    	for (Guild guild : getGuildsWithUser(user)) {
    		VoiceStatus v = guild.getVoiceStatusOfUser(user);
    		if (v.getChannel() != null) return v.getChannel();
    	}
    	return null; // Could not find this user in a voice channel.
        
    }

	public boolean isUser(net.dv8tion.jda.entities.User user) {
		net.dv8tion.jda.entities.User self = (net.dv8tion.jda.entities.User)bot.getSelfInfo();
		return (self.equals(user) || self.getUsername().equals(user.getUsername()));
	}
    
	public boolean isASoundCategory(String category) {
    	for (String c : getSoundCategories()) {
    		if (c.equalsIgnoreCase(category)) return true;
    	}
    	return false;
    }
	
	public void deleteHistoryForChannel(MessageChannel channel) {
		if (hasPermissionInChannel((TextChannel)channel, Permission.MESSAGE_MANAGE)) {
			MessageHistory history = new MessageHistory(channel);
			for (Message message : history.retrieveAll()) {
				if (message.getAuthor() != null) {
					try {
						message.deleteMessage();
					} catch (Exception e) {
						LOG.warn("Could not delete message " + message);
					}
				}
			}
		}
	}

    /**
     * Play file name requested. Will first try to load the file from the map of available sounds.
     * @param fileName - fileName to play.
     */
    public void playFile(String fileName, Guild guild) {
        SoundFile fileToPlay = dispatcher.getAvailableSoundFiles().get(fileName);
        playFile(fileToPlay, guild, true);
    }
    
    private void playFile(SoundFile fileToPlay, Guild guild, boolean addToCount) {
        if (fileToPlay != null && guild != null) {
        	playFile(fileToPlay.getSoundFile(), guild);
        	if (addToCount) {
        		fileToPlay.addOneToNumberOfPlays();
        		dispatcher.saveSound(fileToPlay);
        	}
        }
    }
    
    public Path getSoundsPath() {
    	return Paths.get(System.getProperty("user.dir") + "/sounds");
    }
    
    /**
     * Get a list of users
     */
    public List<net.dirtydeeds.discordsoundboard.beans.User> getUsers() {
        List<User> users = new ArrayList<>();
        for (net.dv8tion.jda.entities.User user : bot.getUsers()) {
            if (user.getOnlineStatus().equals(OnlineStatus.ONLINE)) {
                String username = user.getUsername();
                users.add(new net.dirtydeeds.discordsoundboard.beans.User(user.getId(), username));
            }
        }
        return users;
    }
    
    public List<Guild> getGuilds() {
    	return bot.getGuilds();
    }
    
    public List<Guild> getGuildsWithUser(net.dv8tion.jda.entities.User user) {
    	List<Guild> guilds = new LinkedList<>();
    	for (Guild guild : getGuilds()) {
    		if (guild.getUsers().contains(user)) guilds.add(guild);
    	}
    	return guilds;
    }
    
    public Properties getAppProperties() {
    	return dispatcher.getAppProperties();
    }
    
    public JDA getAPI() {
    	return this.bot;
    }
    
    public ChatListener getChatListener() {
    	return this.chatListener;
    }

    //Play the file provided.
    private void playFile(File audioFile, Guild guild) {
    	AudioManager audio = bot.getAudioManager(guild);
        try {
        	FilePlayer player = (FilePlayer)audio.getSendingHandler();
        	if (player != null) player.stop();
        	audio.setSendingHandler(null);
            player = new FilePlayer(audioFile);
            audio.setSendingHandler(player);
            player.play(); player.setVolume(playerVolume);
        } catch (IOException | UnsupportedAudioFileException e) {
            LOG.fatal("While playing " + audioFile.getName() + ": " + e.toString());
            audio.setSendingHandler(null);
		}
    }

    //Logs the discord bot in and adds the ChatSoundBoardListener if the user configured it to be used
    private void initializeDiscordBot(String token) {
        try {
			bot = new JDABuilder().setBotToken(token).buildBlocking();
	        ChatListener chatListener = new ChatListener(this);
	        this.chatListener = chatListener;
	        this.addListener(chatListener);
	        EntranceListener entranceListener = new EntranceListener(this);
	        this.addListener(entranceListener);
	        ExitListener exitListener = new ExitListener(this);
	        this.addListener(exitListener);
	        GameListener gameListener = new GameListener(this);
	        this.addListener(gameListener);
	        LOG.info("Finished initializing bot with name " + getBotName());
		} catch (LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
		}
    }
    
}
