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

import com.azure.storage.blob.BlobServiceClient;
import com.foreach.common.filemanager.business.FileDescriptor;
import com.foreach.common.filemanager.business.FolderDescriptor;
import com.foreach.common.filemanager.business.FolderResource;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.nio.file.Paths;

@Getter
public class AzureFileRepository extends AbstractFileRepository
{
	private final String containerName;
	private final BlobServiceClient blobServiceClient;

	@Builder
	AzureFileRepository( @NonNull String repositoryId,
	                     @NonNull BlobServiceClient blobServiceClient,
	                     @NonNull String containerName,
	                     PathGenerator pathGenerator ) {
		super( repositoryId );
		setPathGenerator( pathGenerator );
		this.blobServiceClient = blobServiceClient;
		this.containerName = containerName;
	}

	@Override
	protected AzureFileResource buildFileResource( FileDescriptor descriptor ) {
		return new AzureFileResource( descriptor, blobServiceClient, containerName, createObjectName( descriptor ) );
	}

	private String createObjectName( FileDescriptor descriptor ) {
		String result;
		if ( descriptor.getFolderId() != null ) {
			result = Paths.get( descriptor.getFolderId(), descriptor.getFileId() ).toString();
		}
		else {
			result = Paths.get( descriptor.getFileId() ).toString();
		}

		return result.replace( "\\", "/" );
	}

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		String objectName = descriptor.getFolderId() != null ? descriptor.getFolderId() + "/" : "";
		return new AzureFolderResource( descriptor, blobServiceClient, containerName, objectName );
	}
}
