package net.dirtydeeds.discordsoundboard.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;

/**
 * @author dfurrer.
 */
@SuppressWarnings("unused")
public class SoundFile {
    
    final String soundFileId;
    final File soundFile;
    final String category;
    final String description; 

    public SoundFile(String soundFileId, File soundFile, String category, String description) {
        this.soundFileId = soundFileId;
        this.soundFile = soundFile;
        this.category = category;
        this.description = description;
    }

    public String getSoundFileId() {
        return soundFileId;
    }

    public String getSoundFileLocation() {
        return soundFile.toString();
    }

    public String getCategory() {
        return category.replace("\\", "");
    }
    
    public String getDescription() {
    	return description;
    }

    @JsonIgnore
    public File getSoundFile() {
        return soundFile;
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
