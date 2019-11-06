package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.chat.ChatCommandProcessor;
import net.dirtydeeds.discordsoundboard.chat.HelpProcessor;
import net.dirtydeeds.discordsoundboard.chat.NoOpProcessor;
import net.dirtydeeds.discordsoundboard.chat.StatsProcessor;
import net.dirtydeeds.discordsoundboard.chat.admin.*;
import net.dirtydeeds.discordsoundboard.chat.sounds.*;
import net.dirtydeeds.discordsoundboard.chat.users.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.client.events.relationship.FriendRequestReceivedEvent;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.internal.utils.SimpleLogger;

import java.util.*;

public class ChatListener extends AbstractListener {

  public static final SimpleLogger LOG = SimpleLogger.getLog("Chat");
  private static final char CommonPrefix = '.';

  private static final int THROTTLE_TIME_IN_MINUTES = 1;
  private static final int MAX_NUMBER_OF_REQUESTS_PER_TIME = 3;
  private static final int EXCESSIVE_NUMBER_OF_REQUESTS_PER_TIME = 25;

  private Date tick;
  private int totalRequests;
  private Map<User, Integer> requests;
  private List<ChatCommandProcessor> processors;

  public ChatListener(SoundboardBot bot) {
    this.bot = bot;
    this.tick = new Date(System.currentTimeMillis());
    this.requests = new HashMap<>();
    this.processors = new LinkedList<>();
    setupChatProcessors();
  }

  private String withPrefix(String command) {
    return CommonPrefix + command;
  }

  private void setupChatProcessors() {
    processors.add(new PlaySoundProcessor("?", bot));
    processors.add(new PlayUrlProcessor("~~", bot));
    processors.add(new ListSoundsProcessor(withPrefix("list"), bot));
    processors.add(new SearchProcessor(withPrefix("search"), bot));
    processors.add(new ListCategoriesProcessor(withPrefix("categories"), bot));
    processors.add(new ListNewSoundsProcessor(withPrefix("new"), bot));
    processors.add(new ListTopSoundsProcessor(withPrefix("top"), bot));
    processors.add(new ListLowSoundsProcessor(withPrefix("least"), bot));
    processors.add(new ListMostReportsProcessor(withPrefix("controversial"), bot));
    processors.add(new ListLongestSoundsProcessor(withPrefix("longest"), bot));
    processors.add(new ListShortestSoundsProcessor(withPrefix("shortest"), bot));
    processors.add(new PlayRandomTopSoundProcessor(withPrefix("randomtop"), bot));
    processors.add(new PlayRandomProcessor(withPrefix("random"), bot));
    processors.add(new AuthenticateUserProcessor(withPrefix("privilege"), bot));
    processors.add(new DeleteBotMessagesProcessor(withPrefix("clear"), bot));
    processors.add(new FavoritePhraseProcessor(withPrefix("addphrase"), bot));
    processors.add(new UnfavoritePhraseProcessor(withPrefix("rmphrase"), bot));
    processors.add(new SetGameNameProcessor(withPrefix("gamename"), bot));
    processors.add(new DisallowUserProcessor(withPrefix("disallow"), bot));
    processors.add(new AllowUserProcessor(withPrefix("allow"), bot));
    processors.add(new LimitUserProcessor(withPrefix("throttle"), bot));
    processors.add(new RemoveLimitUserProcessor(withPrefix("unthrottle"), bot));
    processors.add(new MuteSoundProcessor(withPrefix("mute"), bot));
    processors.add(new UnmuteSoundProcessor(withPrefix("unmute"), bot));
    processors.add(new DeleteSoundProcessor(withPrefix("rm"), bot));
    processors.add(new RenameSoundProcessor(withPrefix("rename"), bot));
    processors.add(new RecategorizeSoundProcessor(withPrefix("mv"), bot));
    processors.add(new DownloadSoundProcessor(withPrefix("dl"), bot));
    processors.add(new ExcludeSoundFromRandomProcessor(withPrefix("exclude"), bot));
    processors.add(new IncludeSoundFromRandomProcessor(withPrefix("include"), bot));
    processors.add(new SetEntranceForUserProcessor(withPrefix("entrancefor"), bot));
    processors.add(new SetEntranceProcessor(withPrefix("entrance"), bot));
    processors.add(new PlaySoundForUserProcessor(withPrefix("playfor"), bot));
    processors.add(new StopSoundProcessor(withPrefix("stop"), bot));
    processors.add(new RestartBotProcessor(withPrefix("restart"), bot));
    processors.add(new UpdateSoundsProcessor(withPrefix("refresh"), bot));
    processors.add(new ListServersProcessor(withPrefix("servers"), bot));
    processors.add(new UserInfoProcessor(withPrefix("user"), bot));
    processors.add(new StatsProcessor(withPrefix("about"), bot));
    processors.add(new SettingProcessor(withPrefix("setting"), bot));
    processors.add(new NewTopicRoleProcessor(withPrefix("modtopic"), bot));
    processors.add(new SuspendUserProcessor(withPrefix("modsuspend"), bot));
    processors.add(new SoundAttachmentProcessor(bot));
    processors.add(new FilterTwitchClipProcessor(bot));
    processors.add(new FilterYoutubeClipProcessor(bot));

    LOG.info("Registered " + processors.size() + " processors.");
  }

