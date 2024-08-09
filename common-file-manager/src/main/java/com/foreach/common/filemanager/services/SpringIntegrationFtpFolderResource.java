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

import com.foreach.common.filemanager.business.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class SpringIntegrationFtpFolderResource extends SpringIntegrationFolderResource
{
	private final FolderDescriptor folderDescriptor;
	private final FtpRemoteFileTemplate remoteFileTemplate;

	SpringIntegrationFtpFolderResource( @NonNull FolderDescriptor folderDescriptor,
	                                    @NonNull FtpRemoteFileTemplate remoteFileTemplate ) {
		super( folderDescriptor, remoteFileTemplate );
		this.folderDescriptor = folderDescriptor;
		this.remoteFileTemplate = remoteFileTemplate;
	}

	@Override
	public boolean exists() {
		return retrieveRemoteFile( getPath() ) != null;
	}

	protected boolean exists( FTPClient client ) {
		return retrieveRemoteFile( client, getPath() ) != null;
	}

	@Override
	public Optional<FolderResource> getParentFolderResource() {
		return folderDescriptor.getParentFolderDescriptor()
		                       .map( fd -> new SpringIntegrationFtpFolderResource( fd, remoteFileTemplate ) );
	}

	@Override
	public FileRepositoryResource getResource( @NonNull String relativePath ) {
		if ( relativePath.isEmpty() || "/".equals( relativePath ) ) {
			return this;
		}

		if ( relativePath.endsWith( "/" ) ) {
			return new SpringIntegrationFtpFolderResource( folderDescriptor.createFolderDescriptor( relativePath ), remoteFileTemplate );
		}

		FileDescriptor fileDescriptor = folderDescriptor.createFileDescriptor( relativePath );
		String actualPath = SpringIntegrationFtpFileResource.getPath( fileDescriptor );
		FTPFile ftpFile = remoteFileTemplate.exists( actualPath )
				? retrieveRemoteFile( actualPath )
				: null;

		return new SpringIntegrationFtpFileResource( fileDescriptor, ftpFile, remoteFileTemplate );
	}

	@Override
	@SuppressWarnings("Duplicates")
	public Collection<FileRepositoryResource> findResources( @NonNull String pattern ) {
		try (Session session = remoteFileTemplate.getSession();) {
			FTPClient client = (FTPClient) session.getClientInstance();
			return findResources( pattern, client );
		}
	}

	@SuppressWarnings("Duplicates")
	protected Collection<FileRepositoryResource> findResources( @NonNull String pattern, FTPClient client ) {
		if ( exists( client ) ) {
			Set<FileRepositoryResource> resources = new LinkedHashSet<>();
			AntPathMatcher pathMatcher = new AntPathMatcher( "/" );
			String p = StringUtils.startsWith( pattern, "/" ) ? pattern.substring( 1 ) : pattern;
			boolean matchOnlyDirectories = StringUtils.endsWith( p, "/" );

			if ( matchOnlyDirectories ) {
				p = p.substring( 0, p.length() - 1 );
			}

			if ( !p.contains( "*" ) && !p.contains( "?" ) ) {
				return resolveExactPath( p, client );
			}

			BiPredicate<String, String> keyMatcher = ( candidateObjectName, antPattern ) -> {
				String patternToMatch = !StringUtils.isBlank( antPattern ) ? StringUtils.prependIfMissing( antPattern, "/" ) : antPattern;
				String path = patternToMatch.endsWith( "/" ) ? candidateObjectName : StringUtils.removeEnd( candidateObjectName, "/" );
				if ( pathMatcher.match( patternToMatch, path ) ) {
					return !matchOnlyDirectories || candidateObjectName.endsWith( "/" );
				}
				return false;
			};

			if ( !p.contains( "**" ) && !p.contains( "?" ) ) {
				return resolvePatternForListing( p, matchOnlyDirectories, keyMatcher, client );
			}

			findResourcesWithMatchingKeys( keyMatcher, resources, getPath() + getValidPrefix( p ), StringUtils.prependIfMissing( p, "/" ),
			                               matchOnlyDirectories, client );

			return resources;
		}

		return Collections.emptyList();
	}

	private Collection<FileRepositoryResource> resolveExactPath( String p, FTPClient client ) {
		String pathToSearch = StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( p, "/" );
		FTPFile ftpFile = retrieveRemoteFile( client, pathToSearch );

		if ( ftpFile == null ) {
			return Collections.emptyList();
		}

		if ( ftpFile.isDirectory() ) {
			return Collections.singletonList(
					new SpringIntegrationFtpFolderResource( FolderDescriptor.of( folderDescriptor.getRepositoryId(), pathToSearch ),
					                                        remoteFileTemplate ) );
		}
		return Collections.singletonList( createFileResource( ftpFile, StringUtils.removeStart( pathToSearch, "/" ) ) );
	}

	private Collection<FileRepositoryResource> resolvePatternForListing( String p,
	                                                                     boolean matchOnlyDirectories,
	                                                                     BiPredicate<String, String> keyMatcher,
	                                                                     FTPClient client ) {
		String validPrefix = getValidPrefix( p );
		String remainingPattern = StringUtils.removeStart( p, validPrefix );

		String pathToSearch =
				StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( validPrefix, "/" );
		// not yet the final part
		if ( remainingPattern.contains( "/" ) ) {
			int nextSlash = StringUtils.indexOf( remainingPattern, "/" );
			String nextPart = StringUtils.substring( remainingPattern, 0, nextSlash + 1 );
			String keyPattern = StringUtils.removeEnd( getPath(), "/" ) + "/" + StringUtils.removeStart( nextPart, "/" );
			String withoutNextPart = StringUtils.removeStart( remainingPattern, nextPart );
			return retrieveFoldersForPath( client, pathToSearch )
					.stream()
					.filter( f -> {
						String pathToTest = StringUtils.appendIfMissing( f.getPath(), "/" );
						return keyMatcher.test( pathToTest, keyPattern );
					} )
					.map( f -> {
						String pathToFind = matchOnlyDirectories ? StringUtils.appendIfMissing( withoutNextPart, "/" ) : withoutNextPart;
						return f.findResources( pathToFind, client );
					} )
					.flatMap( Collection::stream )
					.collect( Collectors.toSet() );
		}

		SpringIntegrationFtpFolderResource baseResource = validPrefix.isEmpty() ? this : (SpringIntegrationFtpFolderResource) getFolderResource( validPrefix );

		String keyPattern = StringUtils.removeEnd( baseResource.getPath(), "/" ) + "/" + StringUtils.removeStart( remainingPattern, "/" );
		if ( matchOnlyDirectories ) {
			return baseResource.retrieveFoldersForPath( client, pathToSearch )
			                   .stream()
			                   .filter( f -> {
				                   String pathToTest = StringUtils.appendIfMissing( f.getPath(), "/" );
				                   return keyMatcher.test( pathToTest, keyPattern );
			                   } )
			                   .collect( Collectors.toSet() );
		}

		return Stream.concat( baseResource.retrieveFoldersForPath( client, pathToSearch ).stream(),
		                      baseResource.retrieveFilesForPath( client, pathToSearch ).stream() )
		             .filter( f -> {
			             if ( f instanceof FolderResource ) {
				             String folderPath = SpringIntegrationFolderResource.getPath( ( (FolderResource) f ).getDescriptor() );
				             String pathToTest = StringUtils.appendIfMissing( folderPath, "/" );
				             return keyMatcher.test( pathToTest, keyPattern );
			             }
			             String pathToTest = SpringIntegrationFileResource.getPath( ( (FileResource) f ).getDescriptor() );
			             return keyMatcher.test( pathToTest, keyPattern );
		             } ).collect( Collectors.toSet() );
	}

	private FTPFile retrieveRemoteFile( String pathToSearch ) {
		return remoteFileTemplate.<FTPFile, FTPClient>executeWithClient( client -> retrieveRemoteFile( client, pathToSearch ) );
	}

	private FTPFile retrieveRemoteFile( FTPClient client, String path ) {
		try {
			return client.mlistFile( path );
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected exception whilst retrieving file for path '{}'", path, e );
			return null;
		}
	}

	private void findResourcesWithMatchingKeys( BiPredicate<String, String> keyMatcher,
	                                            Set<FileRepositoryResource> resources,
	                                            String currentPath,
	                                            String keyPattern, boolean matchOnlyDirectories, FTPClient client ) {
		if ( !keyPattern.endsWith( "/" ) ) {
			retrieveFilesForPath( client, currentPath )
					.stream()
					.filter( file -> {
						String path = SpringIntegrationFtpFileResource.getPath( file.getDescriptor() );
						return keyMatcher.test( path, keyPattern );
					} )
					.forEach( resources::add );
		}

		String pathToLookFor = currentPath;
		String patternBasedOnPath = keyPattern;
		if ( !keyPattern.startsWith( "**" ) && !keyPattern.startsWith( "?" ) ) {
			String nextValidPrefix = getValidPrefix( keyPattern );
			String remainingKeyPattern = StringUtils.removeStart( keyPattern, nextValidPrefix );
			pathToLookFor = currentPath + StringUtils.prependIfMissing( nextValidPrefix, "/" );
			pathToLookFor = pathToLookFor.replaceAll( "//", "/" );
			patternBasedOnPath = remainingKeyPattern;
		}
		String newKeyPattern = patternBasedOnPath;

		if ( !newKeyPattern.contains( "/" ) && !matchOnlyDirectories && newKeyPattern.contains( "?" ) ) {
			String matchingPattern = pathToLookFor + StringUtils.removeStart( newKeyPattern, "/" );
			List<SpringIntegrationFtpFileResource> filesForPath = retrieveFilesForPath( client, pathToLookFor );
			filesForPath.stream()
			            .filter( file -> {
				            String path = SpringIntegrationFileResource.getPath( file.getDescriptor() );
				            return keyMatcher.test( path, matchingPattern );
			            } )
			            .forEach( resources::add );
		}

		List<SpringIntegrationFtpFolderResource> folderResources = retrieveFoldersForPath( client, pathToLookFor );
		folderResources.stream()
		               .filter( folder -> {
			               String path = StringUtils.appendIfMissing( SpringIntegrationFtpFolderResource.getPath( folder.getDescriptor() ), "/" );
			               return keyMatcher.test( path, newKeyPattern );
		               } )
		               .forEach( resources::add );

		folderResources
				.forEach(
						f -> {
							String recursivePattern = matchOnlyDirectories ? StringUtils.appendIfMissing( newKeyPattern, "/" ) : newKeyPattern;
							resources.addAll( f.findResources( recursivePattern, client ) );
						}
				);

	}

	@SuppressWarnings("Duplicates")
	private String getValidPrefix( String keyPattern ) {
		int starIndex = keyPattern.indexOf( '*' );
		int markIndex = keyPattern.indexOf( '?' );
		int index = Math.min(
				starIndex == -1 ? keyPattern.length() : starIndex,
				markIndex == -1 ? keyPattern.length() : markIndex
		);
		String beforeIndex = keyPattern.substring( 0, index );
		return beforeIndex.contains( "/" ) ? beforeIndex.substring( 0, beforeIndex.lastIndexOf( '/' ) + 1 ) : "";
	}

	private List<SpringIntegrationFtpFolderResource> retrieveFoldersForPath( FTPClient client, String path ) {
		FTPFile[] ftpFiles = null;
		try {
			ftpFiles = client.listDirectories( path );
		}
		catch ( IOException e ) {
			LOG.error( "Unexpected error whilst listing directories for path '{}'. Falling back to no directories found.", path, e );
			ftpFiles = new FTPFile[0];
		}

		return Arrays.stream( ftpFiles )
		             .map( file -> new SpringIntegrationFtpFolderResource(
				             FolderDescriptor.of( folderDescriptor.getRepositoryId(), getPath() ).createFolderDescriptor( file.getName() ),
				             remoteFileTemplate ) )
		             .collect( Collectors.toList() );
	}

	private List<SpringIntegrationFtpFileResource> retrieveFilesForPath( FTPClient client, String path ) {
		FTPFile[] ftpFiles = null;
		try {
			ftpFiles = client.listFiles( path );
		}
		catch ( IOException e ) {
			ftpFiles = new FTPFile[0];
		}

		return Arrays.stream( ftpFiles )
		             .filter( ftpFile -> !ftpFile.isDirectory() )
		             .map( this::createFileResource )
		             .collect( Collectors.toList() );
	}

	private SpringIntegrationFtpFileResource createFileResource( FTPFile file ) {
		return createFileResource( file, file.getName() );
	}

	private SpringIntegrationFtpFileResource createFileResource( FTPFile file, String fullPath ) {
		String fileName = fullPath;
		String folderName = getPath();
		int lastIndexOfSlash = fileName.lastIndexOf( "/" );
		if ( lastIndexOfSlash != -1 ) {
			fileName = fileName.substring( lastIndexOfSlash + 1 );
			folderName = fullPath.replace( fileName, "" );
		}
		return new SpringIntegrationFtpFileResource( FileDescriptor.of( folderDescriptor.getRepositoryId(), folderName, fileName ),
		                                             file,
		                                             remoteFileTemplate );
	}

}
