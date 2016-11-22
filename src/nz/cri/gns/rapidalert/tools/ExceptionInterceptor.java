package nz.cri.gns.rapidalert.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
 
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
 
/**
* Exception interceptor (AOP)
*/
public class ExceptionInterceptor implements MethodInterceptor {

	private static final Logger logger = LoggerFactory.getLogger(ExceptionInterceptor.class);
	
	@Autowired
	MailSender mailSender;

	public Object invoke(MethodInvocation method) throws Throwable {
		Object result = null;
		try {
			result = method.proceed();
		} catch (Exception e) {
			System.out.println("Exception caught in interceptor");
			// Building stack trace string
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			// Building e-mail
			SimpleMailMessage email = new SimpleMailMessage();
			email.setTo("iain@jumbletree.com");
			email.setSubject("[RapidAlert] Exception in '" + method.getMethod().getName() + "' method");
			email.setText(
					"Exception in: " + method.getMethod().getName() + "\n\n" +
							"Class: " + method.getMethod().getDeclaringClass().getName() + "\n\n" +
							"Message: " + e.getMessage() + "\n\n" +
							"StackTrace:\n" + stackTrace.getBuffer().toString()
					);
			// Sending e-mail
			try {
				this.mailSender.send(email);
			} catch (MailException mailException) {
				logger.error(mailException.getMessage());
			}
			throw e;
		}
		return result;
	}
} 