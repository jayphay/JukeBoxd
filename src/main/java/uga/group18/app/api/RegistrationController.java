package uga.group18.app.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.group18.app.services.UserService;

/**
 * Handles /register (GET to show the page, POST to process the form).
 */
@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final UserService userService;

    @Autowired
    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    /** Serve the registration page. */
    @GetMapping
    public ModelAndView page(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("register");
        mv.addObject("errorMessage", error);
        return mv;
    }

    /** Process the registration form. */
    @PostMapping
    public String register(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("passwordRepeat") String passwordRepeat,
                           @RequestParam("firstName") String firstName,
                           @RequestParam("lastName") String lastName) {

        if (password.trim().length() < 3) {
            String msg = URLEncoder.encode("Password must be at least 3 characters.",
                    StandardCharsets.UTF_8);
            return "redirect:/register?error=" + msg;
        }

        if (!password.equals(passwordRepeat)) {
            String msg = URLEncoder.encode("Passwords do not match.",
                    StandardCharsets.UTF_8);
            return "redirect:/register?error=" + msg;
        }

        try {
            boolean success = userService.registerUser(username, password, firstName, lastName);
            if (success) {
                return "redirect:/login";
            } else {
                String msg = URLEncoder.encode("Registration failed. Please try again.",
                        StandardCharsets.UTF_8);
                return "redirect:/register?error=" + msg;
            }
        } catch (Exception e) {
            // Most likely a duplicate username
            String msg = URLEncoder.encode("Username already taken. Please choose another.",
                    StandardCharsets.UTF_8);
            return "redirect:/register?error=" + msg;
        }
    }
}