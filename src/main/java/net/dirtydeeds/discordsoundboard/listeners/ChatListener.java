package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.chat.*;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ChatListener extends AbstractListener {

  public static final SimpleLog LOG = SimpleLog.getLog("Chat");
  public static final char CommonPrefix = '.';

  private static final int THROTTLE_TIME_IN_MINUTES = 1;
  private static final int MAX_NUMBER_OF_REQUESTS_PER_TIME = 3;
  private static final int EXCESSIVE_NUMBER_OF_REQUESTS_PER_TIME = 25;

  private Date tick;
  private int totalRequests;
  private Map<User, Integer> requests;
  private List<ChatCommandProcessor> processors;
  private HelpProcessor helpProcessor;
  private NoOpProcessor noOpProcessor;

  public ChatListener(SoundboardBot soundPlayer) {
    this.bot = soundPlayer;
    this.tick = new Date(System.currentTimeMillis());
    this.requests = new HashMap<>();
    this.processors = new LinkedList<>();
    initializeProcessors();
  }

//    private ChatCommandProcessor WithCommonPrefix(Class<ChatCommandProcessor> c, String prefix) {
//      return c.newInstance(CommonPrefix + prefix, bot);
//    }

  private void initializeProcessors() {

    processors.add(new PlaySoundProcessor ("?",                       bot));
    processors.add(new ListSoundsProcessor(".list",                   bot));
    processors.add(new SearchProcessor(".search",                     bot));
    processors.add(new DescribeSoundProcessor(".whatis",              bot));
    processors.add(new LastPlayedSoundProcessor(".lastplayed",        bot));
    processors.add(new ListCategoriesProcessor(".categories",         bot));
    processors.add(new ListNewSoundsProcessor(".new",                 bot));
    processors.add(new ListTopSoundsProcessor(".top",                 bot));
    processors.add(new ListLowSoundsProcessor(".least",               bot));
    processors.add(new ListMostReportsProcessor(".controversial",     bot));
    processors.add(new ListLongestSoundsProcessor(".longest",         bot));
    processors.add(new ListShortestSoundsProcessor(".shortest",       bot));
    processors.add(new PlayRandomSoundLoopedProcessor(".shuffle",     bot));
    processors.add(new PlayRandomTopSoundProcessor(".randomtop",      bot));
    processors.add(new PlayRandomProcessor(".random",                 bot));
    processors.add(new SetNicknameProcessor(".nickname",              bot));
    processors.add(new AuthenticateUserProcessor(".privilege",        bot));
    processors.add(new DeleteBotMessagesProcessor(".clear",           bot));
    processors.add(new DisallowUserProcessor(".disallow",             bot));
    processors.add(new AllowUserProcessor(".allow",                   bot));
    processors.add(new LimitUserProcessor(".throttle",                bot));
    processors.add(new RemoveLimitUserProcessor(".unthrottle",        bot));
    processors.add(new MuteSoundProcessor(".mute",                    bot));
    processors.add(new UnmuteSoundProcessor(".unmute",                bot));
    processors.add(new DeleteSoundProcessor(".rm",                    bot));
    processors.add(new ReportSoundProcessor(".report",                bot));
    processors.add(new RenameSoundProcessor(".rename",                bot));
    processors.add(new RecategorizeSoundProcessor(".mv",              bot));
    processors.add(new DownloadSoundProcessor(".dl",                  bot));
    processors.add(new ExcludeSoundFromRandomProcessor(".exclude",    bot));
    processors.add(new IncludeSoundFromRandomProcessor(".include",    bot));
    processors.add(new ModifyAllSoundPlayCountProcessor(".countall",  bot));
    processors.add(new ModifySoundPlayCountProcessor(".count",        bot));
    processors.add(new SetEntranceForUserProcessor(".entrancefor",    bot));
    processors.add(new SetEntranceProcessor(".entrance",              bot));
    processors.add(new PlaySoundForUserProcessor(".playfor",          bot));
    processors.add(new PlaySoundSequenceProcessor(".play",            bot));
    processors.add(new StopSoundProcessor(".stop",                    bot));
    processors.add(new PlaySoundLoopedProcessor(".loop",              bot));
    processors.add(new ServerMessageProcessor(".all",                 bot));
    processors.add(new LeaveServerProcessor(".leave",                 bot));
    processors.add(new ExecProcessor(".run",                          bot));
    processors.add(new RestartBotProcessor(".restart",                bot));
    processors.add(new UpdateSoundsProcessor(".refresh",              bot));
    processors.add(new ListServersProcessor(".servers",               bot));
    processors.add(new UserInfoProcessor(".user",                     bot));
    processors.add(new AlternateHandleProcessor(".alt",               bot));
    processors.add(new RollDiceProcessor(".roll",                     bot));
    processors.add(new StatsProcessor(".about",                       bot));
    processors.add(new ReportBugProcessor(".bug",                     bot));
    processors.add(new InviteBotProcessor(".invite",                  bot));
    processors.add(new ServerDonationMessageProcessor(".donate",      bot));
    processors.add(new SoundAttachmentProcessor(                      bot));
    processors.add(new FilterTwitchClipProcessor(                     bot));

    this.helpProcessor = new HelpProcessor(".help", bot, processors);
    this.noOpProcessor = new NoOpProcessor(bot);

  }

  private void updateTick() {
    Date now = new Date(System.currentTimeMillis());
    long minutesSince = (now.getTime() - tick.getTime()) / (1000 * 60);
    if (minutesSince >= THROTTLE_TIME_IN_MINUTES) {
      this.tick = now;
      if (requests.size() > 0) {
        LOG.info(minutesSince + " min have passed. Clearing request counts for " +
                 requests.size() + " users (" + totalRequests + " total requests).");
        requests.clear();
        totalRequests = 0;
      }
    }
  }

  private void process(ChatCommandProcessor processor, MessageReceivedEvent event) {
    User user = event.getAuthor();

    // Get number of requests for this user.
    Integer numRequests = requests.get(user);
    if (numRequests == null) numRequests = 0;

    if (numRequests >= MAX_NUMBER_OF_REQUESTS_PER_TIME && bot.isThrottled(user)) {
      bot.sendMessageToUser("Please wait a little before sending another command.", user);
      return;
    } else if (numRequests >= EXCESSIVE_NUMBER_OF_REQUESTS_PER_TIME && !bot.isOwner(user)) {
      LOG.info("Throttling user " + user.getName() + " because they sent too many requests.");
      bot.throttleUser(user);
      bot.sendMessageToUser("Throttling **" + user.getName() +
                            "** automatically because of too many requests.", bot.getOwner());
      return;
    }

    processor.process(event);
    requests.put(user, numRequests + 1); // Increment number of requests.
    ++totalRequests;
    LOG.info("Processed chat message event with processor " +
             processor.getClass().getSimpleName() + " for user " +
             user.getName() + " with content \"" +
             event.getMessage().getContent() + "\".");
  }

  public void onMessageReceived(MessageReceivedEvent event) {

    updateTick();

    // See if a help command first. Process it here if that is the case. This does not count against requests.
    if (helpProcessor.isApplicableCommand(event)) {
      helpProcessor.process(event);
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
      bot.sendMessageToUser("That's not one of my commands! *Check your spelling*. \u2014 Use `.help` to see all commands.", event.getAuthor());
      noOpProcessor.process(event); // Do nothing - deletes the message.
      LOG.info("User " + event.getAuthor().getName() +
               " tried to run \"" + event.getMessage().getContent() +
               "\" which is not a command.");
      return;
    }

  }

  private boolean isTypoCommand(MessageReceivedEvent event) {
    String content = event.getMessage().getContent(), prefix = CommonPrefix + "";
    return (content.length() > 1 && content.startsWith(prefix) && !content.substring(1).contains(prefix));
  }

}
