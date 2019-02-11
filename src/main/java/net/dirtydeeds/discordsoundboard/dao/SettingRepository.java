package net.dirtydeeds.discordsoundboard.dao;

import net.dirtydeeds.discordsoundboard.beans.Setting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingRepository extends JpaRepository<Setting, String> {

}