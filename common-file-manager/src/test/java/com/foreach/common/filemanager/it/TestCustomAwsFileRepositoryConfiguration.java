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

import com.amazonaws.services.s3.AmazonS3;
import com.foreach.common.filemanager.business.FileDescriptor;
import com.foreach.common.filemanager.business.FileResource;
import com.foreach.common.filemanager.services.*;
import com.foreach.common.filemanager.test.utils.AmazonS3Helper;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@DisplayName("AWS - Local file caching semantics")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TestCustomAwsFileRepositoryConfiguration
{
	private static final String BUCKET_NAME = "caching-test";
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@TempDir
	static File tempDir;

	@Test
	@SneakyThrows
	void cachedFileResource( @Autowired AmazonS3 amazonS3, @Autowired FileManager fileManager ) {
		FileResource myFile = fileManager.createFileResource( "s3" );
		myFile.copyFrom( RES_TEXTFILE );

		FileDescriptor tempFileDescriptor = FileDescriptor.of( FileManager.TEMP_REPOSITORY, myFile.getDescriptor().getFolderId(),
		                                                       myFile.getDescriptor().getFileId() );
		FileResource tempFile = fileManager.getFileResource( tempFileDescriptor );
		assertThat( tempFile.exists() ).isTrue();

		assertThat( readResource( myFile ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( tempFile ) ).isEqualTo( "some dummy text" );

		assertThat( amazonS3.getObjectAsString( BUCKET_NAME, "12/34/56/" + myFile.getDescriptor().getFileId() ) ).isEqualTo( "some dummy text" );
		amazonS3.putObject( BUCKET_NAME, "12/34/56/" + myFile.getDescriptor().getFileId(), "updated text" );

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
		@Bean(destroyMethod = "shutdown")
		AmazonS3 amazonS3() {
			return AmazonS3Helper.createClientWithBuckets( BUCKET_NAME );
		}

		@Bean
		FileManagerImpl fileManager() {
			FileManagerImpl fileManager = new FileManagerImpl();
			fileManager.setFileRepositoryFactory( new LocalFileRepositoryFactory( tempDir.getAbsolutePath(), null ) );
			return fileManager;
		}

		@Bean
		FileRepository remoteRepository( AmazonS3 amazonS3, FileManagerImpl fileManager ) {
			CachingFileRepository repo = CachingFileRepository
					.withTranslatedFileDescriptor()
					.expireOnShutdown( true )
					.targetFileRepository(
							AmazonS3FileRepository.builder()
							                      .repositoryId( "s3" )
							                      .amazonS3( amazonS3 )
							                      .bucketName( BUCKET_NAME )
							                      .pathGenerator( () -> "12/34/56" )
							                      .build()
					).build();
			fileManager.registerRepository( repo );
			return repo;
		}
	}
}
