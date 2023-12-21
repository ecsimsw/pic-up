package ecsimsw.picup.service;

import ecsimsw.picup.domain.User;
import ecsimsw.picup.domain.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void addUser(String username) {
        userRepository.save(new User(username));
    }

    @Transactional(readOnly = true)
    public User readUser(String username) {
        return userRepository.findByUsername(username).get();
    }

}
