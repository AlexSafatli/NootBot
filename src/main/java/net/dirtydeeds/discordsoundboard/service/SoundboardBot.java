package net.dirtydeeds.discordsoundboard.service;

import net.dirtydeeds.discordsoundboard.audio.AudioHandler;
import net.dirtydeeds.discordsoundboard.audio.AudioPlayerSendHandler;
import net.dirtydeeds.discordsoundboard.audio.AudioTrackScheduler;
import net.dirtydeeds.discordsoundboard.beans.Setting;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.listeners.ChatListener;
import net.dirtydeeds.discordsoundboard.listeners.MoveListener;
import net.dirtydeeds.discordsoundboard.listeners.GameListener;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.utils.ChatUtils;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.SimpleLog;

import javax.security.auth.login.LoginException;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import java.io.File;
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
  private static final int CHANNEL_CONNECTION_TIMEOUT = 5000;
  private static final int TOP_PLAYED_SOUND_THRESHOLD = 50;
  private static final int MAX_DURATION_FOR_RANDOM = 10;
  private static final long MIN_MINUTES_TO_SHOW_AS_HOURS = 120;  // 2 hours
  private static final long MIN_MINUTES_TO_SHOW_AS_DAYS  = 2880; // 2 days
  public static String NOT_IN_VOICE_CHANNEL_MESSAGE =
    "Are you in a voice channel? Could not find you!";

  private long startTime;
  private JDA bot;
  private ChatListener chatListener;
  private String owner;
  private SoundboardDispatcher dispatcher;
  private Map<String, Setting> settings;
  private Map<Guild, AudioTrackScheduler> audioSchedulers;

  public SoundboardBot(String token, String owner,
                       SoundboardDispatcher dispatcher) {
    this.startTime = System.currentTimeMillis();
    this.owner = owner;
    this.dispatcher = dispatcher;
    this.settings = new Hashtable<>();
    this.audioSchedulers = new Hashtable<>();
    initializeDiscordBot(token);
    clearPreviousMessages();
  }

  private class SoundPlayedEvent {
    String sound;
    long time;
    String who;
    public SoundPlayedEvent(String sound, String who) {
      this.sound = sound;
      this.who = who;
      this.time = System.currentTimeMillis();
    }
    public SoundFile getSoundFile() {
      return dispatcher.getSoundFileByName(sound);
    }
    public String getUsername() {
      return who;
    }
    public String toString() {
      return "`" + sound + "` was played by **" + who + "**.";
    }
  }

  private SoundPlayedEvent lastPlayed;

  protected void addListener(Object listener) {
    bot.addEventListener(listener);
  }

  public long getUptimeInMinutes() {
    Date now = new Date(System.currentTimeMillis()), then = new Date(startTime);
    long minutesSince = (now.getTime() - then.getTime()) / (1000 * 60);
    return minutesSince;
  }

  public String getUptimeAsString() {
    String uptime = "";
    long minutes = getUptimeInMinutes();
    if (minutes >= MIN_MINUTES_TO_SHOW_AS_DAYS) {
      uptime = (minutes / (60 * 24) + " days");
    } else if (minutes >= MIN_MINUTES_TO_SHOW_AS_HOURS) {
      uptime = (minutes / 60 + " hours");
    } else {
      uptime = (minutes + " minutes");
    }
    return uptime;
  }

  public Map<String, SoundFile> getSoundMap() {
    return dispatcher.getAvailableSoundFiles();
  }

  public SoundboardDispatcher getDispatcher() {
    return this.dispatcher;
  }

  public Setting getSetting(String key) {
    return this.settings.get(key);
  }

  public boolean isMuted(Guild guild) {
    Member self = guild.getMemberById(bot.getSelfUser().getId());
    return (self.getVoiceState().isMuted());
  }

  public AudioTrackScheduler getSchedulerForGuild(Guild guild) {
    if (this.audioSchedulers.get(guild) == null) {
      AudioManager audio = guild.getAudioManager();
      AudioPlayer player = dispatcher.getAudioManager().createPlayer();
      audio.setSendingHandler(new AudioPlayerSendHandler(audio, player));
      AudioTrackScheduler scheduler = new AudioTrackScheduler(
        dispatcher.getAudioManager(), player);
      player.addListener(scheduler);
      audioSchedulers.put(guild, scheduler);
    }
    return this.audioSchedulers.get(guild);
  }

  public String getClosestMatchingSoundName(String str) {
    return dispatcher.getSoundNameTrie().getWordWithPrefix(str);
  }

  public String getRandomSoundName() {
    Random rng = new Random();
    SoundFile file = null;
    Map<String, Boolean> seen = new HashMap<>();
    Object[] files = dispatcher.getAvailableSoundFiles().values().toArray();
    while (file == null
           || file.isExcludedFromRandom()
           || file.getDuration() > MAX_DURATION_FOR_RANDOM) {
      // No sounds that can actually be played.
      if (seen.size() == files.length) return null;
      file = (SoundFile)files[rng.nextInt(files.length)];
      if (seen.get(file.getSoundFileId()) == null)
        seen.put(file.getSoundFileId(), true);
    }
    return file.getSoundFileId();
  }

  public String getRandomSoundNameForCategory(String category) {
    Random rng = new Random();
    Map<String, Boolean> seen = new HashMap<>();
    SoundFile file = null;
    Object[] files = dispatcher.getAvailableSoundFiles().values().toArray();
    while (file == null
           || !dispatcher.isASubCategory(file.getCategory(), category)
           || file.isExcludedFromRandom()
           || file.getDuration() > MAX_DURATION_FOR_RANDOM) {
      // No sounds that can actually be played.
      if (seen.size() == files.length) return null;
      file = (SoundFile)files[rng.nextInt(files.length)];
      if (seen.get(file.getSoundFileId()) == null)
        seen.put(file.getSoundFileId(), true);
    }
    return file.getSoundFileId();
  }

  public String getRandomTopPlayedSoundName(int maxDuration) {
    List<SoundFile> sounds = dispatcher.getSoundFilesOrderedByNumberOfPlays();
    Map<String, Boolean> seen = new HashMap<>();
    Map<String, SoundFile> map = getSoundMap();
    if (sounds == null || sounds.isEmpty()) return null;
    Random rng = new Random();
    SoundFile file = null;
    maxDuration = Math.min(MAX_DURATION_FOR_RANDOM, maxDuration);
    int top = Math.max(TOP_PLAYED_SOUND_THRESHOLD, sounds.size() / 20),
        index = rng.nextInt(Math.min(top, sounds.size())),
        ceiling = top;
    while (file == null
           || file.isExcludedFromRandom()
           || file.getDuration() > maxDuration
           || map.get(file.getSoundFileId()) == null) {
      // No sounds that can actually be played.
      if (seen.size() == sounds.size()) return null;
      index = rng.nextInt(Math.min(ceiling, sounds.size()));
      file = sounds.get(index);
      if (ceiling + 1 < sounds.size()) ++ceiling;
      if (seen.get(file.getSoundFileId()) == null)
        seen.put(file.getSoundFileId(), true);
    }
    return file.getSoundFileId();
  }

  public String getRandomTopPlayedSoundName() {
    return getRandomTopPlayedSoundName(MAX_DURATION_FOR_RANDOM);
  }

  public User getUser(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    if (users != null && !users.isEmpty()) {
      return users.get(0);
    } else {
      User u = new User(user.getId(), user.getName());
      dispatcher.saveUser(u);
      return u;
    }
  }

  public String getOwner() {
    return this.owner;
  }

  public boolean isAuthenticated(
    net.dv8tion.jda.core.entities.User user, Guild guild) {
    if (user.getName().equals(owner) || getUser(user).isPrivileged())
      return true;
    if (guild != null) {
      List<Role> roles = guild.getMember(user).getRoles();
      for (Role role : roles) {
        if (role.hasPermission(Permission.ADMINISTRATOR)
            || role.hasPermission(Permission.MANAGE_SERVER))
          return true;
      }
    } else {
      for (Guild guild_ : getGuildsWithUser(user)) {
        if (isAuthenticated(user, guild_)) return true;
      }
    }
    return false;
  }

  public boolean hasPermissionInChannel(TextChannel textChannel, Permission p) {
    if (textChannel == null || textChannel.getGuild() == null) return false;
    String id = bot.getSelfUser().getId();
    Member self = textChannel.getGuild().getMemberById(id);
    return self.hasPermission(p);
  }

  public boolean hasPermissionInVoiceChannel(
    VoiceChannel voiceChannel, Permission p) {
    if (voiceChannel == null || voiceChannel.getGuild() == null) return false;
    String id = bot.getSelfUser().getId();
    Member self = voiceChannel.getGuild().getMemberById(id);
    return self.hasPermission(p);
  }

  public String getBotName() {
    return this.bot.getSelfUser().getName();
  }

  public boolean isOwner(net.dv8tion.jda.core.entities.User user) {
    return (user.getName().equals(owner));
  }

  public VoiceChannel getConnectedChannel(Guild guild) {
    return (guild.getAudioManager().isConnected()) ?
           guild.getAudioManager().getConnectedChannel() : null;
  }

  public TextChannel getBotChannel(Guild guild) {
    if (guild == null) return null;
    List<TextChannel> botChannels = guild.getTextChannelsByName("bot", true);
    if (!botChannels.isEmpty()) return botChannels.get(0);
    return guild.getPublicChannel();
  }

  public net.dv8tion.jda.core.entities.User getUserByName(String username) {
    for (net.dv8tion.jda.core.entities.User user : bot.getUsers()) {
      if (user.getName().equalsIgnoreCase(username))
        return user;
    }
    return null;
  }

  public String getEntranceForUser(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    return (users != null && !users.isEmpty()) ?
           users.get(0).getEntrance() : null;
  }

  public void setEntranceForUser(
    net.dv8tion.jda.core.entities.User user, String filename,
    net.dv8tion.jda.core.entities.User by) {
    List<User> users = dispatcher.getUserById(user.getId());
    if (users != null && !users.isEmpty()) {
      users.get(0).setEntrance(filename);
      dispatcher.saveUser(users.get(0));
    } else {
      User u = new User(user.getId(), user.getName());
      u.setEntrance(filename);
      dispatcher.saveUser(u);
    }
    if (filename != null && !filename.isEmpty()) {
      if (by != null) {
        sendMessageToUser("Your entrance has been changed to `" + filename +
                          "` by **" + by.getName() +
                          "**. *Contact the bot owner to dispute this action" +
                          " if this action is being abused.*", user);
      }
      LOG.info("New entrance \"" + filename + "\" set for " + user.getName());
    } else {
      LOG.info("Cleared entrance associated with " + user.getName());
    }
  }

  public boolean disallowUser(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    if (users != null && !users.isEmpty() && users.get(0).isDisallowed()) {
      return true; // Already disallowed.
    } else {
      if (users == null || users.isEmpty()) { // Not already in records.
        dispatcher.registerUser(user, true, false);
      } else { // Already in records.
        users.get(0).setDisallowed(true);
        dispatcher.saveUser(users.get(0));
      }
      LOG.info("User " + user + " disallowed from playing sounds.");
      sendMessageToUser("You have been **disallowed** from playing sounds" +
                        " with this bot. *Contact the bot owner to dispute " +
                        "this action.*", user);
      return true;
    }
  }

  public boolean disallowUser(String username) {
    net.dv8tion.jda.core.entities.User user = getUserByName(username);
    return (user != null) ? disallowUser(user) : false;
  }

  public boolean allowUser(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    if (users != null && !users.isEmpty()) {
      if (users.get(0).isDisallowed()) {
        sendMessageToUser("You are no longer **disallowed** " +
                          "from playing sounds with this bot.", user);
      }
      users.get(0).setDisallowed(false);
      dispatcher.saveUser(users.get(0));
      return true;
    } return false;
  }

  public boolean allowUser(String username) {
    net.dv8tion.jda.core.entities.User user = getUserByName(username);
    return (user != null) ? allowUser(user) : false;
  }

  public boolean throttleUser(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    if (users != null && !users.isEmpty() && users.get(0).isThrottled()) {
      return true; // Already throttled.
    } else {
      if (users == null || users.isEmpty()) {
        dispatcher.registerUser(user, false, true);
      } else {
        users.get(0).setThrottled(true);
        dispatcher.saveUser(users.get(0));
      }
      LOG.info("User " + user + " throttled from playing sounds.");
      sendMessageToUser("You have been **throttled** from sending"
                        + " me commands. You will only be able to send me" +
                        " limited requests in a  period of time. " +
                        "*Contact the bot owner " + owner +
                        " to dispute this action.*", user);
      return true;
    }
  }

  public boolean throttleUser(String username) {
    net.dv8tion.jda.core.entities.User user = getUserByName(username);
    return (user != null) ? throttleUser(user) : false;
  }

  public boolean unthrottleUser(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    if (users != null && !users.isEmpty()) {
      if (users.get(0).isThrottled()) {
        sendMessageToUser(
          "You are no longer **throttled** from sending me commands.",
          user);
      }
      users.get(0).setThrottled(false);
      dispatcher.saveUser(users.get(0));
      return true;
    } return false;
  }

  public boolean unthrottleUser(String username) {
    net.dv8tion.jda.core.entities.User user = getUserByName(username);
    return (user != null) ? unthrottleUser(user) : false;
  }

  public void leaveServer(Guild guild) {
    if (guild != null && getGuilds().contains(guild)) {
      guild.getAudioManager().closeAudioConnection();
      guild.leave().queue();
    }
  }

  public void sendMessageToUser(String msg,
                                net.dv8tion.jda.core.entities.User user) {
    if (!user.hasPrivateChannel())
      try {
        user.openPrivateChannel().block();
      } catch (RateLimitedException e) { return; }
    user.getPrivateChannel().sendMessage(msg).queue();
  }


  public void sendMessageToUser(String msg, String username) {
    net.dv8tion.jda.core.entities.User user = getUserByName(username);
    if (isUser(user)) {
      LOG.fatal("Tried to send message to self with content \"" + msg + "\".");
      return;
    }
    if (user != null) sendMessageToUser(msg, user);
    else LOG.warn("Tried to send message \"" + msg + "\" to username " +
                    username + " but could not find user.");
  }

  public void sendMessageToAllGuilds(Message message) {
    for (Guild guild : bot.getGuilds()) {
      guild.getPublicChannel().sendMessage(message).queue();
    }
  }

  public void sendMessageToAllGuilds(String msg) {
    for (Guild guild : bot.getGuilds()) {
      guild.getPublicChannel().sendMessage(msg).queue();
    }
  }

  public void stopPlayingSound(Guild guild) {
    AudioManager audio = guild.getAudioManager();
    AudioTrackScheduler scheduler = getSchedulerForGuild(guild);
    AudioPlayer player = ((AudioPlayerSendHandler)(
                            audio.getSendingHandler())).getPlayer();
    player.stopTrack();
    List<String> files = scheduler.getIdentifiers();
    if (files.size() > 1) {
      for (String name : files) {
        File f = new File(name);
        String id = f.getName();
        id = id.substring(0, id.indexOf("."));
        SoundFile s = dispatcher.getAvailableSoundFiles().get(id);
        if (s != null) {
          s.subtractOneFromNumberOfPlays();
          dispatcher.saveSound(s);
        }
      }
    }
    scheduler.clear();
  }

  public void muteSound(Guild guild) {
    stopPlayingSound(guild);
    AudioManager audio = guild.getAudioManager();
    audio.setSelfMuted(true);
  }

  public void unmuteSound(Guild guild) {
    AudioManager audio = guild.getAudioManager();
    audio.setSelfMuted(false);
  }

  public String playRandomFile(net.dv8tion.jda.core.entities.User user) throws
    Exception {
    if (user != null && isAllowedToPlaySound(user)) {
      String toPlay = getRandomSoundName();
      VoiceChannel toJoin = getUsersVoiceChannel(user);
      if (toJoin == null) {
        sendMessageToUser(NOT_IN_VOICE_CHANNEL_MESSAGE, user);
      } else {
        moveToChannel(toJoin);
        playFile(toPlay, toJoin.getGuild());
        lastPlayed = new SoundPlayedEvent(toPlay, user.getName());
        return toPlay;
      }
    }
    return null;
  }

  public String playRandomFileForCategory(
    net.dv8tion.jda.core.entities.User user, String category) throws Exception {
    if (user != null && isAllowedToPlaySound(user)) {
      if (!isASoundCategory(category)) return null;
      VoiceChannel toJoin = getUsersVoiceChannel(user);
      if (toJoin == null) {
        sendMessageToUser(NOT_IN_VOICE_CHANNEL_MESSAGE, user);
      } else {
        String toPlay = getRandomSoundNameForCategory(category);
        if (toPlay == null) return null;
        moveToChannel(toJoin);
        playFile(toPlay, toJoin.getGuild());
        lastPlayed = new SoundPlayedEvent(toPlay, user.getName());
        return toPlay;
      }
    }
    return null;
  }

  public void playFileForChatCommand(
    String fileName, MessageReceivedEvent event) throws Exception {
    if (event != null && !fileName.isEmpty() &&
        isAllowedToPlaySound(event.getAuthor())) {
      if (dispatcher.getAvailableSoundFiles().get(fileName) != null) {
        VoiceChannel toJoin = getUsersVoiceChannel(event.getAuthor());
        if (toJoin == null) {
          sendMessageToUser(NOT_IN_VOICE_CHANNEL_MESSAGE, event.getAuthor());
        } else {
          moveToChannel(toJoin);
          playFile(fileName, toJoin.getGuild());
          lastPlayed =
            new SoundPlayedEvent(fileName, event.getAuthor().getName());
          LOG.info("Played sound \"" + fileName + "\" in server " +
                   toJoin.getGuild().getName());
        }
      }
    }
  }

  public void playURLForChatCommand(
    String url, MessageReceivedEvent event) throws Exception {
    if (event != null && !url.isEmpty()
        && isAllowedToPlaySound(event.getAuthor())) {
      VoiceChannel toJoin = getUsersVoiceChannel(event.getAuthor());
      if (toJoin == null) {
        sendMessageToUser(NOT_IN_VOICE_CHANNEL_MESSAGE, event.getAuthor());
      } else {
        moveToChannel(toJoin);
        playURL(url, toJoin.getGuild());
        LOG.info("Played url \"" + url + "\" in server " +
                 toJoin.getGuild().getName());
      }
    }
  }

  public String playFileForUser(String fileName,
                                net.dv8tion.jda.core.entities.User user) {
    if (user != null && !fileName.isEmpty()) {
      if (dispatcher.getAvailableSoundFiles().get(fileName) != null) {
        VoiceChannel channel = null;
        try {
          channel = getUsersVoiceChannel(user);
        } catch (Exception e) {
          LOG.fatal("Failed to search for user " + user +
                    " in a voice channel.");
        }
        if (channel == null) {
          LOG.warn("Problem moving to user " + user);
          return null;
        } else {
          moveToChannel(channel);
          playFile(fileName, channel.getGuild());
          lastPlayed = new SoundPlayedEvent(fileName, null);
          return dispatcher.getAvailableSoundFiles().get(
                   fileName).getSoundFileId();
        }
      }
    }
    return null;
  }

  public boolean playFileForEntrance(String fileName,
                                     net.dv8tion.jda.core.entities.User user,
                                     VoiceChannel joined) throws Exception {
    if (fileName == null) return false;
    AudioManager voice = joined.getGuild().getAudioManager();
    VoiceChannel connected = voice.getConnectedChannel();
    if ((voice.isConnected() || voice.isAttemptingToConnect()) &&
        connected != null &&
        (connected.equals(joined) || connected.getMembers().size() == 1) ||
        !voice.isConnected()) {
      if (joined.getGuild().getAfkChannel() != null &&
          joined.getId().equals(joined.getGuild().getAfkChannel().getId())) {
        LOG.info("User joined AFK channel so won't follow to play entrance.");
        return false;
      }
      if (connected == null || !connected.equals(joined))
        if (!moveToChannel(joined)) return false;
      LOG.info("Playing entrance \"" + fileName + "\" for user " +
               user.getName() + " in " + joined.getName());
      SoundFile fileToPlay = dispatcher.getAvailableSoundFiles().get(fileName);
      // Do not add to count for entrances.
      playFile(fileToPlay, joined.getGuild(), false);
      return true;
    }
    return false;
  }

  public boolean isAllowedToPlaySound(String username) {
    net.dv8tion.jda.core.entities.User user = getUserByName(username);
    return (user != null) ? isAllowedToPlaySound(user) : true;
  }

  public boolean isAllowedToPlaySound(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    return !(users != null && !users.isEmpty() && users.get(0).isDisallowed());
  }

  public boolean isThrottled(String username) {
    net.dv8tion.jda.core.entities.User user = getUserByName(username);
    return (user != null) ? isThrottled(user) : false;
  }

  public boolean isThrottled(net.dv8tion.jda.core.entities.User user) {
    List<User> users = dispatcher.getUserById(user.getId());
    return (users != null && !users.isEmpty() && users.get(0).isThrottled());
  }

  public boolean moveToChannel(VoiceChannel channel) {
    if (channel == null) return false;
    AudioManager voice = channel.getGuild().getAudioManager();
    if (voice.isConnected() && voice.getConnectedChannel() != null &&
        voice.getConnectedChannel().equals(channel))
      return false;
    if (!hasPermissionInVoiceChannel(channel, Permission.VOICE_CONNECT)) {
      LOG.info("Could not move to channel " + channel +
               " because no permission to join.");
      return false;
    }
    LOG.info("Moving to channel " + channel);
    try {
      if (voice.isConnected() && !voice.isAttemptingToConnect())
        voice.openAudioConnection(channel);
      else if (voice.isAttemptingToConnect())
        LOG.info("Still waiting to connect to channel " +
                 voice.getQueuedAudioConnection().getName());
      else {
        voice.openAudioConnection(channel);
        voice.setConnectTimeout(CHANNEL_CONNECTION_TIMEOUT);
      }
    } catch (Exception e) {
      voice.closeAudioConnection();
      LOG.warn("Closed audio connection because of an error.");
      e.printStackTrace();
    }
    return true;
  }

  public VoiceChannel getUsersVoiceChannel(
    net.dv8tion.jda.core.entities.User user) throws Exception {
    for (Guild guild : getGuildsWithUser(user)) {
      Member member = guild.getMemberById(user.getId());
      if (member != null && member.getVoiceState().getChannel() != null)
        return member.getVoiceState().getChannel();
    }
    return null; // Could not find this user in a voice channel.
  }

  public boolean isUser(net.dv8tion.jda.core.entities.User user) {
    net.dv8tion.jda.core.entities.User self = (
          net.dv8tion.jda.core.entities.User)bot.getSelfUser();
    return (self.equals(user) || self.getName().equals(user.getName()));
  }

  public boolean isASoundCategory(String category) {
    for (Category c : dispatcher.getCategories()) {
      if (c.getName().equalsIgnoreCase(category)) return true;
    }
    return false;
  }

  public Category getSoundCategory(String category) {
    return dispatcher.getCategory(category);
  }

  public SoundFile getLastPlayed() {
    return (lastPlayed == null) ? null : lastPlayed.getSoundFile();
  }

  public String getLastPlayedUsername() {
    return (lastPlayed == null) ? null : lastPlayed.getUsername();
  }

  public String getLastPlayedSoundInfo() {
    return (lastPlayed == null) ? null : lastPlayed.toString();
  }

  public Path getSoundsPath() {
    return Paths.get(System.getProperty("user.dir") + "/sounds");
  }

  public Path getTempPath() {
    return Paths.get(System.getProperty("user.dir") + "/tmp");
  }

  public List<Guild> getGuilds() {
    return bot.getGuilds();
  }

  public List<Guild> getGuildsWithUser(
    net.dv8tion.jda.core.entities.User user) {
    List<Guild> guilds = new LinkedList<>();
    for (Guild guild : getGuilds()) {
      List<Member> members = guild.getMembersByName(user.getName(), false);
      if (members != null && !members.isEmpty()) guilds.add(guild);
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

  public void clearPreviousMessages() {
    for (Guild guild : getGuilds()) {
      ChatUtils.clearBotMessagesInChannel(this, guild.getPublicChannel());
    }
  }

  public void playFile(String fileName, Guild guild) {
    SoundFile fileToPlay = dispatcher.getAvailableSoundFiles().get(fileName);
    playFile(fileToPlay, guild, true);
    lastPlayed = new SoundPlayedEvent(fileName, null);
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

  private void playFile(File audioFile, Guild guild) {
    AudioManager audio = guild.getAudioManager();
    if (audio.isSelfMuted()) return;
    AudioTrackScheduler scheduler = getSchedulerForGuild(guild);
    AudioPlayer player = ((AudioPlayerSendHandler)(
                            audio.getSendingHandler())).getPlayer();
    String path = audioFile.getPath();
    scheduler.load(path, new AudioHandler(player));
  }

  private void playURL(String url, Guild guild) {
    AudioManager audio = guild.getAudioManager();
    if (audio.isSelfMuted()) return;
    AudioTrackScheduler scheduler = getSchedulerForGuild(guild);
    AudioPlayer player = ((AudioPlayerSendHandler)(
                            audio.getSendingHandler())).getPlayer();
    LOG.info("Sending request for '" + url + "' to AudioManager");
    scheduler.load(url, new AudioHandler(player));
  }

  private void initializeDiscordBot(String token) {
    try {
      bot = new JDABuilder(AccountType.BOT).setToken(token).buildBlocking();
      ChatListener chatListener = new ChatListener(this);
      MoveListener moveListener = new MoveListener(this);
      GameListener gameListener = new GameListener(this);
      this.addListener(chatListener);
      this.addListener(moveListener);
      this.addListener(gameListener);
      this.chatListener = chatListener;
      LOG.info("Finished initializing bot with name " + getBotName());
      for (Guild guild : getGuilds())
        LOG.info("Connected to guild " + guild.getName());
    } catch (LoginException | IllegalArgumentException |
               InterruptedException | RateLimitedException e) {
      LOG.fatal("Could not initialize bot " + getBotName());
      e.printStackTrace();
    }
  }

}
