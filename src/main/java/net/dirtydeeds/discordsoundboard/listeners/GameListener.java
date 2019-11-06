package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.games.GameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.games.GenericGameStartProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.user.UserActivityUpdateEvent;
import net.dv8tion.jda.internal.utils.SimpleLogger;

import java.util.LinkedList;
import java.util.List;

public class GameListener extends AbstractListener {

  public static final SimpleLogger LOG = SimpleLogger.getLog("Game");

  private List<GameUpdateProcessor> processors;

  public GameListener(SoundboardBot bot) {
    this.bot = bot;
    initializeProcessors();
  }

  private void initializeProcessors() {
    processors = new LinkedList<>();
    processors.add(new GenericGameStartProcessor(bot));
  }

  private void logGameChange(String name, Guild guild, Activity previousGame,
                             Activity currentGame) {
    String guildName = (guild != null) ? guild.getName() : null;
    if (currentGame == null && previousGame != null) {
      if (previousGame.getType().equals(ActivityType.STREAMING)) return;
      LOG.info(name + " stopped playing " + previousGame.getName() +
              " in server " + guildName + ".");
    } else if (currentGame != null && previousGame == null) {
      if (currentGame.getType().equals(ActivityType.STREAMING)) return;
      LOG.info(name + " started playing " + currentGame.getName() +
              " in server " + guildName + ".");
    } else if (currentGame != null) {
      if (currentGame.getType().equals(ActivityType.STREAMING) ||
              previousGame.getType().equals(ActivityType.STREAMING)) return;
      LOG.info(name + " changed to " + currentGame.getName() + " from " +
              previousGame.getName() + " in server " + guildName + ".");
    }
  }

  private void cacheGameName(Activity game) {
    if (game == null || game.getType().equals(ActivityType.STREAMING) ||
            game.getName().equals("Spotify")) return;
    StringUtils.cacheWords(game.getName());
  }

  public void onUserActivityUpdate(UserActivityUpdateEvent event) {
    if (event.getUser().isBot()) return; // Ignore bots.

    User user = event.getUser();
    Guild guild = event.getGuild();
    Member member = guild.getMemberById(user.getId());

    String name = user.getName();
    Activity previousGame = event.getPreviousActivity(),
            currentActivity = member.getActivity();
    logGameChange(name, guild, previousGame, currentGame);
    cacheGameName(previousGame);
    cacheGameName(currentGame);

    for (GameUpdateProcessor processor : processors) {
      if (processor.isApplicableUpdateEvent(event, user)) {
        processor.process(event);
        LOG.info("Processed game update event with " + processor);
        if (processor.isMutuallyExclusive()) {
          LOG.info("That processor cannot be run with others. Stopping.");
          return;
        }
      }
    }
  }
}
