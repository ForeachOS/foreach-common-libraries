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
package com.foreach.common.filerepo.test.utils;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.testcontainers.containers.GenericContainer;

public class AzuriteContainer extends GenericContainer<AzuriteContainer>
{

	public AzuriteContainer() {
		super( "mcr.microsoft.com/azure-storage/azurite" );
		for ( Service service : Service.values() ) {
			addExposedPort( service.getPort() );
		}
	}

	public BlobServiceClient storageAccount() {
		return new BlobServiceClientBuilder()
				.endpoint( String.format( "http://%s:%s/devstoreaccount1", getHost().equals( "localhost" ) ? "127.0.0.1" : getHost(),
				                          getMappedPort( Service.BLOB.port ) ) )
				.credential( new StorageSharedKeyCredential( "devstoreaccount1",
				                                             "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==" ) )
				.buildClient();
	}

	@RequiredArgsConstructor
	@Getter
	@FieldDefaults(makeFinal = true)
	public enum Service
	{
		BLOB( 10000 ),
		QUEUE( 10001 ),
		TABLE( 10002 );

		int port;
	}
}
