package com.example.BazaDanychTwoFactor.controler;

import com.example.BazaDanychTwoFactor.dto.LoginRequest;
import com.example.BazaDanychTwoFactor.model.User;
import com.example.BazaDanychTwoFactor.dto.UserDto;
import com.example.BazaDanychTwoFactor.repository.UserRepository;
import com.example.BazaDanychTwoFactor.service.TokenService;
import com.example.BazaDanychTwoFactor.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

import static com.example.BazaDanychTwoFactor.security.Utilities.checkUser;
import static com.example.BazaDanychTwoFactor.service.MailService.sendEmail;

/**
 * Klasa obsługuje endpointy związane z użytkownikiem
 */
@RestController
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, AuthenticationManager authenticationManager) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.userService = new UserService(userRepository, passwordEncoder);
    }

    /**
     * Rejestracja użytkownika
     *
     * @param userDto               dane użytkonika do rejestracji
     * @return                      zarejestrowany użytkownik
     * @throws RuntimeException
     */
    @PostMapping(value ="/user/registration")
    public ResponseEntity<String> registerUserAccount(@RequestBody UserDto userDto) throws RuntimeException {
        User registered = userService.registerNewUserAccount(userDto);

        return new ResponseEntity<>("userRegistered", HttpStatus.OK);
    }

    /**
     * Logowanie użytkownika
     *
     * @param userLogin                 dane do logowania użytkownika
     * @return                          zalogowany użytkownik
     * @throws AuthenticationException
     */
    @PostMapping(value ="/user/login")
    public String token(@RequestBody LoginRequest userLogin) throws AuthenticationException, IOException {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userLogin.login(), userLogin.password()));
        sendEmail(userService.showUserAccount(userLogin.login()).getEmail(), "Twoj Kod Dostepu",tokenService.generateToken(authentication));
        return "CHECK YOUR EMAIL";
    }

    /**
     * Usuwanie konta użytkownika
     *
     * @param login     login do usuwanego konta
     * @return          usuwany użytkownik
     */
    @DeleteMapping(value = "{login}/user/delete")
    public ResponseEntity<User> deleteUserAccount(@PathVariable String login) {
        User deleted = null;
        if(checkUser(login)){
            deleted = userService.deleteUserAccount(login);
            return new ResponseEntity<>(deleted, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    /**
     * Aktualizowanie danych użytkownika
     *
     * @param login     login do aktualizowanego konta
     * @param userDto   dane do aktualizacji
     * @return          zaktualizowany użytkownik
     */
    @PostMapping(value = "{login}/user/update")
    public ResponseEntity<User> updateUserAccount(@PathVariable String login, @RequestBody UserDto userDto) {
        if(checkUser(login)){
            User updated = userService.updateUserAccount(login, userDto);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    /**
     * Wyświetlenie danych użytkownika
     *
     * @param login     login do wyświetlanego konta
     * @return          wyświetlany użytkownik
     */
    @GetMapping(value = "{login}/user")
    public ResponseEntity<User> showUserAccount(@PathVariable String login) {
        User shown = null;
        if(checkUser(login)){
             shown = userService.showUserAccount(login);
            return new ResponseEntity<>(shown, HttpStatus.OK);

        }else{
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }
}
