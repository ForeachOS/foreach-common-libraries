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

import com.foreach.common.filemanager.business.FileRepositoryResource;
import com.foreach.common.filemanager.business.FileResource;
import com.foreach.common.filemanager.business.FolderDescriptor;
import com.foreach.common.filemanager.business.FolderResource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.integration.file.remote.RemoteFileTemplate;

import java.util.Collection;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public abstract class SpringIntegrationFolderResource implements FolderResource
{
	private final FolderDescriptor folderDescriptor;
	private final RemoteFileTemplate remoteFileTemplate;

	@Override
	public FolderDescriptor getDescriptor() {
		return folderDescriptor;
	}

	@Override
	public boolean exists() {
		return remoteFileTemplate.exists( getPath() );
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean delete( boolean deleteChildren ) {
		if ( !exists() ) {
			return false;
		}

		Collection<FileRepositoryResource> resources = findResources( "*" );
		if ( !deleteChildren && !resources.isEmpty() ) {
			return false;
		}
		if ( !resources.isEmpty() ) {
			resources.forEach( frr -> {
				if ( frr instanceof FolderResource ) {
					( (FolderResource) frr ).delete( deleteChildren );
				}
				if ( frr instanceof FileResource ) {
					( (FileResource) frr ).delete();
				}
			} );
		}
		return (boolean) remoteFileTemplate.execute( session -> session.rmdir( getPath() ) );
	}

	@Override
	public boolean deleteChildren() {
		Collection<FileRepositoryResource> resources = findResources( "*" );
		return !resources.isEmpty() && resources.stream()
		                                        .allMatch( f -> {
			                                        if ( f instanceof FileResource ) {
				                                        return ( (FileResource) f ).delete();
			                                        }
			                                        if ( f instanceof FolderResource ) {
				                                        return ( (FolderResource) f ).delete( true );
			                                        }
			                                        return false;
		                                        } );
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean create() {
		if ( exists() ) {
			return false;
		}
		Optional<FolderResource> optionalParent = getParentFolderResource();
		if ( optionalParent.isPresent() ) {
			FolderResource parent = optionalParent.get();
			if ( !parent.exists() ) {
				boolean created = parent.create();
				LOG.debug( "Parent for '{}' did not exist, attempted to create parent with result: {}", this.getDescriptor(), created );
			}
		}
		return (boolean) remoteFileTemplate.execute( session -> session.mkdir( getPath() ) );
	}

	protected String getPath() {
		return getPath( folderDescriptor );
	}

	protected static String getPath( FolderDescriptor descriptor ) {
		return StringUtils.prependIfMissing( StringUtils.defaultString( descriptor.getFolderId(), "/" ), "/" );
	}

	@Override
	public boolean equals( Object o ) {
		if ( this == o ) {
			return true;
		}
		return o != null && ( o instanceof FolderResource && folderDescriptor.equals( ( (FolderResource) o ).getDescriptor() ) );
	}

	@Override
	public int hashCode() {
		return folderDescriptor.hashCode();
	}

	@Override
	public String toString() {
		return "axfs [" + folderDescriptor.toString() + "] -> " + String.format( "FTP folder[path='%s']", getPath() );
	}
}
