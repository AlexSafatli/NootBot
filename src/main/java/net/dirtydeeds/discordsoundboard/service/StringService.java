package net.dirtydeeds.discordsoundboard.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.core.utils.SimpleLog;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author asafatli
 * Server thread to read from external text file(s) and update Strings used
 * throughout the application.
 */
@Service
public class StringService {

	public static final SimpleLog LOG = SimpleLog.getLog("Strings");
	private static final float TICK_RATE_PER_HOUR = 1;
	private List<File> files;
	private Map<String,String> strings;
	
	public StringService() {
		files = new LinkedList<>();
		strings = new HashMap<>();
	}
	
	public void addFile(Path filePath) {
		File file = filePath.toFile();
		files.add(file);
	}
	
	public String lookup(String key) {
		return strings.get(key);
	}
	
	private int readFile(File file) throws IOException {
		int read = 0;
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = br.readLine();
		while (line != null) {
			String[] kvpair = line.split("=");
			if (kvpair.length == 2) {
				String key = kvpair[0], value = kvpair[1];
				if (strings.get(key) == null || !strings.get(key).equals(value)) {
					strings.put(key, value);
					++read;
				}
			}
			line = br.readLine();
		}
		br.close();
		return read;
	}
	
    @Async
    public void maintain() {
    	LOG.info("Starting service with tick rate per hour: " + TICK_RATE_PER_HOUR);
    	while (true) {
    		// Check all file(s).
    		for (File file : files) {
    			int numStringsRead = 0;
    			if (file.exists() && file.canRead()) {
    				try {
        				// Read file. Add to String list if different.
    					numStringsRead = readFile(file);
        			} catch (Exception e) {
        				LOG.fatal("Exception when reading " + file.getName() + 
        						": " + e.toString() + " => " + e.getMessage());
        				continue;
        			}
              if (numStringsRead > 0) {
                LOG.info("Finished reading " + file.getName() + " and parsed " + numStringsRead + " strings.");
              }
    			} else {
    				LOG.warn("Could not read file " + file.getAbsolutePath());
    			}
    		}
    		// Only fire a certain amount of times a minute.
    		long millisecondsToWait = (long)(1/TICK_RATE_PER_HOUR) * 3600000;
    		try {
				Thread.sleep(millisecondsToWait);
			} catch (InterruptedException e) {
				LOG.fatal("Thread sleep failed for ms waiting time: " + 
						millisecondsToWait);
			}
    	}
    }

}
