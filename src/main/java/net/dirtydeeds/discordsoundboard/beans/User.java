package net.dirtydeeds.discordsoundboard.beans;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
    private String userid;
    private String username;
    private String entrancefilename;
    private Boolean disallowed;
	private Boolean throttled;
	private Boolean optedoutofmentions;
	private String battletag;
	
	protected User() { }
	
    public User(String userid, String username) {
        this.userid = userid;
        this.username = username;
        this.entrancefilename = null;
        this.disallowed = false;
        this.throttled = false;
        this.battletag = null;
        this.optedoutofmentions = false;
    }
    
    public User(String userid, String username, String entrancefilename) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.disallowed = false;
    	this.throttled = false;
        this.optedoutofmentions = false;
    }
    
    public User(String userid, String username, String entrancefilename, boolean disallowed, boolean throttled) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.disallowed = disallowed;
    	this.throttled = throttled;
        this.optedoutofmentions = false;
    }
    
    public User(String userid, String username, String entrancefilename, boolean disallowed, boolean throttled, boolean optedoutofmentions) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.disallowed = disallowed;
    	this.throttled = throttled;
        this.optedoutofmentions = optedoutofmentions;
    }
    
    public String getId() {
        return userid;
    }
    
	public void setId(String id) {
		this.userid = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getEntrance() {
		return entrancefilename;
	}
	
	public void setEntrance(String soundFile) {
		this.entrancefilename = soundFile;
	}

	public String getBattleTag() {
		return battletag;
	}
	
	public void setBattleTag(String battletag) {
		this.battletag = battletag;
	}
	
	public boolean isDisallowed() {
		return disallowed;
	}

	public void setDisallowed(boolean disallowed) {
		this.disallowed = disallowed;
	}

	public boolean isThrottled() {
		if (this.throttled == null) {
			this.throttled = false;
		}
		return throttled;
	}

	public void setThrottled(boolean throttled) {
		this.throttled = throttled;
	}
	
	public boolean hasOptedOutOfMentions() {
		if (this.optedoutofmentions == null) {
			this.optedoutofmentions = false;
		}
		return this.optedoutofmentions;
	}
	
	public void setOptedOutOfMentions(boolean optedout) {
		this.optedoutofmentions = optedout;
	}

	public String toString() {
		return String.format("User[id=%d, name=%s, disallowed=%b, throttled=%b]", 
				userid, username, disallowed, throttled);
	}
	

}
