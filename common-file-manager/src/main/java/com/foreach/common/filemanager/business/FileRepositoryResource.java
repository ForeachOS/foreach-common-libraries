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
package com.foreach.common.filemanager.business;

import com.foreach.common.filemanager.services.FileRepository;

import java.net.URI;

/**
 * Represent a single resource in a {@link FileRepository}.
 * Either a {@link FileResource} or a {@link FolderResource}.
 *
 * @author Arne Vandamme
 * @see FileResource
 * @see FolderResource
 * @since 1.4.0
 */
public interface FileRepositoryResource
{
	/**
	 * @return the unique descriptor to this resource
	 */
	FileRepositoryResourceDescriptor getDescriptor();

	/**
	 * @return resource URI to this resource
	 */
	default URI getURI() {
		return getDescriptor().toResourceURI();
	}

	/**
	 * @return true if the resource exists
	 */
	boolean exists();
}
