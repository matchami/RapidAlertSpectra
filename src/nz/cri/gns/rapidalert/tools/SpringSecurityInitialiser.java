package nz.cri.gns.rapidalert.tools;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

public class SpringSecurityInitialiser extends AbstractSecurityWebApplicationInitializer {
	//do nothing - this should automatically load the filter chain.  if not...
	/*
	<filter>
	  	<filter-name>springSecurityFilterChain</filter-name>
		<filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
	</filter>
 
	<filter-mapping>
		<filter-name>springSecurityFilterChain</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
	 */
}