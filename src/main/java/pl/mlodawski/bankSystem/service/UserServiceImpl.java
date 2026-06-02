package pl.mlodawski.bankSystem.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import pl.mlodawski.bankSystem.exception.ResourceNotFoundException;
import pl.mlodawski.bankSystem.model.BankAccount;
import pl.mlodawski.bankSystem.model.User;
import pl.mlodawski.bankSystem.model.dto.AuthRequest;
import pl.mlodawski.bankSystem.model.dto.RegisterRequest;
import pl.mlodawski.bankSystem.util.TotpUtil;

import java.math.BigDecimal;
import java.util.*;

/**
 * Implementation of the {@link UserService} interface.
 * Provides in-memory user management with password encoding and mock data initialization.
 */
@Service
public class UserServiceImpl implements UserService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final BankAccountServiceImpl bankAccountService;
    private final Map<Long, User> userRepository = new HashMap<>();
    private Long nextId = 1L;

    /**
     * Constructor that injects dependencies and initializes sample users.
     *
     * @param passwordEncoder           the password encoder used to hash user passwords
     * @param authenticationManager     the authentication manager responsible for validating credentials
     * @param securityContextRepository the repository used to manage the Spring Security context in HTTP sessions
     * @param bankAccountService        the service for managing bank accounts
     */
    @Autowired
    public UserServiceImpl(PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository, BankAccountServiceImpl bankAccountService) {
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.bankAccountService = bankAccountService;

        // Inicjalizacja przykładowych użytkowników
        initializeUsers();
    }

    /**
     * Creates a few example users (user, admin, moderator) for demonstration purposes
     * and initializes bank account for the default user.
     */
    private void initializeUsers() {
        // Dodanie użytkownika
        User user = new User();
        user.setFirstName("Jan");
        user.setLastName("Nowak");
        user.setUsername("user");
        user.setEmail("user@example.com");
        user.setPassword("user");
        user.setRoles(new String[]{"ROLE_USER"});
        createUser(user);

        BankAccount userAccount = new BankAccount();
        userAccount.setAccountNumber("PL00112233445566778899001122");
        userAccount.setPesel("98120378129");
        userAccount.setOwnerId(user.getId());
        userAccount.setBalance(BigDecimal.valueOf(1000));
        userAccount.setTransactionHistory(new ArrayList<>());
        bankAccountService.createAccount(userAccount);

        // Dodanie administratora
        User admin = new User();
        admin.setFirstName("Anna");
        admin.setLastName("Nowak");
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword("admin");
        admin.setRoles(new String[]{"ROLE_MODERATOR", "ROLE_ADMIN"});
        createUser(admin);

        // Dodanie moderatora
        User moderator = new User();
        moderator.setFirstName("Jan");
        moderator.setLastName("Kowalski");
        moderator.setUsername("moderator");
        moderator.setEmail("moderator@example.com");
        moderator.setPassword("moderator");
        moderator.setRoles(new String[]{"ROLE_MODERATOR"});
        createUser(moderator);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(userRepository.values());
    }

    /**
     * Returns a list of all users in the system.
     *
     * @return list of all users
     */
    @Override
    public User findById(Long id) {
        return userRepository.get(id);
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return the User object if found, otherwise null
     */
    @Override
    public User findByUsername(String username) {
        return userRepository.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Creates and saves a new user in the repository.
     * The password is encrypted before saving.
     *
     * @param user the user to create
     * @return the newly created user with assigned ID
     */
    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setId(nextId++);
        userRepository.put(user.getId(), user);
        return user;
    }

    /**
     * Updates an existing user.
     * If the password is not provided, the original password is preserved.
     *
     * @param id   ID of the user to update
     * @param user updated user data
     * @return the updated User object, or null if user not found
     */
    @Override
    public User updateUser(Long id, User user) {
        if (!userRepository.containsKey(id)) {
            return null;
        }

        // Zachowanie oryginalnego hasła, jeśli nie zostało zmienione
        User existingUser = userRepository.get(id);
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword(existingUser.getPassword());
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user.setId(id);
        userRepository.put(id, user);
        return user;
    }

    /**
     * Adds or removes a role from the specified user.
     * If the role is already assigned, it will be removed.
     * If the user has only one role, and you try to remove it, an exception will be thrown.
     *
     * @param userId the ID of the user
     * @param role   the role to toggle
     * @throws IllegalStateException if trying to remove the user's last remaining role
     */
    public void toggleRole(Long userId, String role) {
        User user = userRepository.get(userId);

        boolean hadUserRole = Arrays.asList(user.getRoles()).contains("ROLE_USER");

        Set<String> roles = new HashSet<>(Arrays.asList(user.getRoles()));

        if (roles.contains(role)) {
            if (roles.size() == 1) {
                throw new IllegalStateException("A user must have at least one role.");
            }
            roles.remove(role);
        } else {
            roles.add(role);
            // Jeśli właśnie dodaliśmy ROLE_USER, a wcześniej jej nie było
            if ("ROLE_USER".equals(role) && !hadUserRole) {
                try {
                    bankAccountService.getAccount(userId);
                } catch (ResourceNotFoundException e) {
                    BankAccount newAccount = new BankAccount();
                    newAccount.setOwnerId(userId);
                    newAccount.setPesel("11111111111");

                    StringBuilder iban = new StringBuilder("PL");
                    Random rand = new Random();
                    for (int i = 0; i < 26; i++) {
                        iban.append(rand.nextInt(10));
                    }

                    newAccount.setAccountNumber(iban.toString());
                    newAccount.setBalance(BigDecimal.ZERO);
                    newAccount.setTransactionHistory(new ArrayList<>());

                    bankAccountService.createAccount(newAccount);
                }
            }
        }
        user.setRoles(roles.toArray(new String[0]));
        userRepository.put(userId, user);
    }

    /**
     * Rejestruje nowego użytkownika i tworzy dla niego konto bankowe.
     *
     * @param request dane rejestracyjne
     * @return utworzony użytkownik
     * @throws IllegalArgumentException jeśli użytkownik już istnieje lub hasła się nie zgadzają
     */
    @Override
    public User registerNewUser(RegisterRequest request) {
        if (findByUsername(request.getUsername()) != null) {
            throw new IllegalArgumentException("A user with the given name already exists.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords must be identical.");
        }

        // Tworzenie użytkownika
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(request.getPassword());
        newUser.setRoles(new String[]{"ROLE_USER"});

        User savedUser = createUser(newUser);

        // Tworzenie konta bankowego
        BankAccount newAccount = new BankAccount();
        newAccount.setPesel(request.getPesel());
        newAccount.setOwnerId(savedUser.getId());
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setTransactionHistory(new ArrayList<>());

        StringBuilder iban = new StringBuilder("PL");
        Random rand = new Random();
        for (int i = 0; i < 26; i++) {
            iban.append(rand.nextInt(10));
        }
        newAccount.setAccountNumber(iban.toString());

        bankAccountService.createAccount(newAccount);

        return savedUser;
    }

    /**
     * Attempts to authenticate a user with username, password, and (optionally) a TOTP code if 2FA is enabled.
     *
     * @param request      the authentication request containing username, password, and totpCode
     * @param httpRequest  the current HTTP request (used to access session and security context)
     * @param httpResponse the current HTTP response (used to store security context)
     * @return true if authentication succeeds (including 2FA if required), false otherwise
     */
    @Override
    public boolean loginWith2FA(AuthRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = findByUsername(request.getUsername());

        if (user == null) {
            return false;
        }

        if(user.isMfaEnabled()){
            String totpCode = request.getTotpCode();
            if(!verifyTotp(user.getId(), totpCode)){
                return false;
            }
        }


        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

            HttpSession session = httpRequest.getSession();
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            return true;
        } catch (AuthenticationException e) {
            return false;
        }
    }

    /**
     * Generates a new TOTP secret for the given user, and stores it in the user's account.
     * This does not activate 2FA yet — verification is still required.
     *
     * @param userId the ID of the user for whom the secret is being generated
     * @return the newly generated Base32-encoded TOTP secret, or null if the user doesn't exist
     */
    @Override
    public String generateMfaSecret(Long userId) {
        User user = findById(userId);
        if(user == null){
            return null;
        }
        String secret = TotpUtil.generateSecret();

        user.setMfaSecret(secret);
        user.setMfaEnabled(false);
        user.setMfaVerified(false);
        return secret;
    }

    /**
     * Verifies the provided TOTP code against the user's secret and enables 2FA if the code is valid.
     *
     * @param userId   the ID of the user attempting to verify and activate 2FA
     * @param totpCode the TOTP code provided by the user from their authenticator app
     * @return true if the code is valid and 2FA was successfully activated; false otherwise
     */
    @Override
    public boolean verifyAndEnableMfa(Long userId, String totpCode) {
        User user = findById(userId);
        if (user == null || user.isMfaEnabled() || user.getMfaSecret() ==
                null) {
            return false;
        }
        // Weryfikuj kod TOTP
        boolean isValid = TotpUtil.verifyCode(user.getMfaSecret(),
                totpCode);
        if (isValid) {
            // Aktywuj 2FA
            user.setMfaEnabled(true);
            user.setMfaVerified(true);
        }
        return isValid;
    }

    /**
     * Verifies a TOTP code against the user's active secret (only if 2FA is already enabled).
     *
     * @param userId   the ID of the user attempting verification
     * @param totpCode the code to be verified
     * @return true if the code is valid; false if invalid, expired, or user is not 2FA-enabled
     */
    @Override
    public boolean verifyTotp(Long userId, String totpCode) {
        User user = findById(userId);
        if (user == null || !user.isMfaEnabled() || user.getMfaSecret()
                == null) {
            return false;
        }
        return TotpUtil.verifyCode(user.getMfaSecret(), totpCode);
    }

    @Override
    public boolean disableMfa(Long userId) {
        User user = findById(userId);
        if (user == null) {
            return false;
        }
        user.setMfaEnabled(false);
        user.setMfaSecret(null);
        user.setMfaVerified(false);
        return true;
    }

    /**
     * Deletes a user from the repository by ID.
     *
     * @param id the ID of the user to delete
     */
    @Override
    public void deleteUser(Long id) {
        userRepository.remove(id);
    }
}
