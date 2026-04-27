import csv
import mysql.connector
from datetime import datetime

# --- Docker MySQL connection settings ---
DB_HOST = "localhost"
DB_PORT = 33306
DB_USER = "root"
DB_PASSWORD = "mysqlpass"       # change to match your docker-compose password
DB_NAME = "jukeboxd_db"
CSV_FILE = "high_popularity_spotify_data.csv"

DDL = """
CREATE DATABASE IF NOT EXISTS jukeboxd_db;
USE jukeboxd_db;

CREATE TABLE IF NOT EXISTS artist (
    artistId   INT AUTO_INCREMENT PRIMARY KEY,
    artist_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS album (
    albumId      VARCHAR(255) PRIMARY KEY,
    artistId     INT NOT NULL,
    title        VARCHAR(255),
    genre        VARCHAR(255),
    release_year INT,
    FOREIGN KEY (artistId) REFERENCES artist(artistId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS song (
    songId   VARCHAR(255) PRIMARY KEY,
    artistId INT NOT NULL,
    albumId  VARCHAR(255),
    title    VARCHAR(255) NOT NULL,
    genre    VARCHAR(255),
    FOREIGN KEY (artistId) REFERENCES artist(artistId) ON DELETE CASCADE,
    FOREIGN KEY (albumId)  REFERENCES album(albumId)   ON DELETE CASCADE
);
"""

def get_year(date_str):
    for fmt in ("%Y-%m-%d", "%Y-%m", "%Y"):
        try:
            return datetime.strptime(date_str.strip(), fmt).year
        except ValueError:
            continue
    return None


def run():
    # Connect without a database first so we can CREATE DATABASE
    conn = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASSWORD
    )
    cursor = conn.cursor()

    for statement in DDL.strip().split(";"):
        stmt = statement.strip()
        if stmt:
            cursor.execute(stmt)
    conn.commit()

    # Reconnect with the database selected
    cursor.close()
    conn.close()
    conn = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER,
        password=DB_PASSWORD, database=DB_NAME
    )
    cursor = conn.cursor()

    artists_seen = {}   # artist_name -> artistId
    albums_seen  = set()
    songs_seen   = set()

    with open(CSV_FILE, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for row in reader:
            artist_name  = row["track_artist"].strip()
            album_id     = row["track_album_id"].strip()
            album_title  = row["track_album_name"].strip()
            genre        = row["playlist_genre"].strip()
            release_date = row["track_album_release_date"].strip()
            song_id      = row["track_id"].strip()
            song_title   = row["track_name"].strip()

            # --- artist ---
            if artist_name not in artists_seen:
                cursor.execute(
                    "INSERT IGNORE INTO artist (artist_name) VALUES (%s)",
                    (artist_name,)
                )
                conn.commit()
                cursor.execute(
                    "SELECT artistId FROM artist WHERE artist_name = %s",
                    (artist_name,)
                )
                artists_seen[artist_name] = cursor.fetchone()[0]
            artist_id = artists_seen[artist_name]

            # --- album ---
            if album_id not in albums_seen:
                cursor.execute(
                    """INSERT IGNORE INTO album (albumId, artistId, title, genre, release_year)
                       VALUES (%s, %s, %s, %s, %s)""",
                    (album_id, artist_id, album_title, genre, get_year(release_date))
                )
                albums_seen.add(album_id)

            # --- song ---
            if song_id not in songs_seen:
                cursor.execute(
                    """INSERT IGNORE INTO song (songId, artistId, albumId, title, genre)
                       VALUES (%s, %s, %s, %s, %s)""",
                    (song_id, artist_id, album_id, song_title, genre)
                )
                songs_seen.add(song_id)

    conn.commit()
    cursor.close()
    conn.close()

    print(f"Done. Inserted {len(artists_seen)} artists, "
          f"{len(albums_seen)} albums, {len(songs_seen)} songs.")


if __name__ == "__main__":
    run()
