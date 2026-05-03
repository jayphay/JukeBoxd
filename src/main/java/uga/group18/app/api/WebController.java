package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.group18.app.models.User;
import uga.group18.app.services.UserService;

import java.util.ArrayList;
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

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String query,
                             @RequestParam(required = false) String genre,
                             Model model) {

        StringBuilder sql = new StringBuilder("""
        SELECT s.songId, s.title, ar.artist_name, COALESCE(a.title, '') as albumTitle, s.genre 
        FROM song s 
        JOIN artist ar ON ar.artistId = s.artistId 
        LEFT JOIN album a ON a.albumId = s.albumId 
        WHERE 1=1 
    """);

        List<Object> params = new ArrayList<>();
        if (query != null && !query.isEmpty()) {
            sql.append(" AND (s.title LIKE ? OR ar.artist_name LIKE ?)");
            params.add("%" + query + "%"); params.add("%" + query + "%");
        }
        if (genre != null && !genre.isEmpty()) {
            sql.append(" AND s.genre = ?");
            params.add(genre);
        }

        List<HomeController.SongItem> results = jdbc.query(sql.toString(), (rs, rowNum) ->
                new HomeController.SongItem(
                        rs.getString("songId"), rs.getString("title"),
                        rs.getString("artist_name"), rs.getString("albumTitle"), rs.getString("genre")
                ), params.toArray());

        model.addAttribute("results", results);
        model.addAttribute("searchQuery", query);
        return "search-results"; // Returns search-results.html
    }

    public record UserItem(Integer userId, String username, String firstName, String lastName) {}
    @GetMapping("/members")
    public String memberSearch(@RequestParam(required = false) String username, Model model) {
        String sql = """
        SELECT userId, username, firstName, lastName 
        FROM user 
        WHERE username LIKE ? OR firstName LIKE ? OR lastName LIKE ?
        LIMIT 20
    """;

        String pattern = (username == null || username.isEmpty()) ? "%" : "%" + username + "%";

        List<UserItem> members = jdbc.query(sql, (rs, rowNum) -> new UserItem(
                rs.getInt("userId"),
                rs.getString("username"),
                rs.getString("firstName"),
                rs.getString("lastName")
        ), pattern, pattern, pattern);

        model.addAttribute("members", members);
        model.addAttribute("searchTerm", username);
        return "members"; // Returns members.html
    }

}
