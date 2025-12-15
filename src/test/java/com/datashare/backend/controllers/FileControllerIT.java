package com.datashare.backend.controllers;

import com.datashare.backend.model.AppUser;
import com.datashare.backend.model.File;
import com.datashare.backend.model.Share;
import com.datashare.backend.repository.AppUserRepository;
import com.datashare.backend.repository.FileRepository;
import com.datashare.backend.repository.ShareRepository;
import com.datashare.backend.services.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class FileControllerIT {

    private MockMvc mockMvc;

    @Mock
    FileStorageService fileStorageService;

    @Mock
    FileRepository fileRepository;

    @Mock
    AppUserRepository userRepository;

    @Mock
    ShareRepository shareRepository;

    @Mock
    SecurityContext securityContext;

    @Mock
    Authentication authentication;

    @Mock
    UserDetails userDetails;

    @InjectMocks
    FileController fileController;

    private AppUser mockUser;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(fileController).build();

        mockUser = new AppUser();
        mockUser.setId(1L);
        mockUser.setEmail("test@test.com");
    }

    private void mockAuthentication() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("test@test.com");
        lenient().when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(mockUser));
    }

    @Test
    public void testUploadFile_Success() throws Exception {
        mockAuthentication();

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "content".getBytes());
        when(fileStorageService.store(any())).thenReturn("path/to/test.txt");
        when(fileRepository.save(any(File.class))).thenAnswer(i -> {
            File f = i.getArgument(0);
            f.setId(10L);
            return f;
        });

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Fichier téléversé avec succès"))
                .andExpect(jsonPath("$.shareToken").exists());

        verify(fileRepository, times(1)).save(any(File.class));
        verify(shareRepository, times(1)).save(any(Share.class));
    }

    @Test
    public void testGetListFiles_Success() throws Exception {
        mockAuthentication();

        File file = new File();
        file.setId(10L);
        file.setOriginalName("test.txt");
        file.setOwner(mockUser);
        file.setCreatedAt(LocalDateTime.now());

        Share share = new Share();
        share.setUniqueToken("token-123");
        share.setDownloadCount(5);

        when(fileRepository.findByOwnerId(1L)).thenReturn(Collections.singletonList(file));
        when(shareRepository.findByFileId(10L)).thenReturn(Optional.of(share));

        mockMvc.perform(get("/api/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originalName").value("test.txt"))
                .andExpect(jsonPath("$[0].shareToken").value("token-123"))
                .andExpect(jsonPath("$[0].downloadCount").value(5));
    }
}
