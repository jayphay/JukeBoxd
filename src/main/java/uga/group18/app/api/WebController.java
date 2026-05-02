package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class WebController {

    private final JdbcTemplate jdbc;

    public WebController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/profile")
    public String profile() {
        return "forward:/profile.html";
    }

    @ModelAttribute("genres")
    public List<String> getGenres() {
        return jdbc.queryForList("SELECT DISTINCT genre FROM song WHERE genre IS NOT NULL", String.class);
    }
}
