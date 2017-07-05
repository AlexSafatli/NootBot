package net.dirtydeeds.discordsoundboard.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
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

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.dao.SoundFileRepository;
import net.dirtydeeds.discordsoundboard.dao.UserRepository;
import net.dirtydeeds.discordsoundboard.org.Category;
import net.dirtydeeds.discordsoundboard.trie.LowercaseTrie;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.SimpleLog;

/**
 * @author asafatli.
 *
 *         Allows for multiple copies of this service across >= 1 Discord
 *         accounts.
 */
@Service
public class SoundboardDispatcher {

	public static final SimpleLog LOG = SimpleLog.getLog("Dispatcher");

	private final UserRepository userDao;
	private final SoundFileRepository soundDao;

	private Properties appProperties;
	private SoundboardBot[] bots;
	private AudioPlayerManager audioManager;
	private Map<String, SoundFile> availableSounds;
	private LowercaseTrie soundNameTrie;
	private Category categoryTree;
	private int numCategories;
	private final AsyncService asyncService;
	private final StringService stringService;
	private final Path soundFilePath = Paths.get(System.getProperty("user.dir") + "/sounds");
	private final Path tmpFilePath = Paths.get(System.getProperty("user.dir") + "/tmp");

	@Inject
	public SoundboardDispatcher(UserRepository userDao, SoundFileRepository soundDao, AsyncService asyncService,
			StringService stringService) {
		this.asyncService = asyncService;
		this.stringService = stringService;
		this.userDao = userDao;
		this.soundDao = soundDao;
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
	
	/**
	 * Gets a Map of the loaded sound files.
	 * 
	 * @return Map of sound files that have been loaded.
	 */
	public Map<String, SoundFile> getAvailableSoundFiles() {
		return availableSounds;
	}

	public LowercaseTrie getSoundNameTrie() {
		return soundNameTrie;
	}

	private void startBot(int i) {
		int index = i - 1;
		if (bots[index] != null) {
			List<Object> listeners = bots[index].getAPI().getRegisteredListeners();
			for (Object listener : listeners) {
				bots[index].getAPI().removeEventListener(listener);
			}
			bots[index].getAPI().shutdown(false);
			bots[index] = null;
		}
		String token = appProperties.getProperty("token_" + i);
		String owner = appProperties.getProperty("owner_" + i);
		if (token == null || owner == null) {
			LOG.fatal("The config was not populated! Please enter both an API token and owner for bot " + i);
			return;
		} else {
			LOG.info("Initializing bot " + i);
		}
		SoundboardBot bot = new SoundboardBot(token, owner, this);
		bots[index] = bot;
	}

	private void startServices() {
		int num = Integer.valueOf(appProperties.getProperty("number_of_users"));
		// Bots
		LOG.info("Starting bots.");
		bots = new SoundboardBot[num];
		for (int i = 1; i <= num; ++i)
			startBot(i);
		// Async jobs
		// LOG.info("Starting async jobs.");
		// Audio Playing
		LOG.info("Adding sources to audio manager.");
		audioManager.registerSourceManager(new YoutubeAudioSourceManager());
		audioManager.registerSourceManager(new TwitchStreamAudioSourceManager());
		audioManager.registerSourceManager(new HttpAudioSourceManager());
		audioManager.registerSourceManager(new LocalAudioSourceManager());
		AudioSourceManagers.registerRemoteSources(audioManager);
		// String files.
		LOG.info("Reading string files.");
		stringService.addFile(Paths.get(System.getProperty("user.dir") + "/strings.txt"));
	}

	// Loads in the properties from the app.properties file
	private void loadProperties() {
		appProperties = new Properties();
		InputStream stream = null;
		try {
			stream = new FileInputStream(System.getProperty("user.dir") + "/app.properties");
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
				e.printStackTrace();
			}
		}
	}

