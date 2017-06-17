package net.dirtydeeds.discordsoundboard.beans;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="AlternateHandles", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="handle")
    private List<String> alternateHandles;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="SoundsReported", joinColumns=@JoinColumn(name="user_id"))
    @Column(name="sound")
    private List<String> soundsReported;
    private Boolean disallowed;
	private Boolean throttled;
	private Boolean askedAboutAlternateHandles;
	
	/* Privilege Levels
	 * 0 - No privileges
	 * 1 - Can upload sounds and perform all typical authenticated command even if not a moderator of a server
	 * 2 - Can upload sounds without restriction of length, duration, or size
	 */
	private Integer privilegeLevel;
	
	protected User() { }
	
    public User(String userid, String username) {
        this.userid = userid;
        this.username = username;
        this.entrancefilename = null;
        this.alternateHandles = new LinkedList<>();
        this.soundsReported = new LinkedList<>();
        this.disallowed = false;
        this.throttled = false;
        this.askedAboutAlternateHandles = false;
        this.privilegeLevel = 0;
    }
    
    public User(String userid, String username, String entrancefilename) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.alternateHandles = new LinkedList<>();
        this.soundsReported = new LinkedList<>();
    	this.disallowed = false;
    	this.throttled = false;
        this.askedAboutAlternateHandles = false;
    	this.privilegeLevel = 0;
    }
    
    public User(String userid, String username, String entrancefilename, boolean disallowed, boolean throttled) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.alternateHandles = new LinkedList<>();
        this.soundsReported = new LinkedList<>();
    	this.disallowed = disallowed;
    	this.throttled = throttled;
        this.askedAboutAlternateHandles = false;
    	this.privilegeLevel = 0;
    }
    
    public User(String userid, String username, String entrancefilename, boolean disallowed, boolean throttled, int privilegeLevel) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.alternateHandles = new LinkedList<>();
        this.soundsReported = new LinkedList<>();
    	this.disallowed = disallowed;
    	this.throttled = throttled;
        this.askedAboutAlternateHandles = false;
    	this.privilegeLevel = privilegeLevel;
    }
    
    public User(String userid, String username, String entrancefilename, List<String> alternateHandles, boolean disallowed, boolean throttled, int privilegeLevel) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.alternateHandles = alternateHandles;
        this.soundsReported = new LinkedList<>();
    	this.disallowed = disallowed;
    	this.throttled = throttled;
        this.askedAboutAlternateHandles = false;
    	this.privilegeLevel = privilegeLevel;
    }
    
    public User(String userid, String username, String entrancefilename, List<String> alternateHandles, boolean disallowed, boolean throttled, boolean askedAboutAlternateHandles, int privilegeLevel) {
    	this.userid = userid;
    	this.username = username;
    	this.entrancefilename = entrancefilename;
    	this.alternateHandles = alternateHandles;
        this.soundsReported = new LinkedList<>();
    	this.disallowed = disallowed;
    	this.throttled = throttled;
        this.askedAboutAlternateHandles = askedAboutAlternateHandles;
    	this.privilegeLevel = privilegeLevel;
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
	
	public List<String> getAlternateHandles() {
		return alternateHandles;
	}
	
	public void addAlternateHandle(String handle) {
		if (!alternateHandles.contains(handle)) alternateHandles.add(handle);
	}
	
	public List<String> getSoundsReported() {
		return soundsReported;
	}
	
	public void addSoundReported(String sound) {
		if (!soundsReported.contains(sound)) soundsReported.add(sound);
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
	
	public boolean wasAskedAboutAlternateHandles() {
		return (this.askedAboutAlternateHandles != null) ? this.askedAboutAlternateHandles : false;
	}
	
	public void setAskedAboutAlternateHandles(boolean asked) { 
		this.askedAboutAlternateHandles = asked;
	}
	
	public void setPrivilegeLevel(Integer level) {
		if (level >= 0) this.privilegeLevel = level;
		else this.privilegeLevel = 0;
	}
	
	public Integer getPrivilegeLevel() {
		return (this.privilegeLevel != null) ? this.privilegeLevel : 0;
	}
	
	public boolean isPrivileged() {
		return this.privilegeLevel != null && this.privilegeLevel > 0;
	}

	public String toString() {
		return String.format("User[id=%s, name=%s, disallowed=%b, throttled=%b, level=%d]", 
				userid, username, disallowed, throttled, privilegeLevel);
	}
	

}
