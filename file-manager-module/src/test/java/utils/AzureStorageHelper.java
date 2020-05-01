package utils;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import lombok.experimental.UtilityClass;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

@UtilityClass
public class AzureStorageHelper
{
	public AzuriteContainer azurite = new AzuriteContainer();

	static {
		azurite.start();
	}

	public static void createFolder( CloudBlobClient cloudBlobClient, String containerName, String folderName ) {
		// create meta-data for your folder and set content-length to 0
		try {
			cloudBlobClient
					.getContainerReference( containerName )
					.getBlockBlobReference( folderName )
					.uploadText( "" );
		}
		catch ( StorageException | IOException | URISyntaxException e ) {
			e.printStackTrace();
		}
	}
}