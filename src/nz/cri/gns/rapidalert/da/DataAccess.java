package nz.cri.gns.rapidalert.da;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import nz.cri.gns.rapidalert.model.DamageThreshold;
import nz.cri.gns.rapidalert.model.Earthquake;
import nz.cri.gns.rapidalert.model.Notify;
import nz.cri.gns.rapidalert.model.NotifyGroup;
import nz.cri.gns.rapidalert.model.Region;
import nz.cri.gns.rapidalert.model.Station;
import nz.cri.gns.rapidalert.model.StationList;

import com.jumbletree.utils.dataaccess.DataAccessException;

public interface DataAccess extends com.jumbletree.utils.dataaccess.DataAccess {
	
	public class Types<T> {
		private Class<T> clazz;
		Types(Class<T> clazz) {
			this.clazz = clazz;
		}

		public Class<T> get() {
			return clazz;
		}
	}
	
	public static final Types<DamageThreshold> DAMAGE_THRESHOLD = new Types<DamageThreshold>(DamageThreshold.class);
	public static final Types<Earthquake> EARTHQUAKE = new Types<Earthquake>(Earthquake.class);
	public static final Types<Region> REGION = new Types<Region>(Region.class);
	public static final Types<Station> STATION = new Types<Station>(Station.class);
	public static final Types<StationList> STATION_LIST = new Types<StationList>(StationList.class);
	public static final Types<Notify> NOTIFY = new Types<Notify>(Notify.class);
	public static final Types<NotifyGroup> NOTIFY_GROUP = new Types<NotifyGroup>(NotifyGroup.class);
	


	<T> T get(Types<T> type, Serializable id) throws DataAccessException;
	
	<T> List<T> listAll(Types<T> type) throws DataAccessException;

	List<Earthquake> listEarthquakes(LocalDate start, LocalDate end) throws DataAccessException;
	
	/**
	 * Returns true if an earthquake with the given slug exists in the database
	 * @param slug
	 * @return
	 * @throws DataAccessException 
	 */
	boolean earthquakeExists(String slug) throws DataAccessException;

	Earthquake getEarthquake(String slug) throws DataAccessException;
	
	/**
	 * Returns a list of earthquakes in the last <code>numberOfDays</code> days
	 * @param numberOfDays
	 * @return
	 * @throws DataAccessException 
	 */
	List<Earthquake> getRecentEarthquakes(int numberOfDays) throws DataAccessException;

	List<Notify> getDevEmailAddresses() throws DataAccessException;
	List<Notify> getRealEmailAddresses() throws DataAccessException;
	List<Notify> getErrorEmailAddresses() throws DataAccessException;



	
	
}

