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
import com.foreach.common.filemanager.test.utils.SftpContainer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

import java.io.File;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSpringIntegrationSftpFileRepository extends BaseFileRepositoryTest
{
	private static final SftpContainer ftpContainer = new SftpContainer();
	private static SftpRemoteFileTemplate template;

	@Override
	FileRepository createRepository() {
		if ( template == null ) {
			DefaultSftpSessionFactory defaultFtpSessionFactory = new DefaultSftpSessionFactory();
			defaultFtpSessionFactory.setUser( "fmm" );
			defaultFtpSessionFactory.setPassword( "test" );
			defaultFtpSessionFactory.setHost( "localhost" );
			defaultFtpSessionFactory.setPort( SftpContainer.TEST_PORT );
			defaultFtpSessionFactory.setTimeout( 5000 );
			defaultFtpSessionFactory.setChannelConnectTimeout( Duration.ofSeconds( 5 ) );
			defaultFtpSessionFactory.setAllowUnknownKeys( true );

			template = new SftpRemoteFileTemplate( defaultFtpSessionFactory );
			template.setAutoCreateDirectory( true );
		}

		FileManager fileManager = mock( FileManager.class );
		when( fileManager.createTempFile() ).thenAnswer( invoc -> new File( tempDir, UUID.randomUUID().toString() ) );

		SpringIntegrationSftpFileRepository ftp = SpringIntegrationSftpFileRepository.builder()
		                                                                             .repositoryId( "ftp-repo" )
		                                                                             .remoteFileTemplate( template )
		                                                                             .build();
		ftp.setFileManager( fileManager );

		return ftp;
	}

	@BeforeAll
	@SneakyThrows
	static void init() {
		ftpContainer.start();
	}

	@AfterAll
	static void tearDown() {
		ftpContainer.stop();
	}

	@Test
	@SneakyThrows
	void folderIsCreated() {
		FileResource file = fileRepository.getFileResource( FileDescriptor.of( "ftp-repo:aa/bb/cc:myfile" ) );
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( "ftp-repo:aa/" ) ).exists() ).isFalse();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( "ftp-repo:aa/bb/" ) ).exists() ).isFalse();
		assertThat( fileRepository.getFolderResource( FolderDescriptor.of( "ftp-repo:aa/bb/cc/" ) ).exists() ).isFalse();

		file.copyFrom( RES_TEXTFILE );

		assertThat( file.exists() ).isTrue();

		FolderResource cc = file.getFolderResource();
		assertThat( cc.getDescriptor() ).isEqualTo( FolderDescriptor.of( "ftp-repo:aa/bb/cc/" ) );
		assertThat( cc.listResources( false ) ).containsExactly( file );
		assertThat( cc.getFolderName() ).isEqualTo( "cc" );

		FolderResource bb = cc.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( bb.getDescriptor() ).isEqualTo( FolderDescriptor.of( "ftp-repo:aa/bb/" ) );
		assertThat( bb.listResources( false ) ).containsExactly( cc );
		assertThat( bb.getFolderName() ).isEqualTo( "bb" );

		FolderResource aa = bb.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( aa.getDescriptor() ).isEqualTo( FolderDescriptor.of( "ftp-repo:aa/" ) );
		assertThat( aa.listResources( false ) ).containsExactly( bb );
		assertThat( aa.getFolderName() ).isEqualTo( "aa" );
		assertThat( aa.listResources( true ) ).containsExactlyInAnyOrder( bb, cc, file );

		FolderResource root = aa.getParentFolderResource().orElseThrow( AssertionError::new );
		assertThat( root.getDescriptor() ).isEqualTo( FolderDescriptor.rootFolder( "ftp-repo" ) );
		assertThat( root.listResources( false ) ).contains( aa );
		assertThat( root.getFolderName() ).isEmpty();

		assertThat( root.getParentFolderResource() ).isEmpty();
	}

	@Test
	@SneakyThrows
	void createFolderAndFilesInFolder() {
		FolderResource folder = fileRepository.getFolderResource( FolderDescriptor.of( "ftp-repo:dd/cc/" ) );
		assertThat( folder.exists() ).isFalse();
		folder.create();

		assertThat( folder.exists() ).isTrue();
		assertThat( folder.listResources( false ) ).isEmpty();

		FileResource file = folder.createFileResource();
		assertThat( file.exists() ).isFalse();
		file.copyFrom( RES_TEXTFILE );
		assertThat( file.exists() ).isTrue();

		FileResource other = folder.getFileResource( "/myfile.txt" );
		assertThat( other.exists() ).isFalse();
		other.copyFrom( RES_TEXTFILE );
		assertThat( other.exists() ).isTrue();

		assertThat( folder.listResources( false ) ).containsExactlyInAnyOrder( file, other );

		FileResource nested = folder.getFileResource( "/subFolder/myfile.txt" );
		assertThat( nested.exists() ).isFalse();
		nested.copyFrom( RES_TEXTFILE );
		assertThat( nested.exists() ).isTrue();

		assertThat( folder.listResources( false ) ).containsExactlyInAnyOrder( folder.getFolderResource( "subFolder/" ), file, other );
		assertThat( folder.listResources( true ) ).containsExactlyInAnyOrder( folder.getFolderResource( "subFolder/" ), file, other, nested );

		assertThat( fileRepository.getRootFolderResource().listResources( false ) )
				.contains( folder.getParentFolderResource().orElseThrow( AssertionError::new ) );

		assertThat( folder.findResources( "/**/myfile.txt" ) ).containsExactlyInAnyOrder( other, nested );
		assertThat( folder.findResources( "/**/subFolder/*" ) ).containsExactly( nested );
	}

	@Test
	@SneakyThrows
	void fileInRootFolder() {
		FolderResource root = fileRepository.getRootFolderResource();
		FolderResource folderInRoot = root.getFolderResource( "ee" );

		FileResource fileInRoot = root.createFileResource();
		assertThat( fileInRoot.exists() ).isFalse();
		fileInRoot.copyFrom( RES_TEXTFILE );

		FileResource fileInFolderInRoot = folderInRoot.createFileResource();
		assertThat( fileInFolderInRoot.exists() ).isFalse();
		fileInFolderInRoot.copyFrom( RES_TEXTFILE );

		assertThat( folderInRoot.exists() ).isTrue();
		assertThat( root.listFolders() ).contains( folderInRoot );
		assertThat( root.listFiles() ).contains( fileInRoot ).doesNotContain( fileInFolderInRoot );
		assertThat( folderInRoot.listFiles() ).contains( fileInFolderInRoot );

		assertThat( root.findResources( "/ee" ) ).contains( folderInRoot );
		assertThat( root.findResources( "ee" ) ).contains( folderInRoot );
		assertThat( root.findResources( "/?e/*" ) ).contains( fileInFolderInRoot );
		assertThat( root.findResources( "/ee/*" ) ).contains( fileInFolderInRoot );
	}
}
