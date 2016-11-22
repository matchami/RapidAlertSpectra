package nz.cri.gns.rapidalert.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
 
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication().withUser("colin").password("123456").roles("USER");
		auth.inMemoryAuthentication().withUser("iain").password("Al3rtR4p1dd3").roles("ADMIN");
		auth.inMemoryAuthentication().withUser("mostafa").password("Al3rtR4p1dd3").roles("ADMIN");
		auth.inMemoryAuthentication().withUser("bob").password("123456").roles();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.authorizeRequests()
		.antMatchers("/manageNotifications.jsp").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
		.antMatchers("/dba/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_DBA')")
		.and().formLogin();

	}
}