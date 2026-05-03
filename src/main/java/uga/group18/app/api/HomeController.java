package uga.group18.app.api;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import uga.group18.app.models.User;
import uga.group18.app.services.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    private final JdbcTemplate jdbc;

    private final UserService userService;

    public HomeController(JdbcTemplate jdbc, UserService userService) {
        this.jdbc = jdbc;
        this.userService = userService;
    }

    public record AlbumItem(String albumId, String title, String artistName, Integer releaseYear, Integer songsCount) {}
    public record SongItem(String songId, String title, String artistName, String albumTitle, String genre, boolean isSaved) {}
    public record ArtistItem(Integer artistId, String artistName, Integer songsCount, Integer albumsCount) {}
    public record ReviewItem(Integer userId, String username, String songId, String songTitle, String artistName, String comment, String rating) {}

    @GetMapping("/popular-albums")
    public List<AlbumItem> popularAlbums() {
        return jdbc.query(
                """
                SELECT
                  a.albumId,
                  COALESCE(a.title, '') AS title,
                  ar.artist_name AS artistName,
                  a.release_year AS releaseYear,
                  COUNT(s.songId) AS songsCount
                FROM album a
                JOIN artist ar ON ar.artistId = a.artistId
                LEFT JOIN song s ON s.albumId = a.albumId
                GROUP BY a.albumId, a.title, ar.artist_name, a.release_year
                ORDER BY songsCount DESC, a.release_year DESC, a.title ASC
                LIMIT 10
                """,
                (rs, rowNum) -> new AlbumItem(
                        rs.getString("albumId"),
                        rs.getString("title"),
                        rs.getString("artistName"),
                        (Integer) rs.getObject("releaseYear"),
                        rs.getInt("songsCount")
                )
        );
    }

    @GetMapping("/popular-singles")
    public List<SongItem> popularSingles() {
        User user = userService.getLoggedInUser();
        // Use the logged-in ID, or a dummy value like -1 for guests
        String userId = (user != null) ? user.getUserId() : "-1";

        return jdbc.query(
                """
                SELECT
                  s.songId,
                  s.title,
                  ar.artist_name AS artistName,
                  COALESCE(a.title, '') AS albumTitle,
                  s.genre,
                  (CASE WHEN ll.songId IS NOT NULL THEN 1 ELSE 0 END) AS isSaved
                FROM song s
                JOIN artist ar ON ar.artistId = s.artistId
                LEFT JOIN album a ON a.albumId = s.albumId
                LEFT JOIN listen_list ll ON ll.songId = s.songId AND ll.userId = ?
                ORDER BY s.title ASC
                LIMIT 10
                """,
                (rs, rowNum) -> new SongItem(
                        rs.getString("songId"),
                        rs.getString("title"),
                        rs.getString("artistName"),
                        rs.getString("albumTitle"),
                        rs.getString("genre"),
                        rs.getInt("isSaved") == 1 // Add this to your SongItem record!
                ),
                userId
        );
    }

    @PostMapping("/listen-list/toggle")
    public ResponseEntity<Map<String, String>> toggleListenList(@RequestParam String songId) {
        User user = userService.getLoggedInUser();
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Please log in first"));
        }

        // Check if it already exists
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM listen_list WHERE userId = ? AND songId = ?",
                Integer.class, user.getUserId(), songId);

        if (count > 0) {
            jdbc.update("DELETE FROM listen_list WHERE userId = ? AND songId = ?", user.getUserId(), songId);
            return ResponseEntity.ok(Map.of("status", "removed"));
        } else {
            jdbc.update("INSERT INTO listen_list (userId, songId) VALUES (?, ?)", user.getUserId(), songId);
            return ResponseEntity.ok(Map.of("status", "added"));
        }
    }

    @GetMapping("/popular-artists")
    public List<ArtistItem> popularArtists() {
        return jdbc.query(
                """
                SELECT
                  ar.artistId,
                  ar.artist_name AS artistName,
                  COUNT(DISTINCT s.songId) AS songsCount,
                  COUNT(DISTINCT a.albumId) AS albumsCount
                FROM artist ar
                LEFT JOIN song s ON s.artistId = ar.artistId
                LEFT JOIN album a ON a.artistId = ar.artistId
                GROUP BY ar.artistId, ar.artist_name
                ORDER BY songsCount DESC, albumsCount DESC, ar.artist_name ASC
                LIMIT 12
                """,
                (rs, rowNum) -> new ArtistItem(
                        (Integer) rs.getObject("artistId"),
                        rs.getString("artistName"),
                        rs.getInt("songsCount"),
                        rs.getInt("albumsCount")
                )
        );
    }

    @GetMapping("/recent-reviews")
    public List<ReviewItem> recentReviews() {
        return jdbc.query(
                """
                SELECT
                  r.userId,
                  u.username,
                  r.songId,
                  s.title AS songTitle,
                  ar.artist_name AS artistName,
                  r.comment,
                  r.rating
                FROM review r
                JOIN user u ON u.userId = r.userId
                JOIN song s ON s.songId = r.songId
                JOIN artist ar ON ar.artistId = s.artistId
                ORDER BY r.userId DESC
                LIMIT 10
                """,
                (rs, rowNum) -> new ReviewItem(
                        (Integer) rs.getObject("userId"),
                        rs.getString("username"),
                        rs.getString("songId"),
                        rs.getString("songTitle"),
                        rs.getString("artistName"),
                        rs.getString("comment"),
                        rs.getString("rating")
                )
        );
    }
}

