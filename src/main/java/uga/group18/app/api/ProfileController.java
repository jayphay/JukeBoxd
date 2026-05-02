package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * REST endpoints that back the profile page.
 *
 *   GET /api/profile/{username}          → basic user info + aggregate stats
 *   GET /api/profile/{username}/reviews  → all reviews written by that user
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final JdbcTemplate jdbc;

    public ProfileController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── Records ──────────────────────────────────────────────────────────────

    public record UserProfile(
            Integer userId,
            String  username,
            String  firstName,
            String  lastName,
            int     reviewCount,
            int     listenListCount,
            String  avgRating        // formatted "4.2" or null
    ) {}

    public record UserReview(
            String songId,
            String songTitle,
            String artistName,
            String comment,
            String rating            // "1"–"5"
    ) {}

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /** Return profile info + aggregate stats for one user. */
    @GetMapping("/{username}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable String username) {

        List<UserProfile> results = jdbc.query(
                """
                SELECT
                  u.userId,
                  u.username,
                  u.firstName,
                  u.lastName,
                  COUNT(DISTINCT r.songId)          AS reviewCount,
                  COUNT(DISTINCT ll.songId)         AS listenListCount,
                  AVG(CAST(r.rating AS DECIMAL(3,1))) AS avgRating
                FROM user u
                LEFT JOIN review     r  ON r.userId  = u.userId
                LEFT JOIN listen_list ll ON ll.userId = u.userId
                WHERE u.username = ?
                GROUP BY u.userId, u.username, u.firstName, u.lastName
                """,
                (rs, rowNum) -> new UserProfile(
                        rs.getInt("userId"),
                        rs.getString("username"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getInt("reviewCount"),
                        rs.getInt("listenListCount"),
                        rs.getString("avgRating") != null
                                ? String.format("%.1f", rs.getDouble("avgRating"))
                                : null
                ),
                username
        );

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results.get(0));
    }

    /** Return all reviews written by a given user. */
    @GetMapping("/{username}/reviews")
    public ResponseEntity<List<UserReview>> getUserReviews(@PathVariable String username) {

        // First confirm the user exists
        List<Integer> ids = jdbc.queryForList(
                "SELECT userId FROM user WHERE username = ?",
                Integer.class,
                username
        );
        if (ids.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<UserReview> reviews = jdbc.query(
                """
                SELECT
                  r.songId,
                  s.title        AS songTitle,
                  ar.artist_name AS artistName,
                  r.comment,
                  r.rating
                FROM review r
                JOIN user   u  ON u.userId   = r.userId
                JOIN song   s  ON s.songId   = r.songId
                JOIN artist ar ON ar.artistId = s.artistId
                WHERE u.username = ?
                ORDER BY r.songId DESC
                """,
                (rs, rowNum) -> new UserReview(
                        rs.getString("songId"),
                        rs.getString("songTitle"),
                        rs.getString("artistName"),
                        rs.getString("comment"),
                        rs.getString("rating")
                ),
                username
        );

        return ResponseEntity.ok(reviews);
    }
}