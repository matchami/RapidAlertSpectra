package nz.cri.gns.rapidalert.model;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="notify_group")
@SequenceGenerator(name = "NOTIFY_GROUP_SEQ", sequenceName = "NOTIFY_GROUP_SEQ")
public class NotifyGroup {

	private int id;
	private String description;
	private List<Notify> notify;
	
	@Id
	@Column(name="notify_group_id", unique=true, nullable=false, insertable=true, updatable=true)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "NOTIFY_GROUP_SEQ")
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	@Column(name="notify_group_description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
	public List<Notify> getNotify() {
		return notify;
	}
	public void setNotify(List<Notify> notify) {
		this.notify = notify;
	}
}
