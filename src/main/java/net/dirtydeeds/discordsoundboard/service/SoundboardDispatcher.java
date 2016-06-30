package net.dirtydeeds.discordsoundboard.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import net.dirtydeeds.discordsoundboard.AsyncService;
import net.dirtydeeds.discordsoundboard.MainWatch;
import net.dirtydeeds.discordsoundboard.async.CleanBotMessagesJob;
import net.dirtydeeds.discordsoundboard.async.RelatedRedditSubmissionsJob;
import net.dirtydeeds.discordsoundboard.async.TopRedditSubmissionsJob;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.dao.UserRepository;
import net.dirtydeeds.discordsoundboard.games.leagueoflegends.LeagueOfLegendsChatEndpoint;
import net.dirtydeeds.discordsoundboard.trie.LowercaseTrie;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import net.dv8tion.jda.utils.SimpleLog;

/**
 * @author asafatli.
 *
 * Allows for multiple copies of this service across >= 1 Discord accounts.
 */
@Service
public class SoundboardDispatcher implements Observer {

	public static final SimpleLog LOG = SimpleLog.getLog("Dispatcher");

	private final UserRepository userDao;
	
	private Properties appProperties;
	private SoundboardBot[] bots;
	private LeagueOfLegendsChatEndpoint leagueEndpoint;
	private Map<String, SoundFile> availableSounds;
	private LowercaseTrie soundNameTrie;
	private final MainWatch mainWatch;
	private final AsyncService asyncService;
	private final Path soundFilePath = Paths.get(System.getProperty("user.dir") + "/sounds");
	
    @Inject
    public SoundboardDispatcher(MainWatch mainWatch, UserRepository userDao, AsyncService asyncService) {
        this.mainWatch = mainWatch;
        this.mainWatch.addObserver(this);
        this.asyncService = asyncService;
        this.userDao = userDao;
        availableSounds = new TreeMap<>();
        soundNameTrie = new LowercaseTrie();
        this.mainWatch.watchDirectoryPath(soundFilePath);
        getFileList();
		loadProperties();
		startServices();
		this.asyncService.maintain(this);
    }
	
    /**
     * Gets a Map of the loaded sound files.
     * @return Map of sound files that have been loaded.
     */
    public Map<String, SoundFile> getAvailableSoundFiles() {
        return availableSounds;
    }
    
    public LowercaseTrie getSoundNameTrie() {
    	return soundNameTrie;
    }
    
    private void startBot(int i) {
    	int index = i-1;
    	if (bots[index] != null) {
    		bots[index].getAPI().shutdown(false);
    		bots[index] = null;
    	}
		String token = appProperties.getProperty("token_"  + i);
		String owner = appProperties.getProperty("owner_"  + i);
		String vol   = appProperties.getProperty("volume_" + i);
		if (token == null || owner == null) {
            LOG.fatal("The config was not populated! Please enter both an API token and owner for bot " + i);
            return;
		} else {
			LOG.info("Initializing bot " + i);
		}
		SoundboardBot bot = new SoundboardBot(token, owner, this);
		if (vol != null) bot.setVolume(Float.valueOf(vol));
		bots[index] = bot;
    }
    
	private void startServices() {
		int num = Integer.valueOf(appProperties.getProperty("number_of_users"));
		// Bots
		bots = new SoundboardBot[num];
		for (int i = 1; i <= num; ++i) {
			startBot(i);
		}
		// League of Legends endpoint
		try {
			leagueEndpoint = new LeagueOfLegendsChatEndpoint(this);
		} catch (Exception e) {
			leagueEndpoint = null;
			e.printStackTrace();
		}
		// Async jobs
		asyncService.addJob(new CleanBotMessagesJob());
		asyncService.addJob(new TopRedditSubmissionsJob());
		asyncService.addJob(new RelatedRedditSubmissionsJob());
	}
	
    //Loads in the properties from the app.properties file
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
        } catch (IOException e) { e.printStackTrace(); }
        if (stream == null) {
            LOG.warn("Loading app.properties file from resources folder");
            try {
                stream = this.getClass().getResourceAsStream("/app.properties");
                appProperties.load(stream);
                stream.close();
            } catch (IOException e) { e.printStackTrace(); }
        }
    }
    
    //This method loads the files. This checks if you are running from a .jar file and loads from the /sounds dir relative
    //to the jar file. If not it assumes you are running from code and loads relative to your resource dir.
    private void getFileList() {
    	
    	Map<String, SoundFile> sounds = new TreeMap<>();
        try {

            if (!soundFilePath.toFile().exists()) {
                LOG.debug("Creating directory: " + soundFilePath.toFile().toString());
                try { soundFilePath.toFile().mkdir(); }
                catch(SecurityException se){
                    LOG.fatal("Could not create directory: " + soundFilePath.toFile().toString());
                }
            }
            
            Files.walk(soundFilePath).forEach(filePath -> {
                if (Files.isRegularFile(filePath)) {
                    String fileName = filePath.getFileName().toString();
                    fileName = fileName.substring(fileName.indexOf("/") + 1, fileName.length());
                    fileName = fileName.substring(0, fileName.indexOf("."));
                    File file = filePath.toFile();
                    String parent = file.getParentFile().getName();
                    SoundFile soundFile = new SoundFile(fileName, filePath.toFile(), parent, "");
                    sounds.put(fileName, soundFile);
                }
            });
            
            availableSounds = sounds;
            try {
            	soundNameTrie = new LowercaseTrie(sounds.keySet());
            } catch (Exception e) {
            	LOG.fatal("Could not instantiate trie.");
            	soundNameTrie = new LowercaseTrie();
            }
            
        } catch (IOException e) {
            LOG.fatal(e.toString());
        }
        
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
    	LOG.info("Restarting bot " + bot.getName());
    	for (int i = 0; i < bots.length; ++i) {
    		if (bots[i] != null && bots[i].equals(bot)) {
    			startBot(i);
    		}
    	}
    }
    
    public void sendMessageToAll(String message) {
    	List<Guild> guilds = new LinkedList<Guild>();
    	for (SoundboardBot bot : getBots()) {
    		for (Guild g : bot.getGuilds()) if (!guilds.contains(g)) guilds.add(g);
    	}
    	for (Guild guild : guilds) {
    		guild.getPublicChannel().sendMessageAsync(message, null);
    	}
    }
    
    public void sendMessageToAll(String message, Consumer<Integer> callback) {
    	sendMessageToAll(message);
    	callback.accept(bots.length);
    }
    
    public List<net.dirtydeeds.discordsoundboard.beans.User> getUserById(String userid) {
    	return userDao.findByUserid(userid);
    }
    
    public LeagueOfLegendsChatEndpoint getLeagueOfLegendsEndpoint() {
    	return leagueEndpoint;
    }
    
    public Properties getAppProperties() {
    	return appProperties;
    }
    
    public net.dirtydeeds.discordsoundboard.beans.User registerUser(User user, boolean disallowed, boolean throttled) {
    	net.dirtydeeds.discordsoundboard.beans.User u = new net.dirtydeeds.discordsoundboard.beans.User(user.getUsername(), user.getId());
    	u.setDisallowed(disallowed);
    	u.setThrottled(throttled);
    	saveUser(u);
    	return u;
    }
    
    public void saveUser(net.dirtydeeds.discordsoundboard.beans.User user) {
    	userDao.save(user);
    }
    	
    @Override
    public void update(Observable o, Object arg) {
        getFileList();
    }
    
    public void updateFileList() {
    	getFileList();
    }
	
}
