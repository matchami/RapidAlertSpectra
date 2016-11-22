package nz.cri.gns.rapidalert.tools;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
 
/**
* Asynchronous mail sender
*/
public class AsyncMailSender extends JavaMailSenderImpl {
	@Override
	@Async
	public void send(SimpleMailMessage simpleMessage) throws MailException {
		super.send(simpleMessage);
	}
	 
	@Override
	@Async
	public void send(SimpleMailMessage... simpleMessages) throws MailException {
		super.send(simpleMessages);
	}
} 