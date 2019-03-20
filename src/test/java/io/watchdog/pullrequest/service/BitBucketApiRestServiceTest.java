package io.watchdog.pullrequest.service;

import io.watchdog.pullrequest.config.RepositoryConfig;
import io.watchdog.pullrequest.dto.BitbucketUserDTO;
import io.watchdog.pullrequest.dto.BitbucketUserDTOWrapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author vladclaudiubulimac on 2019-03-20.
 */

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BitBucketApiRestServiceTest {

    @Mock
    RestTemplate restTemplate;
    @Mock
    RepositoryConfig repositoryConfig;

    @InjectMocks
    BitBucketApiRestService bitBucketApiRestService;

    @Test
    public void fetchBitbucketUserDetailsByEmailEmptyUser() {
        String email = "email@example.com";
        String usersUrl = "http://dummy.com/users/{email}";

        when(repositoryConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.getForObject(eq(usersUrl), eq(BitbucketUserDTOWrapper.class), eq(email))).thenReturn(null);

        BitbucketUserDTO bitbucketUserDTO = bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(email);

        assertThat(bitbucketUserDTO, notNullValue());
        assertThat(bitbucketUserDTO.getDisplayName(), nullValue());
        assertThat(bitbucketUserDTO.getFirstName(), nullValue());
        assertThat(bitbucketUserDTO.getLastName(), nullValue());
        assertThat(bitbucketUserDTO.getUsername(), nullValue());
    }

    @Test
    public void fetchBitbucketUserDetailsByEmail() {
        String email = "email@example.com";
        String usersUrl = "http://dummy.com/users/{email}";
        BitbucketUserDTO bitbucketUserDTO = new BitbucketUserDTO();
        bitbucketUserDTO.setDisplayName("BB bot");
        bitbucketUserDTO.setFirstName("BB");
        bitbucketUserDTO.setLastName("bot");
        bitbucketUserDTO.setUsername("bb-bot");
        BitbucketUserDTOWrapper bitbucketUserDTOWrapper = new BitbucketUserDTOWrapper();
        bitbucketUserDTOWrapper.setUser(bitbucketUserDTO);

        when(repositoryConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.getForObject(eq(usersUrl), eq(BitbucketUserDTOWrapper.class), eq(email))).thenReturn(bitbucketUserDTOWrapper);

        BitbucketUserDTO result = bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(email);

        assertThat(bitbucketUserDTO, notNullValue());
        assertEquals(bitbucketUserDTO, result);
    }

    @Test
    public void fetchBitbucketUserDetailsByEmailRestException() {
        String email = "email@example.com";
        String usersUrl = "http://dummy.com/users/{email}";

        when(repositoryConfig.getUsersUrl()).thenReturn(usersUrl);
        when(restTemplate.getForObject(eq(usersUrl), eq(BitbucketUserDTOWrapper.class), eq(email))).thenThrow(RestClientException.class);

        BitbucketUserDTO bitbucketUserDTO = bitBucketApiRestService.fetchBitbucketUserDetailsByEmail(email);

        assertThat(bitbucketUserDTO, notNullValue());
        assertThat(bitbucketUserDTO.getDisplayName(), nullValue());
        assertThat(bitbucketUserDTO.getFirstName(), nullValue());
        assertThat(bitbucketUserDTO.getLastName(), nullValue());
        assertThat(bitbucketUserDTO.getUsername(), nullValue());
    }
}