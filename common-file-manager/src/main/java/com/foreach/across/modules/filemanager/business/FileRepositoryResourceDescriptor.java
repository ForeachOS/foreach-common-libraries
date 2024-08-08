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
package com.foreach.across.modules.filemanager.business;

import lombok.SneakyThrows;

import java.io.Serializable;
import java.net.URI;

/**
 * Represents a descriptor (unique identifier) to a {@link FileRepositoryResource}.
 *
 * @author Arne Vandamme
 * @see FileDescriptor
 * @see FolderDescriptor
 * @since 1.4.0
 */
public interface FileRepositoryResourceDescriptor extends Serializable
{
	/**
	 * @return actual resource URI
	 */
	@SneakyThrows
	default URI toResourceURI() {
		return new URI( FileDescriptor.PROTOCOL + getUri() );
	}

	/**
	 * @return internal URI string representation of the descriptor
	 */
	String getUri();

	/**
	 * @return Unique id of the repository this resource belongs to.
	 */
	String getRepositoryId();
}
