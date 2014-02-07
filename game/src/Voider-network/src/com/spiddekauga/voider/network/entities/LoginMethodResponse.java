package com.spiddekauga.voider.network.entities;

import java.util.UUID;


/**
 * Response from the login method
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
@SuppressWarnings("serial")
public class LoginMethodResponse implements IEntity {
	/** Username, the user could log in with email, thus reply with the real username */
	public String username;
	/** If the login was successful */
	public boolean success;
	/** The private key which can be used to login without a password */
	public UUID privateKey;
}
