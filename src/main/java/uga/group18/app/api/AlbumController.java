package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/albums")
public class AlbumController {

    private final JdbcTemplate jdbc;

    public AlbumController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // ── Records ──────────────────────────────────────────────────────────────

    public record AlbumInfo(
            String albumId,
            String title,
            String artistName,
            Integer releaseYear,
            String avgRating // Added field
    ) {
    }

    public record SongEntry(
            String songId,
            String title,
            String genre,
            String avgRating
        ) {
    }

    public record AlbumReview(
            String username,
            String songTitle,
            String comment,
            String rating,
            String firstName // Optional: for showing "Alex S." instead of just @username
    ) {
    }

    // ── Endpoints ─────────────────────────────────────────────────────────────

    /**
     * Get basic album metadata, artist name, and aggregate average rating.
     */
    @GetMapping("/{albumId}")
    public ResponseEntity<AlbumInfo> getAlbumInfo(@PathVariable String albumId) {
        String sql = """
                SELECT
                    al.albumId,
                    al.title,
                    ar.artist_name,
                    al.release_year,
                    AVG(CAST(r.rating AS DECIMAL(3,1))) AS avgRating
                FROM album al
                JOIN artist ar ON al.artistId = ar.artistId
                LEFT JOIN song s ON s.albumId = al.albumId
                LEFT JOIN review r ON r.songId = s.songId
                WHERE al.albumId = ?
                GROUP BY al.albumId, al.title, ar.artist_name, al.release_year
                """;

        List<AlbumInfo> results = jdbc.query(sql, (rs, rowNum) -> new AlbumInfo(
                rs.getString("albumId"),
                rs.getString("title"),
                rs.getString("artist_name"),
                rs.getInt("release_year"),
                rs.getString("avgRating") != null
                        ? String.format("%.1f", rs.getDouble("avgRating"))
                        : "—"),
                albumId);

        if (results.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(results.get(0));
    }

    /**
     * Get the list of songs belonging to a specific album.
     */
    @GetMapping("/{albumId}/songs")
    public ResponseEntity<List<SongEntry>> getAlbumSongs(@PathVariable String albumId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM album WHERE albumId = ?", Integer.class, albumId);
        if (count == null || count == 0) {
            return ResponseEntity.notFound().build();
        }

        String sql = """
                SELECT
                    s.songId,
                    s.title,
                    s.genre,
                    AVG(CAST(r.rating AS DECIMAL(3,1))) AS songAvg
                FROM song s
                LEFT JOIN review r ON s.songId = r.songId
                WHERE s.albumId = ?
                GROUP BY s.songId, s.title, s.genre
                ORDER BY s.title ASC
                """;

        List<SongEntry> songs = jdbc.query(sql, (rs, rowNum) -> new SongEntry(
                rs.getString("songId"),
                rs.getString("title"),
                rs.getString("genre"),
                rs.getString("songAvg") != null
                        ? String.format("%.1f", rs.getDouble("songAvg"))
                        : null),
                albumId);

        return ResponseEntity.ok(songs);
    }

    /**
     * Get the 10 most recent reviews for any song on this album.
     */
    @GetMapping("/{albumId}/reviews")
    public ResponseEntity<List<AlbumReview>> getAlbumReviews(@PathVariable String albumId) {
        String sql = """
                SELECT
                    u.username,
                    u.firstName,
                    s.title AS songTitle,
                    r.comment,
                    r.rating
                FROM review r
                JOIN song s ON r.songId = s.songId
                JOIN user u ON r.userId = u.userId
                WHERE s.albumId = ?
                ORDER BY r.songId DESC
                LIMIT 10
                """;

        List<AlbumReview> reviews = jdbc.query(sql, (rs, rowNum) -> new AlbumReview(
                rs.getString("username"),
                rs.getString("songTitle"),
                rs.getString("comment"),
                rs.getString("rating"),
                rs.getString("firstName")), albumId);

        return ResponseEntity.ok(reviews);
    }
}