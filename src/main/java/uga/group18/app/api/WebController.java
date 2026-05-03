package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import uga.group18.app.models.User;
import uga.group18.app.services.UserService;
import java.util.List;

@Controller
public class WebController {

    private final JdbcTemplate jdbc;
    private final UserService userService;

    public WebController(JdbcTemplate jdbc, UserService userService) {
        this.jdbc = jdbc;
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/profile")
    public ModelAndView profile() {
        ModelAndView mv = new ModelAndView("profile");
        User user = userService.getLoggedInUser();
        mv.addObject("username", user != null ? user.getUsername() : null);
        return mv;
    }

    @GetMapping("/logout")
    public String logout() {
        userService.unAuthenticate();
        return "redirect:/login";
    }

    @ModelAttribute("genres")
    public List<String> getGenres() {
        return jdbc.queryForList("SELECT DISTINCT genre FROM song WHERE genre IS NOT NULL", String.class);
    }

    @ModelAttribute("loggedIn")
    public boolean isLoggedIn() {
        return userService.isAuthenticated();
    }

    @GetMapping("/listenlist")
    public String listenList(Model model) {
        // Replace with a real userId once you have a login system set up
        int currentUserId = 1;

        String sql = """
        SELECT s.songId, s.title, ar.artist_name 
        FROM listen_list ll
        JOIN song s ON ll.songId = s.songId
        JOIN artist ar ON s.artistId = ar.artistId
        WHERE ll.userId = ?
    """;

        List<HomeController.SongItem> userSongs = jdbc.query(sql, (rs, rowNum) -> new HomeController.SongItem(
                rs.getString("songId"),
                rs.getString("title"),
                rs.getString("artist_name"),
                null, // albumTitle (optional)
                null  // genre (optional)
        ), currentUserId);

        model.addAttribute("listenSongs", userSongs);
        return "listenlist"; // Looks for templates/listenlist.html
    }
}
