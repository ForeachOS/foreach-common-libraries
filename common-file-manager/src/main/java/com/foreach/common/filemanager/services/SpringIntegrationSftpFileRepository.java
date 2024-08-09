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
import com.foreach.common.filemanager.business.FolderDescriptor;
import com.foreach.common.filemanager.business.FolderResource;
import lombok.Builder;
import lombok.NonNull;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

/**
 * @author Steven Gentens
 * @since 2.3.0
 */
public class SpringIntegrationSftpFileRepository extends AbstractFileRepository
{
	private final SftpRemoteFileTemplate remoteFileTemplate;

	@Builder
	protected SpringIntegrationSftpFileRepository( @NonNull String repositoryId,
	                                               @NonNull SftpRemoteFileTemplate remoteFileTemplate,
	                                               PathGenerator pathGenerator ) {
		super( repositoryId );
		setPathGenerator( pathGenerator );
		this.remoteFileTemplate = remoteFileTemplate;

	}

	@Override
	protected FileResource buildFileResource( FileDescriptor descriptor ) {
		String path = SpringIntegrationSftpFileResource.getPath( descriptor );
		SFTPFile file = null;
		if ( remoteFileTemplate.exists( path ) ) {
			file = remoteFileTemplate.<SFTPFile, SftpClient>executeWithClient( client -> new SFTPFile( remoteFileTemplate, path ) );
		}
		return new SpringIntegrationSftpFileResource( descriptor, file, remoteFileTemplate );
	}

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		return new SpringIntegrationSftpFolderResource( descriptor, remoteFileTemplate );
	}
}
