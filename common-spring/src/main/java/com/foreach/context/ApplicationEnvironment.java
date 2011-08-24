package com.foreach.context;

/**
 * A simple enum providing 5 values to differ between running environments.
 * Can be used for configuration loading, see also {@link ApplicationContextInfo}.
 */
public enum ApplicationEnvironment
{
	DEVELOPMENT,
	TEST,
	STAGING,
	ACCEPTANCE,
	PRODUCTION
}
