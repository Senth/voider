package com.spiddekauga.voider.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings({ "serial", "javadoc" })
public class WeeklyReport extends HttpServlet {
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");

		MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()));
		try {
			message.setFrom(new InternetAddress("spiddekauga@voider-game.com", "Voider"));
			message.setReplyTo(new Address[] { new InternetAddress("spiddekauga@voider-game.com", "Voider") });
			message.addRecipient(Message.RecipientType.TO, new InternetAddress("senth.wallace@gmail.com", "Matteus"));
			message.setSubject("[Voider] Weekly Report");
			message.setContent("<h1>Weekly Report</h1></br>Test.", "text/html");
			Transport.send(message);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}
