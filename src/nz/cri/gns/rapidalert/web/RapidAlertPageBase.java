package nz.cri.gns.rapidalert.web;

import javax.servlet.http.HttpServletRequest;

import nz.cri.gns.rapidalert.da.DataAccess;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.jumbletree.utils.web.PageBase;
import com.jumbletree.utils.web.User;


public abstract class RapidAlertPageBase extends PageBase<User, DataAccess> {

	private static final long serialVersionUID = 1L;

	private static final String MMICALC_ATTRIBUTE = "mmicalc.connection";
	
	public DataAccess dataAccess;
	
	private DataAccess loadDAFromContext() throws NestedRuntimeException {
		WebApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
		return (DataAccess) ac.getBean("dataAccess");
	}
	
	@Override
	public DataAccess getDataAccess(HttpServletRequest request) {
		if (dataAccess == null) {
			dataAccess = loadDAFromContext();
		}
		return dataAccess;
	}
	
	@Override
	protected User checkLogin(HttpServletRequest request) {
//		try {
//			String login = request.getParameter("l_name");
//			if (login == null)
//				return null;
//			User user = getDataAccess(request).getUser(login, request.getParameter("l_pass"));
//			registerUser(request, user);
//			return user;
//		} catch (DataAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return new User() {
		};
	}

	@Override
	protected User getCurrentUser(HttpServletRequest request) {
		//!!*@!#&($ Hibernate !
		User user = super.getCurrentUser(request);
		if (user == null)
			return user;
//		try {
			getDataAccess(request).refresh(user);
			return user;
//		} catch (DataAccessException e) {
//			e.printStackTrace();
//			return user;
//		}
	}
}
