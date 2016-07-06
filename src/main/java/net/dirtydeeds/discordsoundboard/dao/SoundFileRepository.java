package net.dirtydeeds.discordsoundboard.dao;

import java.util.List;

import net.dirtydeeds.discordsoundboard.beans.SoundFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SoundFileRepository extends JpaRepository<SoundFile, String> {
	
	List<SoundFile> findAllByOrderByNumberPlaysDesc();
	
}