  private void updateTick() {
    Date now = new Date(System.currentTimeMillis());
    long minutesSince = (now.getTime() - tick.getTime()) / (1000 * 60);
    if (minutesSince >= THROTTLE_TIME_IN_MINUTES) {
      this.tick = now;
      if (requests.size() > 0) {
        LOG.info(minutesSince +
                " min have passed. Clearing request counts for " +
                requests.size() + " users (" + totalRequests +
                " total requests).");
        requests.clear();
        totalRequests = 0;
      }
    }
  }

  private void process(ChatCommandProcessor processor,
                       MessageReceivedEvent event) {
    User user = event.getAuthor();
    Integer numRequests = requests.get(user);
    if (numRequests == null) numRequests = 0;

    if (numRequests >= MAX_NUMBER_OF_REQUESTS_PER_TIME
            && bot.isThrottled(user)) {
      bot.sendMessageToUser(
              "Please wait before sending another command.", user);
      return;
    } else if (numRequests >= EXCESSIVE_NUMBER_OF_REQUESTS_PER_TIME
            && !bot.isOwner(user)) {
      LOG.info("Throttling user " + user.getName() +
              " because sent too many requests.");
      bot.throttleUser(user);
      bot.sendMessageToUser("Throttling **" + user.getName() +
                      "** automatically because too many requests.",
              bot.getOwner());
      return;
    }

    processor.process(event);
    requests.put(user, numRequests + 1); // Increment number of requests.
    ++totalRequests;
    LOG.info("Processed message using " + processor.getClass().getSimpleName() +
            " for user " + user.getName() + " with content \"" +
            event.getMessage().getContent() + "\" (request: " + totalRequests +
            ", bot: " + bot.getBotName() + ").");
  }

  public void onMessageReceived(MessageReceivedEvent event) {

    updateTick();

    // See if a help command first. Process it here if that is the case.
    // This does not count against requests.
    if (event.getMessage().getContent().toLowerCase().equals(withPrefix("help"))) {
      HelpProcessor help = new HelpProcessor(bot, processors);
      help.process(event);
      return;
    }

    // Respond to a particular message using a processor otherwise.
    for (ChatCommandProcessor processor : processors) {
      if (processor.isApplicableCommand(event)) {
        process(processor, event);
        return;
      }
    }

    // Handle typo commands with common prefix.
    if (isTypoCommand(event)) {
      bot.sendMessageToUser("That is not a command.", event.getAuthor());
      (new NoOpProcessor(bot)).process(event); // Do nothing - deletes the message.
      LOG.info("User " + event.getAuthor().getName() +
              " tried to run \"" + event.getMessage().getContent() +
              "\" which is not a command.");
    }

  }

  public void onFriendRequestReceived(FriendRequestReceivedEvent event) {
    // If the friending user is the owner, add him or her.
    LOG.info("Received friend request from " + event.getUser().getName());
    if (event.getUser().equals(bot.getUserByName(bot.getOwner()))) {
      LOG.info("Accepting friend request.");
      event.getFriendRequest().accept().queue();
    } else {
      LOG.info("Ignoring friend request.");
      event.getFriendRequest().ignore().queue();
    }
  }

  private boolean isTypoCommand(MessageReceivedEvent event) {
    String content = event.getMessage().getContent(),
            prefix = CommonPrefix + "";
    return (content.length() > 1
            && content.startsWith(prefix)
            && !content.substring(1).contains(prefix)
            && !StringUtils.containsOnly(content, CommonPrefix));
  }

}
