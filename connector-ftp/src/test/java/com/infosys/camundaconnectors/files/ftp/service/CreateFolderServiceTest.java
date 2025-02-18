/*
 * Copyright (c) 2023 Infosys Ltd.
 * Use of this source code is governed by MIT license that can be found in the LICENSE file
 * or at https://opensource.org/licenses/MIT
 */

package com.infosys.camundaconnectors.files.ftp.service;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.*;
import static org.mockito.ArgumentMatchers.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import com.infosys.camundaconnectors.files.ftp.model.response.FTPResponse;
import com.infosys.camundaconnectors.files.ftp.model.response.Response;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateFolderServiceTest {
	@Mock private FTPClient ftpClient;
	@Mock private CreateFolderService service;

	@BeforeEach
	public void init() {
	    service = new CreateFolderService();
	    service.setParentFolderPath("/Documents/ftproot");
	    service.setFolderName("Demo");
	    service.setActionIfFolderExists("rename");
	}

	@DisplayName("Should create given folder")
	@Test
	void validTestCreateFolder() throws Exception {
	    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true, false);	    
	    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
	    Mockito.when(ftpClient.isConnected()).thenReturn(true);
	    Response result = service.invoke(ftpClient);
	    @SuppressWarnings("unchecked")
	    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
	    Mockito.verify(ftpClient, Mockito.times(1)).logout();
	    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
	    assertThat(queryResponse)
	        .extracting("response")
	        .asInstanceOf(STRING)
	        .contains("New Folder Successfully created!!");
	}

	@DisplayName("Should replace given folder, if given folder is already exists")
	@Test
	void validTestCreateFolderReplace() throws Exception {
	    service.setActionIfFolderExists("replace");
	    FTPFile file1 = new FTPFile();
		file1.setName("a.txt");
		FTPFile file2 = new FTPFile();
		file2.setName("b.txt");
		FTPFile file3 = new FTPFile();
	    file3.setName("c.txt");
	    FTPFile[] files = {file1};
	    file1.setType(0);
	    Mockito.when(ftpClient.listFiles(any(String.class))).thenReturn(files);
	    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);	    
	    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
	    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
	    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
	    Mockito.when(ftpClient.isConnected()).thenReturn(true);
	    Response result = service.invoke(ftpClient);
	    @SuppressWarnings("unchecked")
	    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
	    Mockito.verify(ftpClient, Mockito.times(1)).logout();
	    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
	    assertThat(queryResponse)
	        .extracting("response")
	        .asInstanceOf(STRING)
	        .contains("New Folder Successfully created!!");
	}

	@DisplayName("Should rename given folder, if given folder is already exists")
	@Test
	void validTestCreateFolderRename() throws Exception {
	    service.setActionIfFolderExists("rename");
	    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true, true, false);	    
	    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);
	    Mockito.when(ftpClient.deleteFile(any(String.class))).thenReturn(true);
	    Mockito.when(ftpClient.removeDirectory(any(String.class))).thenReturn(true);
	    Mockito.when(ftpClient.isConnected()).thenReturn(true);
	    Response result = service.invoke(ftpClient);
	    @SuppressWarnings("unchecked")
	    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
	    Mockito.verify(ftpClient, Mockito.times(1)).logout();
	    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
	    assertThat(queryResponse)
	        .extracting("response")
	        .asInstanceOf(STRING)
	        .contains("New Folder Successfully created!!");
	}

	@DisplayName("Should skip this operation")
	@Test
	void validTestCreateFolderSkip() throws Exception {
	    service.setActionIfFolderExists("skip");
	    Mockito.when(ftpClient.changeWorkingDirectory(any(String.class))).thenReturn(true);	    
	    Mockito.when(ftpClient.makeDirectory(any(String.class))).thenReturn(true);	 
	    Mockito.when(ftpClient.isConnected()).thenReturn(true);
	    Response result = service.invoke(ftpClient);
	    @SuppressWarnings("unchecked")
	    FTPResponse<String> queryResponse = (FTPResponse<String>) result;
	    Mockito.verify(ftpClient, Mockito.times(1)).logout();
	    Mockito.verify(ftpClient, Mockito.times(1)).disconnect();
	    assertThat(queryResponse)
	        .extracting("response")
	        .asInstanceOf(STRING)
	        .contains("Operation skipped!!");
	}
}