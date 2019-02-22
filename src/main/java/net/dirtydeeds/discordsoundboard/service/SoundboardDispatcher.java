package net.dirtydeeds.discordsoundboard.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.text.DecimalFormat;
import java.util.function.Consumer;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import net.dirtydeeds.discordsoundboard.async.*;
import net.dirtydeeds.discordsoundboard.beans.Phrase;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.dao.PhraseRepository;
import net.dirtydeeds.discordsoundboard.dao.SoundFileRepository;
import net.dirtydeeds.discordsoundboard.dao.UserRepository;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.trie.LowercaseTrie;
import net.dirtydeeds.discordsoundboard.utils.StringUtils;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.SimpleLog;

@Service
public class SoundboardDispatcher {

  public static final SimpleLog LOG = SimpleLog.getLog("Dispatcher");

  private final UserRepository userDao;
  private final SoundFileRepository soundDao;
  private final PhraseRepository phraseDao;

  private Properties appProperties;
  private SoundboardBot[] bots;
  private AudioPlayerManager audioManager;
  private Map<String, SoundFile> availableSounds;
  private LowercaseTrie soundNameTrie;
  private Category categoryTree;
  private int numCategories;

  private final AsyncService asyncService;
  private final StringService stringService;

  private final Path soundFilePath =
          Paths.get(System.getProperty("user.dir") + "/sounds");
  private final Path tmpFilePath =
          Paths.get(System.getProperty("user.dir") + "/tmp");

  private static final List<String> STARTING_PHRASES = Arrays.asList(
          "Forza Battlegrounds", "World of American Trucks",
          "Black Space Online", "Nioh: Injustice Offensive",
          "Tom Clancy's Farming Simulator");
  private static final String[] UNITS = new String[]{
          "B", "KB", "MB", "GB", "TB"
  };
  private static final String LIBRARY_TOO_BIG = "**CANNOT COMPUTE**";

  @Inject
  public SoundboardDispatcher(UserRepository userDao,
                              SoundFileRepository soundDao,
                              PhraseRepository phraseDao,
                              AsyncService asyncService,
                              StringService stringService) {
    this.asyncService = asyncService;
    this.stringService = stringService;
    this.userDao = userDao;
    this.soundDao = soundDao;
    this.phraseDao = phraseDao;
    audioManager = new DefaultAudioPlayerManager();
    availableSounds = new TreeMap<>();
    soundNameTrie = new LowercaseTrie();
    updateFileList();
    loadProperties();
    startServices();
    this.asyncService.maintain(this);
    this.stringService.maintain();
  }

  public AudioPlayerManager getAudioManager() {
    return audioManager;
  }

  public Map<String, SoundFile> getAvailableSoundFiles() {
    return availableSounds;
  }

  public LowercaseTrie getSoundNameTrie() {
    return soundNameTrie;
  }

