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

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import net.dirtydeeds.discordsoundboard.MainWatch;
import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.games.leagueoflegends.LeagueOfLegendsChatEndpoint;
import net.dv8tion.jda.utils.SimpleLog;

/**
 * @author asafatli.
 *
 * Allows for multiple copies of this service across >= 1 Discord accounts.
 */
@Service
public class SoundboardDispatcher implements Observer {

	public static final SimpleLog LOG = SimpleLog.getLog("Dispatcher");
	private Properties appProperties;
	private List<SoundboardBot> bots;
	private LeagueOfLegendsChatEndpoint leagueEndpoint;
	private Map<String, SoundFile> availableSounds;
	private final MainWatch mainWatch;
	
    @Inject
    public SoundboardDispatcher(MainWatch mainWatch) {
        this.mainWatch = mainWatch;
        this.mainWatch.addObserver(this);
        bots = new LinkedList<SoundboardBot>();
        availableSounds = new TreeMap<>();
        getFileList();
		loadProperties();
		startServices();
    }
	
    /**
     * Gets a Map of the loaded sound files.
     * @return Map of sound files that have been loaded.
     */
    public Map<String, SoundFile> getAvailableSoundFiles() {
        return availableSounds;
    }
    
	private void startServices() {
		int num = Integer.valueOf(appProperties.getProperty("number_of_users"));
		for (int i = 1; i <= num; ++i) {
			try {
				String token = appProperties.getProperty("token_" + i);
				String owner = appProperties.getProperty("owner_" + i);
				SoundboardBot bot = new SoundboardBot(token, owner, this);
				bots.add(bot);
			} catch (IllegalArgumentException e) {
	            LOG.warn("The config was not populated. Please enter an API token for " + i);
	        }
		}
		try {
			leagueEndpoint = new LeagueOfLegendsChatEndpoint(this);
		} catch (Exception e) {
			leagueEndpoint = null;
			e.printStackTrace();
		}
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
    
    //This method loads the files. This checks if you are running from a .jar file and loads from the /sounds dir relative
    //to the jar file. If not it assumes you are running from code and loads relative to your resource dir.
    private void getFileList() {
    	
    	Map<String, SoundFile> sounds = new TreeMap<>();
        try {
        	
            LOG.info("Loading from " + System.getProperty("user.dir") + "/sounds");
            Path soundFilePath = Paths.get(System.getProperty("user.dir") + "/sounds");

            if (!soundFilePath.toFile().exists()) {
                LOG.debug("Creating directory: " + soundFilePath.toFile().toString());
                try { soundFilePath.toFile().mkdir(); }
                catch(SecurityException se){
                    LOG.fatal("Could not create directory: " + soundFilePath.toFile().toString());
                }
            }

            mainWatch.watchDirectoryPath(soundFilePath);
            if (!availableSounds.isEmpty()) {
            	LOG.info("Regenerating file list because of notification from file watch.");
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
            
        } catch (IOException e) {
            LOG.fatal(e.toString());
            e.printStackTrace();
        }
        
    }

    public List<SoundboardBot> getBots() {
    	return bots;
    }
    
    public LeagueOfLegendsChatEndpoint getLeagueOfLegendsEndpoint() {
    	return leagueEndpoint;
    }
    
    public Properties getAppProperties() {
    	return appProperties;
    }
    
    @Override
    public void update(Observable o, Object arg) {
        getFileList();
    }
	
}
