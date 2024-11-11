package io.twentysixty.ollama.hologram.chatbot.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name="histo", indexes = {
        @Index(name = "histo_idx_ts", columnList = "ts, session_id")})
@DynamicUpdate
@DynamicInsert
@NamedQueries({
	@NamedQuery(name="History.find", query="SELECT h FROM History h where h.session=:session order by h.ts desc"),
	@NamedQuery(name="History.delete", query="DELETE FROM History h where h.session=:session"),
	
})
public class History implements Serializable {
	private static final long serialVersionUID = 1L;

	
	@Id
	private UUID historyId;
	
	@Column(columnDefinition="timestamptz")
	private Instant ts;
	
	private LlamaRole role;
	
	
	@ManyToOne
	@JoinColumn(name="session_id")
	private Session session;
	
	@Column(columnDefinition="text")
	private String data;

	public UUID getHistoryId() {
		return historyId;
	}

	public void setHistoryId(UUID historyId) {
		this.historyId = historyId;
	}

	public Instant getTs() {
		return ts;
	}

	public void setTs(Instant ts) {
		this.ts = ts;
	}

	public LlamaRole getRole() {
		return role;
	}

	public void setRole(LlamaRole role) {
		this.role = role;
	}

	

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	


	
}