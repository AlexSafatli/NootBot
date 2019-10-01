package net.dirtydeeds.discordsoundboard.listeners;

import net.dirtydeeds.discordsoundboard.games.GameUpdateProcessor;
import net.dirtydeeds.discordsoundboard.games.GenericGameStartProcessor;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.user.UserGameUpdateEvent;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.util.LinkedList;
import java.util.List;

public class GameListener extends AbstractListener {

  public static final SimpleLog LOG = SimpleLog.getLog("Game");

  private List<GameUpdateProcessor> processors;

  public GameListener(SoundboardBot bot) {
    this.bot = bot;
    initializeProcessors();
  }

  private void initializeProcessors() {
    processors = new LinkedList<>();
    processors.add(new GenericGameStartProcessor(bot));
  }

  private void logGameChange(String name, Guild guild, Game previousGame,
                             Game currentGame) {
    String guildName = (guild != null) ? guild.getName() : null;
    if (currentGame == null && previousGame != null) {
      if (previousGame.getType().equals(GameType.STREAMING)) return;
      LOG.info(name + " stopped playing " + previousGame.getName() +
              " in server " + guildName + ".");
    } else if (currentGame != null && previousGame == null) {
      if (currentGame.getType().equals(GameType.STREAMING)) return;
      LOG.info(name + " started playing " + currentGame.getName() +
              " in server " + guildName + ".");
    } else if (currentGame != null) {
      if (currentGame.getType().equals(GameType.STREAMING) ||
              previousGame.getType().equals(GameType.STREAMING)) return;
      LOG.info(name + " changed to " + currentGame.getName() + " from " +
              previousGame.getName() + " in server " + guildName + ".");
    }
  }

  private void cacheGameName(Game game) {
    if (game == null || game.getType().equals(GameType.STREAMING) ||
            game.getName().equals("Spotify")) return;
    StringUtils.cacheWords(game.getName());
  }

  public void onUserGameUpdate(UserGameUpdateEvent event) {
    if (event.getUser().isBot()) return; // Ignore bots.

    User user = event.getUser();
    Guild guild = event.getGuild();
    Member member = guild.getMemberById(user.getId());

    String name = user.getName();
    Game previousGame = event.getPreviousGame(),
            currentGame = member.getGame();
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
