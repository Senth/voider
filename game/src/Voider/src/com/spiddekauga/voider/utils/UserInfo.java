package com.spiddekauga.voider.utils;

import java.util.UUID;

/**
 * Wrapper for user information
 * 
 * @author Matteus Magnusson <senth.wallace@gmail.com>
 */
public class UserInfo {
	/** Username */
	public String username;
	/** Private key */
	public UUID privateKey = null;
	/** Password */
	public String password = null;
	/** Email */
	public String email = null;
	/** Online/Offline mode */
	public boolean online = true;
}
