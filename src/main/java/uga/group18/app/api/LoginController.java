package uga.group18.app.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.group18.app.services.UserService;

/**
 * Handles /login (GET to show the page, POST to process the form).
 */
@Controller
@RequestMapping("/login")
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /** Serve the login page. Also logs out any existing session. */
    @GetMapping
    public ModelAndView page(@RequestParam(name = "error", required = false) String error) {
        userService.unAuthenticate();
        ModelAndView mv = new ModelAndView("login");
        mv.addObject("errorMessage", error);
        return mv;
    }

    /** Process the login form. */
    @PostMapping
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password) {
        try {
            if (userService.authenticate(username, password)) {
                return "redirect:/";
            }
        } catch (SQLException e) {
            String msg = URLEncoder.encode("Authentication failed. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/login?error=" + msg;
        }

        String msg = URLEncoder.encode("Invalid username or password.",
                StandardCharsets.UTF_8);
        return "redirect:/login?error=" + msg;
    }
}