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

import com.foreach.common.filemanager.business.FileResource;

/**
 * Extends the default file resource interface with tracking properties which can be used for expiration.
 *
 * @author Arne Vandamme
 * @see ExpiringFileRepository
 * @see CachingFileRepository
 * @since 1.4.0
 */
public interface ExpiringFileResource extends FileResource
{
	/**
	 * @return timestamp when the the actual content of the file resource was last accessed.
	 */
	long getLastAccessTime();

	/**
	 * @return timestamp when this file resource was created (what that means depends on the actual implementation)
	 */
	long getCreationTime();
}
