package ecsimsw.picup.controller;

import ecsimsw.picup.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("api/user/{username}")
    public String read(@PathVariable String username){
        return userService.readUser(username).toString();
    }

    @PostMapping("api/user/{username}")
    public void save(@PathVariable String username){
        userService.addUser(username);
    }
}
