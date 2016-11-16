package com.spiddekauga.voider.network.user;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.spiddekauga.voider.network.entities.IMethodEntity;
import com.spiddekauga.voider.version.Version;

import java.util.Date;
import java.util.UUID;


/**
 * Log in method. Can use either private key or password.
 */
public class LoginMethod implements IMethodEntity {
/** Client id */
@Tag(1)
public UUID clientId;
/** Username */
@Tag(2)
public String username;
/** Password */
@Tag(3)
public String password;
/** Private key, alternative login method */
@Tag(4)
public UUID privateKey;
/** Previous login date on this client */
@Tag(22)
public Date lastLogin;
/** Current gameVersion of the client */
@Tag(24)
public Version currentVersion;
@Deprecated
@Tag(5)
private int _unused1;

@Override
public MethodNames getMethodName() {
	return MethodNames.LOGIN;
}
}
