package uga.group18.app.api;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    private final JdbcTemplate jdbc;

    public HomeController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record AlbumItem(String albumId, String title, String artistName, String genre, Integer releaseYear, Integer songsCount) {}
    public record SongItem(String songId, String title, String artistName, String albumTitle, String genre) {}
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
                  a.genre,
                  a.release_year AS releaseYear,
                  COUNT(s.songId) AS songsCount
                FROM album a
                JOIN artist ar ON ar.artistId = a.artistId
                LEFT JOIN song s ON s.albumId = a.albumId
                GROUP BY a.albumId, a.title, ar.artist_name, a.genre, a.release_year
                ORDER BY songsCount DESC, a.release_year DESC, a.title ASC
                LIMIT 10
                """,
                (rs, rowNum) -> new AlbumItem(
                        rs.getString("albumId"),
                        rs.getString("title"),
                        rs.getString("artistName"),
                        rs.getString("genre"),
                        (Integer) rs.getObject("releaseYear"),
                        rs.getInt("songsCount")
                )
        );
    }

    @GetMapping("/popular-singles")
    public List<SongItem> popularSingles() {
        return jdbc.query(
                """
                SELECT
                  s.songId,
                  s.title,
                  ar.artist_name AS artistName,
                  COALESCE(a.title, '') AS albumTitle,
                  s.genre
                FROM song s
                JOIN artist ar ON ar.artistId = s.artistId
                LEFT JOIN album a ON a.albumId = s.albumId
                ORDER BY s.title ASC
                LIMIT 10
                """,
                (rs, rowNum) -> new SongItem(
                        rs.getString("songId"),
                        rs.getString("title"),
                        rs.getString("artistName"),
                        rs.getString("albumTitle"),
                        rs.getString("genre")
                )
        );
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

