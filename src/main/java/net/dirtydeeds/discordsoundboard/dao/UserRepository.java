package net.dirtydeeds.discordsoundboard.dao;

import java.util.List;

import net.dirtydeeds.discordsoundboard.beans.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	List<User> findByUserid(String userid);
  List<User> findAllByEntrancefilename(String entrancefilename);
	
}
