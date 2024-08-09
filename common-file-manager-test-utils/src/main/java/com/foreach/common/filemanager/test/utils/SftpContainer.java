/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.foreach.common.filemanager.test.utils;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

public class SftpContainer
{
	public static final int TEST_PORT = 10022;

	private final Path tempDir;
	private final SshServer server;

	@SneakyThrows
	public SftpContainer() {
		tempDir = Files.createTempDirectory( "fmm-ftp" );
		server = SshServer.setUpDefaultServer();
		server.setPort( TEST_PORT );
		server.setKeyPairProvider( new SimpleGeneratorHostKeyProvider() );
		server.setUserAuthFactories( Arrays.asList( new UserAuthPasswordFactory() ) );
		server.setPasswordAuthenticator( new SftpAuthenticator() );
		SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder()
				.build();
		VirtualFileSystemFactory fileSystemFactory = new VirtualFileSystemFactory();
		fileSystemFactory.setDefaultHomeDir( tempDir );
		server.setFileSystemFactory( fileSystemFactory );
		server.setSubsystemFactories( Collections.singletonList( factory ) );
	}

	@SneakyThrows
	public void start() {
		server.start();
	}

	@SneakyThrows
	public void stop() {
		server.stop();
		FileUtils.forceDelete( tempDir.toFile() );
	}

	public class SftpAuthenticator implements PasswordAuthenticator
	{
		SftpAuthenticator() {
		}

		@Override
		public boolean authenticate( String username, String password, ServerSession session ) throws PasswordChangeRequiredException, AsyncAuthException {
			boolean matchesUsername = "fmm".equals( username );
			boolean matchesPassword = "test".equals( password );
			return matchesUsername && matchesPassword;
		}
	}
}
