package com.example.demo.controllers;

import com.example.demo.TestUtils;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.CreateUserRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class UserControllerTest {
    private UserController userController;
    private UserRepository userRepo = mock(UserRepository.class);
    private CartRepository cartRepo = mock(CartRepository.class);
    private BCryptPasswordEncoder bCryptPasswordEncoder = mock(BCryptPasswordEncoder.class);

    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Before
    public void init() {
        userController = new UserController();
        TestUtils.injectObjects(userController, "userRepository", userRepo);
        TestUtils.injectObjects(userController, "cartRepository", cartRepo);
        TestUtils.injectObjects(userController, "bCryptPasswordEncoder", bCryptPasswordEncoder);
    }

    @Test
    public void testCreateUserSuccess() throws Exception {
        when(bCryptPasswordEncoder.encode("password1")).thenReturn("Hashed");
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("user1");
        createUserRequest.setPassword("password1");
        createUserRequest.setConfirmPassword("password1");
        final ResponseEntity<User> response = userController.createUser(createUserRequest);
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        User user = response.getBody();
        assertNotNull(user);
        assertEquals(0, user.getId());
        assertEquals("user1", user.getUsername());
        assertEquals("Hashed", user.getPassword());
    }

    @Test
    public void testFindUserByUserName() throws Exception {
        User userFake = getUser();
        when(userRepo.findByUsername("user1")).thenReturn(userFake);
        final ResponseEntity<User> response = userController.findByUserName("user1");
        User user = response.getBody();
        assertNotNull(user);
        assertEquals(userFake.getUsername(), user.getUsername());
    }

    @Test
    public void testFindUserByUserNameNotExists() throws Exception {
        when(userRepo.findByUsername("user2")).thenReturn(null);
        final ResponseEntity<User> response = userController.findByUserName("user2");
        User user = response.getBody();
        assertNull(user);
    }

    @Test
    public void testCreateUserNameExists() throws Exception {
        User userFake = getUser();
        when(userRepo.findByUsername("user1")).thenReturn(userFake);

        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("user1");
        createUserRequest.setPassword("password1");

        thrown.expect(Exception.class);
        thrown.expectMessage("Username is exists");
        userController.createUser(createUserRequest);
    }

    @Test
    public void testCreateUserPasswordNotSame() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("user1");
        createUserRequest.setPassword("password1");
        createUserRequest.setConfirmPassword("password2");

        thrown.expect(Exception.class);
        thrown.expectMessage("Confirm password is not same");
        userController.createUser(createUserRequest);
    }

    @Test
    public void testCreateUserPasswordLengthNotMin() throws Exception {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("user1");
        createUserRequest.setPassword("pas");
        createUserRequest.setConfirmPassword("pas");

        thrown.expect(Exception.class);
        thrown.expectMessage("Password must more 6 character");
        userController.createUser(createUserRequest);
    }

    @Test
    public void testFindUserById() throws Exception {
        User userFake = getUser();
        when(userRepo.findById(0L)).thenReturn(Optional.of((userFake)));
        final ResponseEntity<User> response = userController.findById(0L);
        User user = response.getBody();
        assertNotNull(user);
        assertEquals(userFake.getUsername(), user.getUsername());
    }

    @Test
    public void testFindUserByIdNotExists() throws Exception {
        final ResponseEntity<User> response = userController.findById(1L);
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
    }

    private User getUser() {
        User user = new User();
        Cart cart = new Cart();
        user.setId(0);
        user.setUsername("user1");
        user.setPassword("Hashed");
        user.setCart(cart);
        return user;
    }
}
