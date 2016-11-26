package net.dirtydeeds.discordsoundboard.beans;

import java.io.File;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

@Entity
public class SoundFile {
    
	@Id
    private final String soundFileId;
    @Transient
    private File soundFile;
    private final String category;
    private String description;
    private Long numberPlays;
    @OneToOne(cascade = {CascadeType.ALL})
    private User user;

	protected SoundFile() { 
		this.soundFileId = null;
		this.category = null;
		this.numberPlays = 0L;
	}
    
    public SoundFile(String soundFileId, String category, String description) {
    	this.soundFileId = soundFileId;
    	this.category = category;
    	this.description = description;
    	this.soundFile = null;
    	this.numberPlays = 0L;
    }
    
    public SoundFile(String soundFileId, File soundFile, String category, String description) {
        this.soundFileId = soundFileId;
        this.soundFile = soundFile;
        this.category = category;
        this.description = description;
        this.numberPlays = 0L;
    }

    public String getSoundFileId() {
        return soundFileId;
    }

    public String getCategory() {
        return category.replace("\\", "");
    }
    
    public String getDescription() {
    	return description;
    }
    
    public File getSoundFile() {
        return soundFile;
    }
    
    public void setSoundFile(File file) {
    	this.soundFile = file;
    }
    
    public void setDescription(String desc) {
    	this.description = desc;
    }
    
    public Long getNumberOfPlays() {
    	return numberPlays;
    }
    
    public void setNumberOfPlays(Long plays) {
    	numberPlays = plays;
    }
    
    public void addOneToNumberOfPlays() {
    	++numberPlays;
    }
    
    public User getUser() {
    	return this.user;
    }
    
    public void setUser(User user) {
    	this.user = user;
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
    
}
