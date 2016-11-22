package nz.cri.gns.rapidalert.da;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import nz.cri.gns.rapidalert.model.Earthquake;
import nz.cri.gns.rapidalert.model.Notify;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.jumbletree.utils.dataaccess.DataAccessException;
import com.jumbletree.utils.dataaccess.DatabaseTransaction;
import com.jumbletree.utils.dataaccess.hibernate.HibernateUtils;

@Repository("dataAccess")
@Transactional
public class RapidAlertDAOFactory implements DataAccess {
	

	public RapidAlertDAOFactory() {};

	@Autowired
	SessionFactory sessionFactory;

	public DataAccess getDataAccess() throws DataAccessException {
		return this;
	}

	@Override
	public <T> T get(Types<T> type, Serializable id) throws DataAccessException {
		return HibernateUtils.get(sessionFactory.getCurrentSession(), type.get(), id);
	}
	
	@Override
	public <T> List<T> listAll(Types<T> type) throws DataAccessException {
		System.out.println("Trying to list all from "+type.get().getCanonicalName());
		try {
			Query query = sessionFactory.getCurrentSession().createQuery("FROM " + type.get().getCanonicalName());
			return query.list();
		} catch (HibernateException e) {
			throw new DataAccessException(e);
		}
	}

	@Override
	public boolean earthquakeExists(String slug) throws DataAccessException {
		Query query = sessionFactory.getCurrentSession().createQuery("FROM Earthquake WHERE slug = :slug");
		query.setString("slug", slug);
		List<Earthquake> answer = HibernateUtils.list(query, Earthquake.class);
		return answer.size() > 0;
	}

	@Override
	public List<Earthquake> getRecentEarthquakes(int numberOfDays) throws DataAccessException {
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery("SELECT * FROM earthquake WHERE eq_time > current_date - integer '" + numberOfDays + "'");
		query.addEntity(Earthquake.class);
		
		return HibernateUtils.list(query, Earthquake.class);
	}

	@Override
	public Earthquake getEarthquake(String slug) throws DataAccessException {
		Query query = sessionFactory.getCurrentSession().createQuery("FROM Earthquake WHERE slug = :slug");
		query.setString("slug", slug);
		List<Earthquake> answer = HibernateUtils.list(query, Earthquake.class);
		return answer.size() > 0 ? answer.get(0) : null;
	}
	
	@Override
	public List<Notify> getDevEmailAddresses() throws DataAccessException {
		return sessionFactory.getCurrentSession().createQuery("FROM Notify n WHERE n.group.id = 1").list();
	}
	
	@Override
	public List<Notify> getRealEmailAddresses() throws DataAccessException {
		return sessionFactory.getCurrentSession().createQuery("FROM Notify n WHERE n.group.id = 2").list();
	}
	
	@Override
	public List<Notify> getErrorEmailAddresses() throws DataAccessException {
		return sessionFactory.getCurrentSession().createQuery("FROM Notify n WHERE n.group.id = 3").list();
	}
	
	

	
	@Override
	public void update(Object o) {
		sessionFactory.getCurrentSession().saveOrUpdate(o);
		sessionFactory.getCurrentSession().flush();
	}
	
	@Override
	public void save(Object o) throws DataAccessException {
		update(o);
	}

	@Override
	public void closeConnection() {
	}

	@Override
	public void refresh(Object object) {
	}

	@Override
	public DatabaseTransaction beginTransaction() {
		throw new IllegalArgumentException("Transactions handled by Spring, annotate the relevant method");
	}


	@Override
	public void delete(Object o) throws DataAccessException {
		HibernateUtils.delete(sessionFactory.getCurrentSession(), o);
		
	}

	@Override
	public <T> T ensureAlive(T o) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean executeSQL(String query, List<Object> params,
			List<Object> results, Class<?>... resultClass)
			throws DataAccessException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> T get(Class<T> clazz, Serializable id) throws DataAccessException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Earthquake> listEarthquakes(LocalDate start, LocalDate end) throws DataAccessException {
		LocalDate startDate = start.minusDays(1);
		LocalDate endDate = end.plusDays(1);
		SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery("SELECT * FROM earthquake WHERE eq_time > :start AND eq_time < :end");
		query.addEntity(Earthquake.class);
		query.setDate("start", Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		query.setDate("end", Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
		
		return HibernateUtils.list(query, Earthquake.class);
	}
	
}
