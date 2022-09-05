package com.example.polls.controllers;

import com.example.polls.controller.AuthController;
import com.example.polls.model.Role;
import com.example.polls.model.RoleName;
import com.example.polls.model.User;
import com.example.polls.payload.ApiResponse;
import com.example.polls.payload.JwtAuthenticationResponse;
import com.example.polls.payload.LoginRequest;
import com.example.polls.payload.SignUpRequest;
import com.example.polls.repository.RoleRepository;
import com.example.polls.repository.UserRepository;
import com.example.polls.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {

    private ObjectMapper objectMapper;

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtTokenProvider tokenProvider;

    AuthController controller;

    MockMvc mockMvc;

    Gson gson;


    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        gson = new Gson();

        controller = new AuthController(
            authenticationManager,
            userRepository,
            roleRepository,
            passwordEncoder,
            tokenProvider
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testRegisterUserSuccessfulUserCreationRequest() throws Exception {
        SignUpRequest signUpRequest = getSignUpRequest();

        Mockito.when(
            roleRepository.findByName(Mockito.any(RoleName.class))
        ).thenReturn(Optional.of(new Role(RoleName.ROLE_USER)));

        Mockito.when(
           userRepository.save(Mockito.any())
        ).thenReturn(getUser());

        String jsonRequest = objectMapper.writeValueAsString(signUpRequest);

        MvcResult mvcResult =
            mockMvc.perform(
                    post("/api/auth/signup")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        Assertions.assertEquals(201, mvcResult.getResponse().getStatus());
    }

    @Test
    public void testRegisterUserSuccessResponse() throws Exception {
        SignUpRequest signUpRequest = getSignUpRequest();

        Mockito.when(
            roleRepository.findByName(Mockito.any(RoleName.class))
        ).thenReturn(Optional.of(new Role(RoleName.ROLE_USER)));

        Mockito.when(
            userRepository.save(Mockito.any())
        ).thenReturn(getUser());

        String jsonRequest = objectMapper.writeValueAsString(signUpRequest);

        MvcResult mvcResult =
            mockMvc.perform(
                    post("/api/auth/signup")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        Gson gson = new Gson();
        ApiResponse apiResponse = gson.fromJson(jsonResult, ApiResponse.class);
        Assertions.assertTrue(apiResponse.getSuccess());
        Assertions.assertEquals(USER_REGISTERED_SUCCESSFULLY, apiResponse.getMessage());
    }

    @Test
    public void testRegisterUserWhenUserAlreadyExistsByUsername() throws Exception {
        SignUpRequest signUpRequest = getSignUpRequest();

        Mockito.when(userRepository.existsByUsername(signUpRequest.getUsername()))
            .thenReturn(true);

        String jsonRequest = objectMapper.writeValueAsString(signUpRequest);

        MvcResult mvcResult =
            mockMvc.perform(
                    post("/api/auth/signup")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        ApiResponse apiResponse = gson.fromJson(jsonResult,ApiResponse.class);
        Assertions.assertFalse(apiResponse.getSuccess());
        Assertions.assertEquals(USERNAME_ALREADY_TAKEN, apiResponse.getMessage());
    }

    @Test
    public void testRegisterUserWhenUserAlreadyExistsByEmail() throws Exception {
        SignUpRequest signUpRequest = getSignUpRequest();

        Mockito.when(userRepository.existsByEmail(signUpRequest.getEmail()))
            .thenReturn(true);

        String jsonRequest = objectMapper.writeValueAsString(signUpRequest);

        MvcResult mvcResult =
            mockMvc.perform(
                    post("/api/auth/signup")
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();
        ApiResponse apiResponse = gson.fromJson(jsonResult,ApiResponse.class);
        Assertions.assertFalse(apiResponse.getSuccess());
        Assertions.assertEquals(EMAIL_ADDRESS_ALREADY_IN_USE, apiResponse.getMessage());
    }

    @Test
    public void testAuthenticateUserSuccess() throws Exception {
        LoginRequest loginRequest =
            new LoginRequest("test", "test123");
        Mockito.when(
            authenticationManager.authenticate(Mockito.any())
        ).thenReturn(new UsernamePasswordAuthenticationToken(
            loginRequest.getUsernameOrEmail(),
            loginRequest.getPassword()
        ));

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        MvcResult mvcResult =
            mockMvc.perform(
                post("/api/auth/signin")
                    .content(jsonRequest)
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        Assertions.assertEquals(200, mvcResult.getResponse().getStatus());
    }

    @Test
    public void testAuthenticateUserSuccessResponseAuthenticationToken() throws Exception {
        LoginRequest loginRequest =
            new LoginRequest("test", "test123");

        Mockito.when(
            authenticationManager.authenticate(Mockito.any())
        ).thenReturn(new UsernamePasswordAuthenticationToken(
            loginRequest.getUsernameOrEmail(),
            loginRequest.getPassword()
        ));

        String expectedToken = UUID.randomUUID().toString();
        Mockito.when(
            tokenProvider.generateToken(Mockito.any())
        ).thenReturn(expectedToken);

        String jsonRequest = objectMapper.writeValueAsString(loginRequest);

        MvcResult mvcResult = mockMvc.perform(
                post("/api/auth/signin")
                    .content(jsonRequest)
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn();

        String jsonResult = mvcResult.getResponse().getContentAsString();

        JwtAuthenticationResponse jwtAuthenticationResponse =
            gson.fromJson(jsonResult, JwtAuthenticationResponse.class);
        Assertions.assertEquals(expectedToken, jwtAuthenticationResponse.getAccessToken());
    }

    public User getUser() {
        return new User(
            "Test User",
            "test",
            "test@test.com",
            "test123"
        );
    }

    private SignUpRequest getSignUpRequest() {
        return new SignUpRequest(
            "Test User",
            "test",
            "test@test.com",
            "test123"
        );
    }

    private static final String USERNAME_ALREADY_TAKEN = "Username is already taken!";
    private static final String EMAIL_ADDRESS_ALREADY_IN_USE = "Email Address already in use!";
    private static final String USER_REGISTERED_SUCCESSFULLY = "User registered successfully";
}
