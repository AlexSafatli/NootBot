package net.dirtydeeds.discordsoundboard.web;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;
import net.dirtydeeds.discordsoundboard.beans.User;
import net.dirtydeeds.discordsoundboard.service.SoundboardBot;
import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author dfurrer.
 */
@RestController
@RequestMapping("/api")
@SuppressWarnings("unused")
public class SoundboardRestController {
    
    SoundboardDispatcher dispatcher;

    @SuppressWarnings("unused") //Damn spring and it's need for empty constructors
    public SoundboardRestController() {
    }

    @Inject
    public SoundboardRestController(final SoundboardDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @RequestMapping("/getAvailableSounds")
    public List<SoundFile> getSoundFileList() {
        Map<String, SoundFile> soundMap = dispatcher.getAvailableSoundFiles();
        return soundMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toCollection(LinkedList::new));
    }
    
    @RequestMapping("/getSoundCategories")
    public Set<String> getSoundCategories() {
        Set<String> categories = new HashSet<>();
        Map<String, SoundFile> soundMap = dispatcher.getAvailableSoundFiles();
        for (Map.Entry<String, SoundFile> entry : soundMap.entrySet()) {
            categories.add(entry.getValue().getCategory());
        }
        return categories;
    }
    
}
