package net.dirtydeeds.discordsoundboard.beans;

import java.io.File;

public class SoundFile {
    
    private final String soundFileId;
    private final File soundFile;
    private final String category;
    private final String description; 

    public SoundFile(String soundFileId, File soundFile, String category, String description) {
        this.soundFileId = soundFileId;
        this.soundFile = soundFile;
        this.category = category;
        this.description = description;
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
