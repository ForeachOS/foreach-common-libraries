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

import com.foreach.common.filemanager.business.FileDescriptor;
import com.foreach.common.filemanager.business.FileResource;
import com.foreach.common.filemanager.business.FolderResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.common.SftpException;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * @author Steven Gentens
 * @since 2.3.0
 */
@Slf4j
public class SpringIntegrationSftpFileResource extends SpringIntegrationFileResource
{
	private final FileDescriptor fileDescriptor;
	private final SftpRemoteFileTemplate remoteFileTemplate;
	private SFTPFile file;

	SpringIntegrationSftpFileResource( FileDescriptor fileDescriptor,
	                                   SFTPFile file,
	                                   SftpRemoteFileTemplate remoteFileTemplate ) {
		super( fileDescriptor, remoteFileTemplate );
		this.fileDescriptor = fileDescriptor;
		this.file = file;
		this.remoteFileTemplate = remoteFileTemplate;
	}

	@Override
	public FolderResource getFolderResource() {
		return new SpringIntegrationSftpFolderResource( fileDescriptor.getFolderDescriptor(), remoteFileTemplate );
	}

	@Override
	public boolean exists() {
		return getSftpFile().exists();
	}

	@Override
	public boolean delete() {
		boolean deleted = super.delete();
		resetFileMetadata();
		return deleted;
	}

	@Override
	public URL getURL() throws IOException {
		throw new UnsupportedOperationException( "URL is not supported for an FTP FileResource" );
	}

	@Override
	public long contentLength() throws IOException {
		SFTPFile file = getSftpFile();
		if ( !file.exists() ) {
			throw new FileNotFoundException( "Unable to locate file " + fileDescriptor );
		}
		return file.getSize();
	}

	@Override
	public long lastModified() throws IOException {
		SFTPFile file = getSftpFile();
		if ( file == null ) {
			throw new FileNotFoundException( "Unable to locate file " + fileDescriptor );
		}
		return file.getLastModified();
	}

	@Override
	public FileResource createRelative( String relativePath ) {
		throw new UnsupportedOperationException( "Creating relative path is not yet supported" );
	}

	@Override
	public String getDescription() {
		return "axfs [" + fileDescriptor.toString() + "] -> " + String.format( "FTP file[path='%s']", getPath() );
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		boolean shouldCreateFile = !exists();
		Session<SftpClient.DirEntry> session = remoteFileTemplate.getSession();
		SftpClient client = (SftpClient) session.getClientInstance();

		if ( shouldCreateFile ) {
			instantiateAsEmptyFile( client );
		}
		resetFileMetadata();
		try {
			return client.write( getPath() );
		}
		catch ( SftpException e ) {
			LOG.error( "Unexpected error whilst opening an OutputStream for file {}", getPath() );
			throw new IOException( e );
		}
	}

	void resetFileMetadata() {
		this.file = null;
	}

	private void instantiateAsEmptyFile( SftpClient client ) throws IOException {
		FolderResource folder = getFolderResource();
		if ( !folder.exists() ) {
			folder.create();
		}

		//noinspection EmptyTryBlock
		try (var ignore = client.write( getPath() )) {
		}
		catch ( SftpException e ) {
			LOG.error( "Unable to create empty file at {}", getPath() );
			throw new IOException( e );
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if ( !exists() ) {
			throw new FileNotFoundException( "Unable to locate file " + fileDescriptor );
		}
		Session<SftpClient.DirEntry> session = remoteFileTemplate.getSession();
		SftpClient client = (SftpClient) session.getClientInstance();
		try {
			return client.read( getPath() );
		}
		catch ( SftpException e ) {
			LOG.error( "Unable to create inputstream for file {} ", getPath() );
			throw new IOException( e );
		}
	}

	private SFTPFile getSftpFile() {
		if ( file == null ) {
			this.file = remoteFileTemplate.executeWithClient( this::fetchFileInfo );
		}
		return file;
	}

	private SFTPFile fetchFileInfo( SftpClient client ) {
		return new SFTPFile( remoteFileTemplate /*client*/, getPath() );
	}
}
