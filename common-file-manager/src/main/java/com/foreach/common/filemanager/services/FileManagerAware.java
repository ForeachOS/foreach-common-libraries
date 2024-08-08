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

/**
 * Interface that can be implemented by a {@link FileRepository}.
 * The {@link #setFileManager(FileManager)} will be called when the repository is attached to the {@link FileManager}.
 * The {@link #shutdown()} method will then be called when the file manager is being destroyed.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
public interface FileManagerAware
{
	/**
	 * Set the {@link FileManager} to which the repository is being attached.
	 *
	 * @param fileManager instance
	 */
	void setFileManager( FileManager fileManager );

	/**
	 * Called when the file manager is being destroyed.
	 */
	default void shutdown() {
	}
}
