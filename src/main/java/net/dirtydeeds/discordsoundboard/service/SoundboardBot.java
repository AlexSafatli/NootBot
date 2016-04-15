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

    public SoundboardBot(String username, String password, String owner, SoundboardDispatcher dispatcher) {
        this.owner = owner;
    	this.dispatcher = dispatcher;
    	this.pastMessages = new LinkedList<Message>();
    	initializeDiscordBot(username, password);
        setSoundPlayerVolume(75);
    }
    
    public String getOwner() {
    	return this.owner;
    }
    
    protected void addListener(Object listener) {
        bot.addEventListener(listener);
    }
    
    public void sendMessageToChannel(String msg, TextChannel channel) {
    	if (pastMessages.size() > MAX_PAST_MESSAGES_TO_KEEP) {
    		if (hasPermissionInChannel(channel, Permission.MESSAGE_MANAGE)) {
    			Message pastMessage = pastMessages.remove();
    			LOG.debug("Deleted message " + pastMessage.toString());
    			pastMessage.deleteMessage();
    		}
    	}
    	Message message = channel.sendMessage(msg);
    	LOG.info("[Message: " + channel.getGuild().getName() + "] " + msg);
    	pastMessages.add(message);
    }
    
    public void sendMessageToAllGuilds(String msg) {
    	for (Guild guild : bot.getGuilds()) {
    		sendMessageToChannel(msg, guild.getPublicChannel());
    	}
    }
    
    /**
     * Sets volume of the player.
     * @param volume - The volume value to set.
     */
    public void setSoundPlayerVolume(int volume) {
        playerVolume = (float) volume / 100;
    }
    
    /**
     * Joins the channel of the user provided and then plays a file.
     * @param fileName - The name of the file to play.
     * @param userName - The name of the user to lookup what VoiceChannel they are in.
     */
    public void playFileForUser(String fileName, String userName) {
        if (userName == null || userName.isEmpty()) userName = owner;
        try {
            joinCurrentChannel(userName);
            playFile(fileName);
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
    		playFile(toPlay);
    		return toPlay;
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
    public void playFileForChatCommand(String fileName, GuildMessageReceivedEvent event) throws Exception {
        if (event != null && !fileName.isEmpty()) {
        	if (dispatcher.getAvailableSoundFiles().get(fileName) != null) {
        		moveToUserIdsChannel(event);
        		playFile(fileName);
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
        if (event != null && bot.getAudioManager().isConnected() && bot.getAudioManager().getConnectedChannel().equals(event.getChannel())) {
            playFile(fileName);
            sendMessageToChannel("Welcome, " + event.getUser().getAsMention() + ".", event.getGuild().getPublicChannel());
        } else {
        	LOG.info("Was going to play file for entrance of " + event.getUser().getUsername() + " but bot was not in channel yet.");
        }
    }
    
    
    public boolean hasPermissionInChannel(TextChannel channel, Permission permission) {
    	return channel.checkPermission(bot.getSelfInfo(), permission);
    }
    
    /**
     * Moves to the specified voice channel.
     * @param channel - The channel specified.
     */
    public void moveToChannel(VoiceChannel channel){
        if (bot.getAudioManager().isConnected()) {
            if (bot.getAudioManager().isAttemptingToConnect()) {
                bot.getAudioManager().closeAudioConnection();
            }
            bot.getAudioManager().moveAudioConnection(channel);
        } else {
            bot.getAudioManager().openAudioConnection(channel);
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

    /**
     * Play file name requested. Will first try to load the file from the map of available sounds.
     * @param fileName - fileName to play.
     */
    public void playFile(String fileName) {
        SoundFile fileToPlay = dispatcher.getAvailableSoundFiles().get(fileName);
        if (fileToPlay != null) playFile(fileToPlay.getSoundFile());
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

    //Play the file provided.
    private void playFile(File audioFile) {
        try {
            if (soundPlayer != null && soundPlayer.isPlaying()) {
            	soundPlayer.pause();
            }
            soundPlayer = new FilePlayer(audioFile);
            bot.getAudioManager().setSendingHandler(soundPlayer);
            soundPlayer.play();
            soundPlayer.setVolume(playerVolume);
        }
        catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
		}
    }

    //Join the users current channel.
    private void joinCurrentChannel(String userName) {
        for (Guild guild : bot.getGuilds()) {
            for (VoiceChannel channel : guild.getVoiceChannels()) {
                channel.getUsers().stream().filter(user -> user.getUsername()
                        .equalsIgnoreCase(userName)).forEach(user -> moveToChannel(channel));
            }
        }
    }

    //Logs the discord bot in and adds the ChatSoundBoardListener if the user configured it to be used
    private void initializeDiscordBot(String username, String password) {
        try {
			bot = new JDABuilder().setEmail(username).setPassword(password).buildBlocking();
	        ChatSoundBoardListener chatListener = new ChatSoundBoardListener(this);
	        this.addListener(chatListener);
	        EntranceSoundBoardListener entranceListener = new EntranceSoundBoardListener(this);
	        this.addListener(entranceListener);
	        GameListener gameListener = new GameListener(this);
	        this.addListener(gameListener);
	        LOG.info("Initialized bot with username " + username);
		} catch (LoginException | IllegalArgumentException | InterruptedException e) {
			e.printStackTrace();
		}
    }

}
