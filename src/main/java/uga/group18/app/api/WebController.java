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
    public ModelAndView profile(@RequestParam(required = false) String user) {
        ModelAndView mv = new ModelAndView("profile");

        // 1. If a 'user' param exists in the URL (?user=alex), use that
        // 2. Otherwise, fall back to the logged-in user
        if (user != null && !user.isEmpty()) {
            mv.addObject("username", user);
        } else {
            User loggedIn = userService.getLoggedInUser();
            mv.addObject("username", loggedIn != null ? loggedIn.getUsername() : null);
        }

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
        User user = userService.getLoggedInUser();
        String userId = (user != null) ? user.getUserId() : "-1"; // Use -1 if guest

        // Updated SQL: Checks if the song is in the user's listen list
        StringBuilder sql = new StringBuilder("""
        SELECT s.songId, s.title, ar.artist_name, COALESCE(a.title, '') as albumTitle, s.genre,
               (CASE WHEN ll.songId IS NOT NULL THEN 1 ELSE 0 END) as isSaved
        FROM song s 
        JOIN artist ar ON ar.artistId = s.artistId 
        LEFT JOIN album a ON a.albumId = s.albumId 
        LEFT JOIN listen_list ll ON ll.songId = s.songId AND ll.userId = ?
        WHERE 1=1 
    """);

        List<Object> params = new ArrayList<>();
        params.add(userId); // Add userId for the LEFT JOIN check

        if (query != null && !query.isEmpty()) {
            sql.append(" AND (s.title LIKE ? OR ar.artist_name LIKE ?)");
            params.add("%" + query + "%"); params.add("%" + query + "%");
        }
        if (genre != null && !genre.isEmpty()) {
            sql.append(" AND s.genre = ?");
            params.add(genre);
        }

        // Map the results and include the "isSaved" status
        List<SongSearchItem> results = jdbc.query(sql.toString(), (rs, rowNum) ->
                new SongSearchItem(
                        rs.getString("songId"), rs.getString("title"),
                        rs.getString("artist_name"), rs.getString("genre"),
                        rs.getInt("isSaved") == 1
                ), params.toArray());

        model.addAttribute("results", results);
        model.addAttribute("searchQuery", query);
        return "search-results";
    }

    // Add this record inside WebController or as a separate file
    public record SongSearchItem(String songId, String title, String artistName, String genre, boolean isSaved) {}

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

    @GetMapping("/listenlist")
    public String listenList(Model model) {
        // Use the actual logged-in user from your service
        User user = userService.getLoggedInUser();

        if (user == null) {
            return "redirect:/login"; // Redirect to login if session is lost
        }

        String sql = """
        SELECT s.songId, s.title, ar.artist_name 
        FROM listen_list ll
        JOIN song s ON ll.songId = s.songId
        JOIN artist ar ON s.artistId = ar.artistId
        WHERE ll.userId = ?
    """;

        // Use user.getUserId() instead of hardcoded '1'
        List<HomeController.SongItem> userSongs = jdbc.query(sql, (rs, rowNum) ->
                new HomeController.SongItem(
                        rs.getString("songId"),
                        rs.getString("title"),
                        rs.getString("artist_name"),
                        null, // albumTitle
                        null, // genre
                        true  // isSaved (Setting this to true fixes the argument mismatch)
                ), user.getUserId());

        model.addAttribute("listenSongs", userSongs);
        model.addAttribute("username", user.getUsername());
        return "listenlist";
    }

}
