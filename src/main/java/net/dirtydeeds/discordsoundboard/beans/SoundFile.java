package net.dirtydeeds.discordsoundboard.beans;

import java.io.File;
import java.util.Map;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.sampled.file.TAudioFileFormat;

@Entity
public class SoundFile {
    
	@Id
    private final String soundFileId;
    @Transient
    private File soundFile;
    @Transient
    private Date modified;
    private final String category;
    private final Long duration;
    private Long numberPlays;
    private Integer numberReports;
    private Boolean excludedFromRandom;

	protected SoundFile() { 
		this.soundFileId = null;
		this.category = null;
		this.duration = 0L;
		this.numberPlays = 0L;
		this.numberReports = 0;
		this.excludedFromRandom = false;
	}
    
    public SoundFile(String soundFileId, String category) {
    	this.soundFileId = soundFileId;
    	this.category = category;
    	this.duration = 0L;
    	this.soundFile = null;
    	this.numberPlays = 0L;
		this.numberReports = 0;
    	this.excludedFromRandom = false;
    }
    
    public SoundFile(String soundFileId, File soundFile, String category) {
        this.soundFileId = soundFileId;
        this.soundFile = soundFile;
        if (soundFile != null) {
            this.modified = new Date(soundFile.lastModified());
        }
        this.category = category;
        this.duration = readDuration();
        this.numberPlays = 0L;
		this.numberReports = 0;
        this.excludedFromRandom = false;
    }

    private Long readDuration() {
    	String extension = soundFile.getName().substring(soundFile.getName().indexOf(".") + 1).toLowerCase();
    	try {
    		if (extension.equals("wav")) {
				AudioInputStream audio = AudioSystem.getAudioInputStream(soundFile);
				AudioFormat format = audio.getFormat();
				return (long) ((audio.getFrameLength())/format.getFrameRate());
    		} else if (extension.equals("mp3")) {
    			Map<?, ?> props = ((TAudioFileFormat)AudioSystem.getAudioFileFormat(soundFile)).properties();
    			Long micros = (Long)props.get("duration");
    			return (micros)/1000000;
    		} else return 0L;
		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}
    }
    
    public String getSoundFileId() {
        return soundFileId;
    }

    public String getCategory() {
        return category.replace("\\", "");
    }
    
    public Long getDuration() {
    	return duration;
    }
    
    public File getSoundFile() {
        return soundFile;
    }
    
    public void setSoundFile(File file) {
    	this.soundFile = file;
    }

    public Date getLastModified() {
        return modified;
    }
    
    public Long getNumberOfPlays() {
    	return numberPlays;
    }
    
    public Integer getNumberOfReports() {
    	return (numberReports != null) ? numberReports : 0;
    }

    public void addOneToNumberOfReports() {
    	++numberReports;
    }
	
    public void setNumberOfReports(Integer numberReports) {
		this.numberReports = (numberReports != null) ? numberReports : 0;
	}
    
    public void setNumberOfPlays(Long plays) {
    	numberPlays = (plays != null) ? plays : 0;
    }
    
    public void addOneToNumberOfPlays() {
    	++numberPlays;
    }

    public void subtractOneFromNumberOfPlays() {
        numberPlays = (numberPlays == 0) ? --numberPlays : 0;
    }
        
    public Boolean isExcludedFromRandom() {
    	return this.excludedFromRandom;
    }
    
    public void setExcludedFromRandom(Boolean excluded) {
    	this.excludedFromRandom = excluded;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoundFile soundFile = (SoundFile) o;
        return soundFileId.equals(soundFile.soundFileId);
    }

    @Override
    public int hashCode() {
        return soundFileId.hashCode();
    }

    public String toString() {
    	return "`" + soundFileId + "`";
    }

}
