package nz.cri.gns.rapidalert.da;

import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import com.jumbletree.utils.dataaccess.DAOFactory;
import com.jumbletree.utils.dataaccess.DataAccessException;
import com.jumbletree.utils.dataaccess.hibernate.ConnectionProvider;
import com.jumbletree.utils.dataaccess.hibernate.HibernateConnection;

/**
 * 
 * Replaced by Spring managed connection
 */
@Deprecated
public class RapidAlertHibernate extends HibernateConnection<DataAccess> {
	
	static Properties getProps() {
		Properties props = new Properties();
		props.put("hibernate.connection.datasource", "java:comp/env/jdbc/rapidalert");
		return props;
	}
	
	public RapidAlertHibernate(String dbHost, String dbName, String user, String password) throws ClassNotFoundException, SQLException, DataAccessException {
		super(dbHost, dbName, user, password, RapidAlertHibernate.class.getResource("hibernate.cfg.xml"));
	}

	public RapidAlertHibernate() throws DataAccessException {
		super(getProps(), RapidAlertHibernate.class.getResource("hibernate.cfg.xml"));
	}

	@Override
	protected DAOFactory<DataAccess> createFactory(Session session,
			ConnectionProvider connectionProvider) throws HibernateException {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	protected RapidAlertDAOFactory createFactory(Session session, ConnectionProvider connectionProvider) throws HibernateException {
//		return new RapidAlertDAOFactory(session, connectionProvider);
//	}
}
