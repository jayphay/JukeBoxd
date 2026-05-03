package uga.group18.app.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.group18.app.models.User;
import uga.group18.app.services.UserService;

import java.util.List;

@Controller
@RequestMapping("/create-review")
public class CreateReviewController {

    private final JdbcTemplate jdbc;
    private final UserService userService;

    @Autowired
    public CreateReviewController(JdbcTemplate jdbc, UserService userService) {
        this.jdbc = jdbc;
        this.userService = userService;
    }

    public record SongOption(String songId, String title, String artistName) {}

    /** Load all songs for the dropdown. */
    private List<SongOption> loadSongs() {
        return jdbc.query(
                """
                SELECT s.songId, s.title, ar.artist_name AS artistName
                FROM song s
                JOIN artist ar ON ar.artistId = s.artistId
                ORDER BY s.title ASC
                """,
                (rs, rowNum) -> new SongOption(
                        rs.getString("songId"),
                        rs.getString("title"),
                        rs.getString("artistName")
                )
        );
    }

    /** Serve the create review page. */
    @GetMapping
    public ModelAndView page() {
        ModelAndView mv = new ModelAndView("create-review");
        mv.addObject("songs", loadSongs());
        return mv;
    }

    /** Handle the form submission and save to the database. */
    @PostMapping
    public ModelAndView submit(@RequestParam("songId") String songId,
                               @RequestParam("rating") String rating,
                               @RequestParam("comment") String comment) {

        User user = userService.getLoggedInUser();
        if (user == null) {
            return new ModelAndView("redirect:/login");
        }

        ModelAndView mv = new ModelAndView("create-review");
        mv.addObject("songs", loadSongs());

        try {
            jdbc.update(
                    """
                    INSERT INTO review (userId, songId, comment, rating)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE comment = VALUES(comment), rating = VALUES(rating)
                    """,
                    Integer.parseInt(user.getUserId()),
                    songId,
                    comment,
                    rating
            );
            mv.addObject("successMessage", "Your review was published!");
        } catch (Exception e) {
            mv.addObject("errorMessage", "Something went wrong: " + e.getMessage());
        }

        return mv;
    }
}