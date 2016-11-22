package nz.cri.gns.rapidalert.web;

import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;
import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Import({ SecurityConfig.class })
public class AppConfig {

	@Bean
    public DataSource dataSource() throws IllegalArgumentException, NamingException {
        final JndiObjectFactoryBean dsLookup = new JndiObjectFactoryBean();
        dsLookup.setJndiName("java:comp/env/jdbc/rapidalert");
        dsLookup.setResourceRef(true);
        dsLookup.afterPropertiesSet();
        return (DataSource)dsLookup.getObject();
    } 
	
	@Bean
	public SessionFactory sessionFactory() throws Exception {
		AnnotationSessionFactoryBean factory = new AnnotationSessionFactoryBean();
		factory.setDataSource(dataSource());
		factory.setPackagesToScan("nz.cri.gns.rapidalert.model");
		factory.afterPropertiesSet();
		return factory.getObject();
	}
	
	@Bean
	public URL url() throws IllegalArgumentException, NamingException, MalformedURLException {
		final JndiObjectFactoryBean dsLookup = new JndiObjectFactoryBean();
		dsLookup.setJndiName("java:comp/env/rapidalert/rss/rss.url");
        dsLookup.setResourceRef(true);
        dsLookup.afterPropertiesSet();
        return new URL((String)dsLookup.getObject());
	}
	
	@Bean
	public HibernateTransactionManager transactionManager() throws Exception {
		HibernateTransactionManager mgr = new HibernateTransactionManager(sessionFactory());
		return mgr;
	}
	
//	@Bean
//	public MailSender mailSender() {
//		return new AsyncMailSender();
//	}
//	
//	@Bean
//	public ExceptionInterceptor exceptionInterceptor() {
//		return new ExceptionInterceptor();
//	}
//
//	@Bean
//	public BeanNameAutoProxyCreator beanNameAutoProxyCreator() {
//		BeanNameAutoProxyCreator creator = new BeanNameAutoProxyCreator();
//		creator.setBeanNames("CheckRSS");
//		creator.setInterceptorNames("exceptionInterceptor()");
//		return creator;
//	}
	
//	@Bean
//	PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
//		return new PersistenceExceptionTranslationPostProcessor();
//	}
	
	
}
