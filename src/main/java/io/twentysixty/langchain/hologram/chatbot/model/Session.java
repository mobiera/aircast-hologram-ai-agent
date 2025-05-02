package io.twentysixty.langchain.hologram.chatbot.model;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;



/**
 * The persistent class for the session database table.
 * 
 */
@Entity
@Table(name="session")
@DynamicUpdate
@DynamicInsert
@NamedQueries({
	@NamedQuery(name="Session.findWithToken", query="SELECT s FROM Session s where s.token=:token"),
	
})
public class Session implements Serializable {
	private static final long serialVersionUID = 1L;

	
	@Id
	private UUID connectionId;
	
	
	
	@Column(columnDefinition="text")
	private String citizenId;
	@Column(columnDefinition="text")
	private String firstname;
	@Column(columnDefinition="text")
	private String lastname;
	
	@Column(columnDefinition="date")
	private LocalDate birthdate;
	@Column(columnDefinition="text")
	private String placeOfBirth;
	
	@Column(columnDefinition="text")
	private String avatarName;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="memoryId")
	private Memory memory;
	
	
	@Column(columnDefinition="text")
	private String address;
	
	private UUID photo;
	
	private UUID token;

	@Column(columnDefinition="timestamptz")
	private Instant authTs;
	
	
	public UUID getConnectionId() {
		return connectionId;
	}

	public void setConnectionId(UUID connectionId) {
		this.connectionId = connectionId;
	}

	public String getCitizenId() {
		return citizenId;
	}

	public void setCitizenId(String citizenId) {
		this.citizenId = citizenId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public LocalDate getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(LocalDate birthdate) {
		this.birthdate = birthdate;
	}

	public String getPlaceOfBirth() {
		return placeOfBirth;
	}

	public void setPlaceOfBirth(String placeOfBirth) {
		this.placeOfBirth = placeOfBirth;
	}

	public UUID getPhoto() {
		return photo;
	}

	public void setPhoto(UUID photo) {
		this.photo = photo;
	}

	public UUID getToken() {
		return token;
	}

	public void setToken(UUID token) {
		this.token = token;
	}

	public Instant getAuthTs() {
		return authTs;
	}

	public void setAuthTs(Instant authTs) {
		this.authTs = authTs;
	}

	public String getAvatarName() {
		return avatarName;
	}

	public void setAvatarName(String avatarName) {
		this.avatarName = avatarName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Memory getMemory() {
		return memory;
	}

	public void setMemory(Memory memory) {
		this.memory = memory;
	}

	
	


	
}