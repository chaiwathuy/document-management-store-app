package uk.gov.hmcts.dm.service;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import uk.gov.hmcts.dm.componenttests.TestUtil;
import uk.gov.hmcts.dm.domain.DocumentContentVersion;
import uk.gov.hmcts.dm.exception.CantReadDocumentContentVersionBinaryException;

import java.io.OutputStream;
import java.net.URISyntaxException;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CloudBlobContainer.class, CloudBlockBlob.class})
@PowerMockIgnore({"javax.net.ssl.*"})
public class BlobStorageReadServiceTest {

    private BlobStorageReadService blobStorageReadService;

    private CloudBlockBlob blob;
    private CloudBlobContainer cloudBlobContainer;
    private DocumentContentVersion documentContentVersion;
    private OutputStream outputStream;

    @Before
    public void setUp() {
        cloudBlobContainer = PowerMockito.mock(CloudBlobContainer.class);
        blob = PowerMockito.mock(CloudBlockBlob.class);
        outputStream = mock(OutputStream.class);
        documentContentVersion = TestUtil.DOCUMENT_CONTENT_VERSION;
        blobStorageReadService = new BlobStorageReadService(cloudBlobContainer);
    }

    @Test
    public void loadsBlob() throws URISyntaxException, StorageException {
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString())).willReturn(blob);

        blobStorageReadService.loadBlob(documentContentVersion, outputStream);

        verify(blob).download(outputStream);
    }

    @Test(expected = CantReadDocumentContentVersionBinaryException.class)
    public void throwsCantReadDocumentContentVersionBinaryException() throws URISyntaxException, StorageException {
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId()
            .toString())).willThrow(new StorageException("404",
            "Message", mock(Exception.class)));

        blobStorageReadService.loadBlob(documentContentVersion, outputStream);
    }

    @Test
    public void doesBinaryExist() throws URISyntaxException, StorageException {
        given(cloudBlobContainer.getBlockBlobReference(documentContentVersion.getId().toString())).willReturn(blob);
        given(blob.exists()).willReturn(true);
        Assert.assertTrue(blobStorageReadService.doesBinaryExist(documentContentVersion.getId()));
    }

}
