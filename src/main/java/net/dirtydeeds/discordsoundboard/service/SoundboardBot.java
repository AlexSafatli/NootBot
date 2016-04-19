package net.dirtydeeds.discordsoundboard.service;

import net.dirtydeeds.discordsoundboard.ChatSoundBoardListener;
import net.dirtydeeds.discordsoundboard.EntranceSoundBoardListener;
import net.dirtydeeds.discordsoundboard.GameListener;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.beans.User;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.OnlineStatus;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.audio.player.FilePlayer;
import net.dv8tion.jda.audio.player.Player;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.VoiceChannel;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.guild.GuildMessageReceivedEvent;
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

/**
 * @author dfurrer.
 *
 * This class handles moving into channels and playing sounds. Also, it loads the available sound files.
 */
public class SoundboardBot {

    public static final SimpleLog LOG = SimpleLog.getLog("Bot");
    private static final int MAX_PAST_MESSAGES_TO_KEEP = 10;
    
    private JDA bot;
    private String owner;
    private Player soundPlayer;
    private SoundboardDispatcher dispatcher;
    private Queue<Message> pastMessages;
    private float playerVolume = (float) .75;

    public SoundboardBot(String token, String owner, SoundboardDispatcher dispatcher) {
        this.owner = owner;
    	this.dispatcher = dispatcher;
    	this.pastMessages = new LinkedList<Message>();
    	initializeDiscordBot(token);
    }
    
    protected void addListener(Object listener) {
        bot.addEventListener(listener);
    }
    
    /**
     * Gets a Map of the loaded sound files.
     * @return Map of sound files that have been loaded.
     */
    public Map<String, SoundFile> getAvailableSoundFiles() {
        return dispatcher.getAvailableSoundFiles();
    }
    
    public List<String> getSoundCategories() {
    	LinkedList<String> categories = new LinkedList<String>();
    	for (SoundFile soundFile : dispatcher.getAvailableSoundFiles().values()) {
    		if (!categories.contains(soundFile.getCategory())) categories.add(soundFile.getCategory());
    	}
    	return categories;
    }
    
    public String getOwner() {
    	return this.owner;
    }
    
    public VoiceChannel getConnectedChannel(Guild guild) {
    	if (bot.getAudioManager(guild).isConnected()) return bot.getAudioManager(guild).getConnectedChannel();
    	return null;
    }
    
    public void sendMessageToChannel(String msg, TextChannel channel) {
    	if (pastMessages.size() > MAX_PAST_MESSAGES_TO_KEEP) {
    		if (hasPermissionInChannel(channel, Permission.MESSAGE_MANAGE)) {
    			Message pastMessage = pastMessages.remove();
    			LOG.debug("Deleting message " + pastMessage.toString());
    			pastMessage.deleteMessage();
    		}
    	}
    	Message message = channel.sendMessage(msg);
    	pastMessages.add(message);
    }
    
    public void sendMessageToAllGuilds(String msg) {
    	for (Guild guild : bot.getGuilds()) {
    		sendMessageToChannel(msg, guild.getPublicChannel());
    	}
    }

