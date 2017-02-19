package net.dirtydeeds.discordsoundboard.web;

import javax.inject.Inject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import net.dirtydeeds.discordsoundboard.service.SoundboardDispatcher;

//@Controller
public class ListSoundsController {

	private final SoundboardDispatcher dispatcher;
	
	//@Inject
	public ListSoundsController(SoundboardDispatcher soundboardDispatcher) {
		dispatcher = soundboardDispatcher;
	}
	
	//@RequestMapping("/list")
	public String list(Model model, @RequestParam(value="category", required=false) String category) {
		model.addAttribute("category", category);
		model.addAttribute("sounds", dispatcher.getSoundFilesOrderedByNumberOfPlays());
		return "list";
	}
	
}