  // Loads in the properties from the app.properties file
  private void loadProperties() {
    appProperties = new Properties();
    InputStream stream = null;
    try {
      stream = new FileInputStream(System.getProperty("user.dir") +
              "/app.properties");
      appProperties.load(stream);
      stream.close();
      return;
    } catch (FileNotFoundException e) {
      LOG.warn("Could not find app.properties file.");
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (stream == null) {
      LOG.warn("Loading app.properties file from resources folder");
      try {
        stream = this.getClass().getResourceAsStream("/app.properties");
        appProperties.load(stream);
        stream.close();
      } catch (IOException e) {
        LOG.fatal("Could not load properties.");
        e.printStackTrace();
      }
    }
  }

  private void shutdownBot(int i) {
    int index = i - 1;
    if (bots[index] == null) return;
    LOG.info("Shutting down bot " + i + ": " + bots[index].getBotName());
    for (Object listener : bots[index].getAPI().getRegisteredListeners()) {
      bots[index].getAPI().removeEventListener(listener);
    }
    bots[index].getAPI().shutdown();
    bots[index] = null;
  }

  private void startBot(int i) {
    SoundboardBot bot = null;
    int index = i - 1;
    if (bots[index] != null) shutdownBot(i);
    String token = getProperty("token_" + i),
            owner = getProperty("owner_" + i);
    if (token == null || owner == null) {
      LOG.fatal("Config not populated! Need API token and owner for bot " + i);
      return;
    } else {
      LOG.info("Initializing bot " + i +
              " (token: " + token + ", owner: " + owner + ")");
    }
    try {
      bot = new SoundboardBot(token, owner, this);
    } catch (Exception e) {
      LOG.warn("When starting bot " + i + ", ran into exception => " +
              e.getMessage());
    }
    bots[index] = bot;
  }

  public void restartBot(SoundboardBot bot) {
    LOG.info("Restarting bot " + bot.getBotName());
    for (int i = 0; i < bots.length; ++i) {
      if (bots[i] != null && bots[i].equals(bot)) {
        startBot(i + 1);
      }
    }
  }

  private void startServices() {
    // Add some starting phrases to the phrase repository.
    addStartingPhrases();
    // Bots
    int num = Integer.valueOf(getProperty("number_of_users"));
    LOG.info("Starting " + num + " bots.");
    bots = new SoundboardBot[num];
    for (int i = 1; i <= num; ++i) startBot(i);
    // Audio Playing
    LOG.info("Adding sources to audio manager.");
    audioManager.registerSourceManager(new YoutubeAudioSourceManager());
    audioManager.registerSourceManager(new TwitchStreamAudioSourceManager());
    audioManager.registerSourceManager(new HttpAudioSourceManager());
    audioManager.registerSourceManager(new LocalAudioSourceManager());
    AudioSourceManagers.registerRemoteSources(audioManager);
    // String files.
    LOG.info("Reading string files.");
    stringService.addFile(
            Paths.get(System.getProperty("user.dir") + "/strings.txt")
    );
    // Async jobs
    LOG.info("Starting async jobs.");
    asyncService.addJob(PeriodicLambdas.cleanOldBotMessages());
    asyncService.addJob(PeriodicLambdas.changeToRandomGame());
  }

  // This method loads the files. This checks if you are running from a .jar
  // file and loads from the /sounds dir relative
  // to the jar file. If not it assumes you are running from code and loads
  // relative to your resource dir.
  private void getFileList() {
    Map<String, SoundFile> sounds = new TreeMap<>();
    try {
      if (!soundFilePath.toFile().exists())
        soundFilePath.toFile().mkdir();
      if (!tmpFilePath.toFile().exists())
        tmpFilePath.toFile().mkdir();

      Files.walk(soundFilePath).forEach(filePath -> {
        if (Files.isRegularFile(filePath)) {
          String name = filePath.getFileName().toString();
          name = name.substring(name.indexOf("/") + 1);
          name = name.substring(0, name.indexOf("."));

          File file = filePath.toFile();
          String parent = file.getParentFile().getName(); // Category name.
          SoundFile soundFile = new SoundFile(name, filePath.toFile(), parent);
          SoundFile db = soundDao.findOne(name);
          if (db != null) {
            // Resolve conflicts between persistence object and new object.
            soundFile.setNumberOfPlays(db.getNumberOfPlays());
            soundFile.setNumberOfReports(db.getNumberOfReports());
            soundFile.setExcludedFromRandom(db.isExcludedFromRandom());
          }
          saveSound(soundFile);
          sounds.put(name, soundFile);
        }
      });
      availableSounds = sounds;
      LOG.info("Instantiating trie with " + availableSounds.size() +
              " sound names.");
      soundNameTrie = new LowercaseTrie(sounds.keySet());
    } catch (Exception e) {
      LOG.fatal(e.toString());
      e.printStackTrace();
    }
  }

  private void getCategoryList(Path path, Category cursor) {
    try {
      Files.list(path).forEach(child -> {
        if (Files.isDirectory(child)) {
          String name = child.toFile().getName();
          Category category = new Category(name, child);
          ++numCategories;
          cursor.getChildren().add(category);
          getCategoryList(child, category);
        }
      });
    } catch (IOException e) {
      LOG.fatal(e.toString());
      e.printStackTrace();
    }
  }

  public AsyncService getAsyncService() {
    return this.asyncService;
  }

  public List<SoundboardBot> getBots() {
    List<SoundboardBot> bots = new LinkedList<>();
    for (int i = 0; i < this.bots.length; ++i) {
      if (this.bots[i] != null) bots.add(this.bots[i]);
    }
    return bots;
  }

  public void runLambda(Consumer<SoundboardBot> lambda) {
    for (int i = 0; i < this.bots.length; ++i) {
      if (this.bots[i] != null) {
        try {
          lambda.accept(this.bots[i]);
          LOG.info("Ran lambda using bot " + i + ": " +
                  this.bots[i].getBotName());
        } catch (Exception e) {
          LOG.warn("When running lambda: " + e.getMessage());
          e.printStackTrace();
        }
      } else {
        LOG.warn("Bot " + i + " was null when trying to run lambda.");
      }
    }
  }

  public List<net.dirtydeeds.discordsoundboard.beans.User> getUserById(
          String userid) {
    return userDao.findByUserid(userid);
  }

  public List<net.dirtydeeds.discordsoundboard.beans.User> getUsersWithEntrance(
          String entrance) {
    return userDao.findAllByEntrancefilename(entrance);
  }

  public List<Phrase> getPhrase(String p) {
    return phraseDao.findByValue(p);
  }

  public List<String> getPhrases() {
    List<String> out = new LinkedList<>();
    List<Phrase> phrases = phraseDao.findAll();
    for (Phrase phrase : phrases) {
      out.add(phrase.getValue());
      StringUtils.cacheWords(phrase.getValue());
    }
    return out;
  }

  public List<SoundFile> getSoundFilesOrderedByNumberOfPlays() {
    return soundDao.findAllByOrderByNumberPlaysDesc();
  }

  public List<SoundFile> getSoundFilesOrderedByDuration() {
    return soundDao.findAllByOrderByDurationDesc();
  }

  public List<SoundFile> getSoundFilesOrderedByNumberOfReports() {
    return soundDao.findAllByOrderByNumberReportsDesc();
  }

  public SoundFile getSoundFileByName(String name) {
    return availableSounds.get(name);
  }

  public Properties getAppProperties() {
    return appProperties;
  }

  public String getProperty(String key) {
    return (appProperties != null) ? appProperties.getProperty(key) : null;
  }

  public StringService getStringService() {
    return this.stringService;
  }

  public Category getCategoryTree() {
    return this.categoryTree;
  }

  public int getNumberOfCategories() {
    return this.numCategories;
  }

  public Collection<Category> getCategories() {
    return getCategories(categoryTree);
  }

  private Collection<Category> getCategories(Category category) {
    Collection<Category> categories = new LinkedList<>();
    for (Category c : category.getChildren()) {
      categories.add(c);
      categories.addAll(getCategories(c));
    }
    return categories;
  }

  public Category getCategory(String name) {
    for (Category category : getCategories())
      if (category.getName().equalsIgnoreCase(name)) return category;
    return null;
  }

  public boolean isASubCategory(String subcategory, String category) {
    if (category.equalsIgnoreCase(subcategory))
      return true;
    Category _category = getCategory(category);
    for (Category child : _category.getChildren()) {
      if (subcategory.equalsIgnoreCase(child.getName())
              || isASubCategory(subcategory, child.getName())) {
        LOG.info("Category " + subcategory + " is subcategory to " + category);
        return true;
      }
    }
    return false;
  }

  public net.dirtydeeds.discordsoundboard.beans.User registerUser(
          User user, boolean disallowed, boolean throttled) {
    net.dirtydeeds.discordsoundboard.beans.User u =
            new net.dirtydeeds.discordsoundboard.beans.User(
                    user.getName(), user.getId());
    u.setDisallowed(disallowed);
    u.setThrottled(throttled);
    saveUser(u);
    return u;
  }

  public void saveUser(net.dirtydeeds.discordsoundboard.beans.User user) {
    userDao.saveAndFlush(user);
  }

  public void saveSound(SoundFile soundFile) {
    soundDao.saveAndFlush(soundFile);
  }

  public void savePhrase(Phrase phrase) {
    phraseDao.saveAndFlush(phrase);
  }

  public Phrase registerPhrase(String phrase) {
    Phrase p = new Phrase(phrase);
    savePhrase(p);
    return p;
  }

  public void addPhrase(String phrase) {
    List<Phrase> phrases = getPhrase(phrase);
    if (phrases != null && !phrases.isEmpty()) {
      return; // Already present.
    }
    registerPhrase(phrase);
  }

  public boolean removePhrase(String phrase) {
    List<Phrase> phrases = phraseDao.deleteByValue(phrase);
    return (phrases != null && !phrases.isEmpty());
  }

  private void addStartingPhrases() {
    for (String phrase : STARTING_PHRASES) {
      addPhrase(phrase);
    }
  }

  public void updateFileList() {
    categoryTree = new Category("", soundFilePath);
    numCategories = 0;
    LOG.info("Getting list of files.");
    getFileList();
    LOG.info("Getting list of categories.");
    getCategoryList(soundFilePath, categoryTree);
  }

  private long getFolderSize(File target) {
    long len = 0;
    File[] files = target.listFiles();
    for (int i = 0; i < files.length; ++i) {
      if (files[i].isFile()) len += files[i].length();
      else len += getFolderSize(files[i]);
    }
    return len;
  }

  public String sizeOfLibrary() {
    long size = getFolderSize(soundFilePath.toFile());
    int index = (int) (Math.log10(size) / 3);
    if (index >= UNITS.length) {
      return LIBRARY_TOO_BIG;
    }
    double val = 1 << (index * 10);
    return new DecimalFormat("#,##0.#").format(size / val) + " " + UNITS[index];
  }

}
