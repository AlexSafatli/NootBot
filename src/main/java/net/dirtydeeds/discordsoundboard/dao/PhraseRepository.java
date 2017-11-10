package net.dirtydeeds.discordsoundboard.dao;

import java.util.List;

import net.dirtydeeds.discordsoundboard.beans.Phrase;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhraseRepository extends JpaRepository<Phrase, Long> {

  List<Phrase> findAll();
  List<Phrase> findByValue(String value);
  List<Phrase> deleteByValue(String value);

}