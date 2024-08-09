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
package com.foreach.common.filemanager.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.common.SftpException;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;

import static org.apache.sshd.sftp.common.SftpConstants.SSH_FILEXFER_TYPE_UNKNOWN;
import static org.apache.sshd.sftp.common.SftpConstants.SSH_FX_NO_SUCH_FILE;

@Slf4j
public class SFTPFile
{
	private final String path;
	private final String fileName;

	private final SftpRemoteFileTemplate remoteFileTemplate;

	SFTPFile( SftpRemoteFileTemplate remoteFileTemplate, String path ) {
		this.remoteFileTemplate = remoteFileTemplate;
		this.path = path;
		this.fileName = StringUtils.getFilename( path );
	}

	public boolean exists() {

		return remoteFileTemplate.<Boolean, SftpClient>executeWithClient( client -> {
			try {
				client.stat( path );
			}
			catch ( SftpException e ) {
				if ( e.getStatus() == SSH_FX_NO_SUCH_FILE ) {
					return false;
				}
				LOG.error( "Unexpected error when checking whether file at {} exists", path, e );
				return false;
			}
			catch ( IOException e ) {
				throw new RuntimeException( e );
			}
			return true;
		} );

	}

	public boolean isDirectory() {
		return remoteFileTemplate.<Boolean, SftpClient>executeWithClient( client -> {
			try {
				return client.stat( path ).isDirectory();
			}
			catch ( Exception ignore ) {
			}
			return false;
		} );

	}

	public String getName() {
		return fileName;
	}

	public long getSize() {
		return remoteFileTemplate.<Long, SftpClient>executeWithClient( client -> {
			try {
				return client.stat( path ).getSize();
			}
			catch ( Exception e ) {
				LOG.error( "Unexpected error when checking file size for {} ", path, e );
			}
			return -1L;
		} );

	}

	// epochmilli
	public long getLastModified() {
		return remoteFileTemplate.<Long, SftpClient>executeWithClient( client -> {
			try {
				return (long) client.stat( path ).getModifyTime().toMillis();
			}
			catch ( Exception e ) {
				LOG.error( "Unexpected error when checking last modified for {}", path, e );
			}
			return -1L;
		} );
	}
}
