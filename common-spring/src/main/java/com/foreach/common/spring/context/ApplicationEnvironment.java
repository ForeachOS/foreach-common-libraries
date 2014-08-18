package com.foreach.common.spring.context;

/**
 * A enum to differentiate between running environments.
 * Used for configuration or the conditional execution of code,
 * see also {@link ApplicationContextInfo}.
 */
public enum ApplicationEnvironment
{
	DEVELOPMENT,
	TEST,
	STAGING,
	ACCEPTANCE,
	PRODUCTION
}
