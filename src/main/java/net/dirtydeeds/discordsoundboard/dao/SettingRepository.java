package net.dirtydeeds.discordsoundboard.dao;

import net.dirtydeeds.discordsoundboard.beans.Setting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {

  List<Setting> findAllByKey(String key);

}