	// This method loads the files. This checks if you are running from a .jar
	// file and loads from the /sounds dir relative
	// to the jar file. If not it assumes you are running from code and loads
	// relative to your resource dir.
	private void getFileList() {

		Map<String, SoundFile> sounds = new TreeMap<>();
		try {

			if (!soundFilePath.toFile().exists()) {
				LOG.info("Creating directory: " + soundFilePath.toFile());
				try {
					soundFilePath.toFile().mkdir();
				} catch (SecurityException se) {
					LOG.fatal("Could not create directory: " + soundFilePath.toFile());
				}
			}

			if (!tmpFilePath.toFile().exists()) {
				LOG.info("Creating directory: " + tmpFilePath.toFile());
				try {
					tmpFilePath.toFile().mkdir();
				} catch (SecurityException se) {
					LOG.fatal("Could not create directory: " + tmpFilePath.toFile());
				}
			}
			
			Files.walk(soundFilePath).forEach(filePath -> {
				if (Files.isRegularFile(filePath)) {
					// Sound file case
					String fileName = filePath.getFileName().toString();
					fileName = fileName.substring(fileName.indexOf("/") + 1, fileName.length());
					fileName = fileName.substring(0, fileName.indexOf("."));
					File file = filePath.toFile();
					String parent = file.getParentFile().getName(); // Category name.
					SoundFile soundFile = new SoundFile(fileName, filePath.toFile(), parent);
					SoundFile _soundFile = soundDao.findOne(fileName);
					if (_soundFile != null) {
						// Resolve conflicts between persistence object and new
						// object.
						soundFile.setNumberOfPlays(_soundFile.getNumberOfPlays());
						soundFile.setNumberOfReports(_soundFile.getNumberOfReports());
						soundFile.setExcludedFromRandom(_soundFile.isExcludedFromRandom());
					}
					saveSound(soundFile);
					sounds.put(fileName, soundFile);
				}
			});

			availableSounds = sounds;
			try {
				LOG.info("Instantiating trie with " + availableSounds.size() + " sound file names.");
				soundNameTrie = new LowercaseTrie(sounds.keySet());
			} catch (Exception e) {
				LOG.fatal("Could not instantiate trie.");
				soundNameTrie = new LowercaseTrie();
			}

		} catch (IOException e) {
			LOG.fatal(e.toString());
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
		}
	}

	public AsyncService getAsyncService() {
		return this.asyncService;
	}

	public List<SoundboardBot> getBots() {
		LinkedList<SoundboardBot> bots = new LinkedList<>();
		for (int i = 0; i < this.bots.length; ++i) {
			if (this.bots[i] != null) {
				bots.add(this.bots[i]);
			}
		}
		return bots;
	}

	public void restartBot(SoundboardBot bot) {
		LOG.info("Restarting bot " + bot.getBotName());
		for (int i = 0; i < bots.length; ++i) {
			if (bots[i] != null && bots[i].equals(bot)) {
				startBot(i + 1);
			}
		}
	}

	public void sendMessageToAll(String message) {
		List<Guild> guilds = new LinkedList<Guild>();
		for (SoundboardBot bot : getBots()) {
			for (Guild g : bot.getGuilds())
				if (!guilds.contains(g))
					guilds.add(g);
		}
		for (Guild guild : guilds) {
			guild.getPublicChannel().sendMessage(message).queue();
		}
	}

	public void sendMessageToAll(String message, Consumer<Integer> callback) {
		sendMessageToAll(message);
		callback.accept(bots.length);
	}

	public List<net.dirtydeeds.discordsoundboard.beans.User> getUserById(String userid) {
		return userDao.findByUserid(userid);
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
		return soundDao.findOne(name);
	}

	public Properties getAppProperties() {
		return appProperties;
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

	private Category getCategory(String name) {
		for (Category category : getCategories())
			if (category.getName().equalsIgnoreCase(name))
				return category;
		return null;
	}

	public boolean isASubCategory(String subcategory, String category) {
		if (category.equalsIgnoreCase(subcategory))
			return true;
		Category _category = getCategory(category);
		for (Category child : _category.getChildren()) {
			if (subcategory.equalsIgnoreCase(child.getName()) || isASubCategory(subcategory, child.getName())) {
				LOG.info("Category " + subcategory + " is subcategory to " + category);
				return true;
			}
		}
		return false;
	}

	public net.dirtydeeds.discordsoundboard.beans.User registerUser(User user, boolean disallowed, boolean throttled) {
		net.dirtydeeds.discordsoundboard.beans.User u = new net.dirtydeeds.discordsoundboard.beans.User(
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

	public void updateFileList() {
		categoryTree = new Category("", soundFilePath);
		numCategories = 0;
		getFileList();
		getCategoryList(soundFilePath, categoryTree);
	}

}