    /**
     * Joins the channel of the user provided and then plays a file.
     * @param fileName - The name of the file to play.
     * @param userName - The name of the user to lookup what VoiceChannel they are in.
     */
    public void playFileForUser(String fileName, String userName) {
        if (userName == null || userName.isEmpty()) userName = owner;
        try {
            Guild guild = joinCurrentChannel(userName);
            playFile(fileName, guild);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays a random sound file.
     */
    public String playRandomFile(GuildMessageReceivedEvent event) throws Exception {
        if (event != null) {
        	Object[] fileNames = dispatcher.getAvailableSoundFiles().keySet().toArray();
        	String toPlay = (String)fileNames[new Random().nextInt(fileNames.length)];
    		moveToUserIdsChannel(event);
    		playFile(toPlay, event.getGuild());
    		return toPlay;
        }
        return null;
    }
    
    public SoundFile playRandomFileForCategory(String username, String category, Guild guild) throws Exception {
    	if (!isASoundCategory(category)) {
    		LOG.info(category + " not found when playing random file from a category.");
    		return null;
    	}
    	Random rng = new Random();
    	SoundFile file = null;
    	Object[] files = dispatcher.getAvailableSoundFiles().values().toArray();
    	while (file == null || !file.getCategory().equalsIgnoreCase(category)) {
    		file = (SoundFile)files[rng.nextInt(files.length)];
    		LOG.debug("Randomed file " + file.getSoundFileId() + " with category " + file.getCategory());
    	}
    	joinCurrentChannel(username);
    	playFile(file.getSoundFile(), guild);
    	return file;
    }
    
    /**
     * Plays the fileName requested.
     * @param fileName - The name of the file to play.
     * @param event -  The even that triggered the sound playing request. The event is used to find the channel to play
     *              the sound back in.
     * @throws Exception
     */
    public void playFileForChatCommand(String fileName, GuildMessageReceivedEvent event) throws Exception {
        if (event != null && !fileName.isEmpty()) {
        	if (dispatcher.getAvailableSoundFiles().get(fileName) != null) {
        		moveToUserIdsChannel(event);
        		playFile(fileName, event.getGuild());
        	} else {
        		sendMessageToChannel("No sound file to play with name `" + fileName + "` " + event.getAuthor().getAsMention() + ".", event.getChannel());
        	}
        }
    }

    /**
     * Plays the fileName requested for a voice channel entrance.
     * @param fileName - The name of the file to play.
     * @param event -  The even that triggered the sound playing request. The event is used to find the channel to play
     *              the sound back in.
     * @throws Exception
     */
    public void playFileForEntrance(String fileName, VoiceJoinEvent event) throws Exception {
    	if (event == null) return;
    	AudioManager voice = bot.getAudioManager(event.getGuild());
    	VoiceChannel connected = voice.getConnectedChannel();
        if (voice.isConnected() && connected.equals(event.getChannel()) || !voice.isConnected()) {
        	LOG.info("Responding to the entrance of " + event.getUser().getUsername() + " in channel " + event.getChannel().getName() + " in guild " + event.getChannel().getGuild().getName());
        	moveToChannel(event.getChannel());
        	playFile(fileName, event.getGuild());
            sendMessageToChannel("Welcome, " + event.getUser().getAsMention() + ".", event.getGuild().getPublicChannel());
        }
    }
    
    
    public boolean hasPermissionInChannel(TextChannel channel, Permission permission) {
    	return channel.checkPermission(bot.getSelfInfo(), permission);
    }
    
    public boolean isConnectedToChannel(VoiceChannel channel) {
    	AudioManager voice = bot.getAudioManager(channel.getGuild());
    	return (voice != null && voice.isConnected() && voice.getConnectedChannel().equals(channel));
    }
    

    /**
     * Moves to the specified voice channel.
     * @param channel - The channel specified.
     */
    public void moveToChannel(VoiceChannel channel) {
    	AudioManager voice = bot.getAudioManager(channel.getGuild());
        if (voice.isConnected() || voice.isAttemptingToConnect()) {
            voice.moveAudioConnection(channel);
        } else {
            voice.openAudioConnection(channel);
        }
    }

    /**
     * Find the "author" of the event and join the voice channel they are in.
     * @param event - The event
     * @throws Exception
     */
    public void moveToUserIdsChannel(GuildMessageReceivedEvent event) throws Exception {
        VoiceChannel channel = null;

        outerloop:
        for (VoiceChannel channel1 : event.getGuild().getVoiceChannels()) {
            for (net.dv8tion.jda.entities.User user : channel1.getUsers()) {
                if (user.getId().equals(event.getAuthor().getId())) {
                    channel = channel1;
                    break outerloop;
                }
            }
        }

        if (channel == null) {
            sendMessageToChannel("Could not move to your channel " + event.getAuthor().getAsMention() + "!", event.getChannel());
            throw new Exception("Problem moving to requested user " + event.getAuthor().getId());
        }

        moveToChannel(channel);
    }

    public boolean isASoundCategory(String category) {
    	for (String c : getSoundCategories()) {
    		if (c.equalsIgnoreCase(category)) return true;
    	}
    	return false;
    }

    /**
     * Play file name requested. Will first try to load the file from the map of available sounds.
     * @param fileName - fileName to play.
     */
    public void playFile(String fileName, Guild guild) {
        SoundFile fileToPlay = dispatcher.getAvailableSoundFiles().get(fileName);
        if (fileToPlay != null && guild != null) playFile(fileToPlay.getSoundFile(), guild);
    }
    
    public Path getSoundsPath() {
    	return Paths.get(System.getProperty("user.dir") + "/sounds");
    }
    
    /**
     * Get a list of users
     */
    public List<net.dirtydeeds.discordsoundboard.beans.User> getUsers() {
        String userNameToSelect = owner;
        List<User> users = new ArrayList<>();
        for (net.dv8tion.jda.entities.User user : bot.getUsers()) {
            if (user.getOnlineStatus().equals(OnlineStatus.ONLINE)) {
                boolean selected = false;
                String username = user.getUsername();
                if (userNameToSelect.equals(username)) {
                    selected = true;
                }
                users.add(new net.dirtydeeds.discordsoundboard.beans.User(user.getId(), username, selected));
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

    //Play the file provided.
    private void playFile(File audioFile, Guild guild) {
        try {
            if (soundPlayer != null && soundPlayer.isPlaying()) {
            	soundPlayer.pause();
            }
            soundPlayer = new FilePlayer(audioFile);
            bot.getAudioManager(guild).setSendingHandler(soundPlayer);
            soundPlayer.play();
            soundPlayer.setVolume(playerVolume);
        }
        catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
		}
    }

    //Join the users current channel.
    private Guild joinCurrentChannel(String userName) {
        for (Guild guild : bot.getGuilds()) {
            for (VoiceChannel channel : guild.getVoiceChannels()) {
            	List<net.dv8tion.jda.entities.User> users = channel.getUsers();
            	for (net.dv8tion.jda.entities.User user : users) {
            		if (user.getUsername().equalsIgnoreCase(userName)) {
            			moveToChannel(channel);
            			return channel.getGuild();
            		}
            	}
            }
        }
        return null;
    }

    //Logs the discord bot in and adds the ChatSoundBoardListener if the user configured it to be used
    private void initializeDiscordBot(String token) {
        try {
			bot = new JDABuilder().setBotToken(token).buildBlocking();
	        ChatSoundBoardListener chatListener = new ChatSoundBoardListener(this);
	        this.addListener(chatListener);
	        EntranceSoundBoardListener entranceListener = new EntranceSoundBoardListener(this);
	        this.addListener(entranceListener);
	        GameListener gameListener = new GameListener(this);
	        this.addListener(gameListener);
	        LOG.info("Finished initializing bot with token " + token);
		} catch (LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
		}
    }

}
