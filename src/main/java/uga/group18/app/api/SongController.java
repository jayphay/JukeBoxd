package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final JdbcTemplate jdbc;

    public SongController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record SongDetails(
            String songId,
            String title,
            String genre,
            String albumId,
            String albumTitle,
            String artistName,
            String avgRating
    ) {}

    public record SongReview(
            String username,
            String comment,
            String rating
    ) {}

    @GetMapping("/{songId}")
    public ResponseEntity<SongDetails> getSongDetails(@PathVariable String songId) {
        String sql = """
            SELECT 
                s.songId, s.title, s.genre, s.albumId, 
                al.title AS albumTitle, 
                ar.artist_name,
                AVG(CAST(r.rating AS DECIMAL(3,1))) AS avgRating
            FROM song s
            JOIN artist ar ON s.artistId = ar.artistId
            LEFT JOIN album al ON s.albumId = al.albumId
            LEFT JOIN review r ON r.songId = s.songId
            WHERE s.songId = ?
            GROUP BY s.songId, s.title, s.genre, s.albumId, al.title, ar.artist_name
            """;

        List<SongDetails> results = jdbc.query(sql, (rs, rowNum) -> new SongDetails(
                rs.getString("songId"),
                rs.getString("title"),
                rs.getString("genre"),
                rs.getString("albumId"),
                rs.getString("albumTitle"),
                rs.getString("artist_name"),
                rs.getString("avgRating") != null ? String.format("%.1f", rs.getDouble("avgRating")) : "—"
        ), songId);

        return results.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(results.get(0));
    }

    @GetMapping("/{songId}/reviews")
    public ResponseEntity<List<SongReview>> getRecentReviews(@PathVariable String songId) {
        String sql = """
            SELECT u.username, r.comment, r.rating
            FROM review r
            JOIN user u ON r.userId = u.userId
            WHERE r.songId = ?
            ORDER BY r.userId DESC -- Placeholder for recency
            LIMIT 5
            """;

        List<SongReview> reviews = jdbc.query(sql, (rs, rowNum) -> new SongReview(
                rs.getString("username"),
                rs.getString("comment"),
                rs.getString("rating")
        ), songId);

        return ResponseEntity.ok(reviews);
    }
}