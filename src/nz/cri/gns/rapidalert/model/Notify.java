package nz.cri.gns.rapidalert.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="notify")
@SequenceGenerator(name = "NOTIFY_SEQ", sequenceName = "NOTIFY_SEQ")
public class Notify {

	private int id;
	private String email;
	private NotifyGroup group;
	
	@Id
	@Column(name="notify_id", unique=true, nullable=false, insertable=true, updatable=true)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTIFY_SEQ")
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name="notify_email")
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	public NotifyGroup getGroup() {
		return group;
	}
	public void setGroup(NotifyGroup group) {
		this.group = group;
	}
	
	
	
}
