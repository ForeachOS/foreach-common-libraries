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
package com.foreach.common.filemanager.it;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobServiceClient;
import com.foreach.common.filemanager.business.FileDescriptor;
import com.foreach.common.filemanager.business.FileResource;
import com.foreach.common.filemanager.services.*;
import com.foreach.common.filemanager.test.utils.AzuriteContainer;
import com.google.common.collect.ImmutableList;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@DisplayName("Azure - Local file caching semantics")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class TestCustomAzureFileRepositoryConfiguration
{
	private static final String CONTAINER_NAME = "caching-test";
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@TempDir
	static File tempDir;

	@Test
	@SneakyThrows
	void cachedFileResource( @Autowired BlobServiceClient blobServiceClient, @Autowired FileManager fileManager ) {
		FileResource myFile = fileManager.createFileResource( "az" );
		myFile.copyFrom( RES_TEXTFILE );

		FileDescriptor tempFileDescriptor = FileDescriptor.of( FileManager.TEMP_REPOSITORY, myFile.getDescriptor().getFolderId(),
		                                                       myFile.getDescriptor().getFileId() );
		FileResource tempFile = fileManager.getFileResource( tempFileDescriptor );
		assertThat( tempFile.exists() ).isTrue();

		assertThat( readResource( myFile ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( tempFile ) ).isEqualTo( "some dummy text" );
		assertThat(
				blobServiceClient.getBlobContainerClient( CONTAINER_NAME )
				                 .getBlobClient( "12/34/56/" + myFile.getDescriptor().getFileId() )
				                 .downloadContent()
				                 .toString()
		).isEqualTo( "some dummy text" );

		blobServiceClient.getBlobContainerClient( CONTAINER_NAME )
		                 .getBlobClient( "12/34/56/" + myFile.getDescriptor().getFileId() )
		                 .upload( BinaryData.fromString( "updated text" ), true );

		tempFile.delete();
		assertThat( tempFile.exists() ).isFalse();
		assertThat( readResource( myFile ) ).isEqualTo( "updated text" );
		assertThat( readResource( tempFile ) ).isEqualTo( "updated text" );
	}

	@SneakyThrows
	private String readResource( FileResource resource ) {
		try (InputStream is = resource.getInputStream()) {
			return StreamUtils.copyToString( is, Charset.defaultCharset() );
		}
	}

	@Configuration
	static class RepositoriesConfiguration
	{

		@Bean
		@SneakyThrows
		BlobServiceClient blobServiceClient( AzuriteContainer azurite ) {
			BlobServiceClient blobServiceClient = azurite.storageAccount();
			blobServiceClient.getBlobContainerClient( CONTAINER_NAME ).createIfNotExists();
			return blobServiceClient;
		}

		@Bean
		AzuriteContainer azurite() {
			AzuriteContainer azurite = new AzuriteContainer();
			azurite.setExposedPorts( ImmutableList.of( 10000, 10001 ) );
			azurite.setCommand( "azurite", "-l", "/data", "--blobHost", "0.0.0.0", "--queueHost", "0.0.0.0" );
			azurite.waitingFor( Wait.forListeningPort() );
			azurite.start();
			return azurite;
		}

		@Bean
		FileManagerImpl fileManager() {
			FileManagerImpl fileManager = new FileManagerImpl();
			fileManager.setFileRepositoryFactory( new LocalFileRepositoryFactory( tempDir.getAbsolutePath(), null ) );
			return fileManager;
		}

		@Bean
		FileRepository remoteRepository( BlobServiceClient blobServiceClient, FileManagerImpl fileManager ) {
			CachingFileRepository repo = CachingFileRepository
					.withTranslatedFileDescriptor()
					.expireOnShutdown( true )
					.targetFileRepository(
							AzureFileRepository.builder()
							                   .repositoryId( "az" )
							                   .blobServiceClient( blobServiceClient )
							                   .containerName( CONTAINER_NAME )
							                   .pathGenerator( () -> "12/34/56" )
							                   .build()
					).build();
			fileManager.registerRepository( repo );
			return repo;
		}
	}
}
