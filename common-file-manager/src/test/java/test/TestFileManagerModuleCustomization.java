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
package test;

import com.foreach.across.modules.filemanager.services.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class TestFileManagerModuleCustomization
{
	@Autowired
	private FileManager fileManager;

	@Autowired
	private FileManagerAware myRepository;

	@Test
	void fileRepositoryBeansAreAutomaticallyPickedUp() {
		FileRepositoryDelegate fileRepository = (FileRepositoryDelegate) fileManager.getRepository( "my-repository" );
		assertThat( fileRepository.getActualImplementation() ).isSameAs( myRepository );
		verify( myRepository ).setFileManager( fileManager );
	}

	@Test
	void customFileRepositoryFactoryIsUsed() {
		FileRepository createdRepository = fileManager.getRepository( "newlyCreatedRepository" );
		assertThat( createdRepository ).isNotNull();
		verify( ( (FileManagerAware) ( (FileRepositoryDelegate) createdRepository ).getActualImplementation() ) ).setFileManager( fileManager );
		assertThat( fileManager.getRepository( "newlyCreatedRepository" ) ).isSameAs( createdRepository );
	}

	@Configuration
	static class RepositoriesConfiguration
	{
		@Bean
		FileManagerImpl fileManager( List<FileRepository> repositories ) {
			FileManagerImpl fileManager = new FileManagerImpl();
			fileManager.setFileRepositoryFactory( fileRepositoryFactory() );
			repositories.forEach( fileManager::registerRepository );
			return fileManager;
		}

		@Bean
		FileRepository myRepository() {
			FileRepository fileRepository = mock( FileRepository.class, withSettings().extraInterfaces( FileManagerAware.class ) );
			when( fileRepository.getRepositoryId() ).thenReturn( "my-repository" );
			return fileRepository;
		}

		@Bean
		FileRepositoryFactory fileRepositoryFactory() {
			FileRepositoryFactory factory = mock( FileRepositoryFactory.class );
			FileRepository repository = mock( FileRepository.class, withSettings().extraInterfaces( FileManagerAware.class ) );
			when( repository.getRepositoryId() ).thenReturn( UUID.randomUUID().toString() );
			when( factory.create( any() ) ).thenReturn( repository );
			return factory;
		}
	}
}
