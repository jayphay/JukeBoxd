package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.DayOfWeek;
import java.util.List;

@Controller
public class ListenListController {
    private final JdbcTemplate jdbc;

    public ListenListController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
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
