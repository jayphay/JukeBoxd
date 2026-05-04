-- loads songs with artits's name
-- /create-review
SELECT s.songId, s.title, ar.artist_name AS artistName
    FROM song s
    JOIN artist ar ON ar.artistId = s.artistId
    ORDER BY s.title ASC

-- creates a new review
-- /create-review
INSERT INTO review (userId, songId, comment, rating)
    VALUES (?, ?, ?, ?)
    ON DUPLICATE KEY UPDATE comment = VALUES(comment), rating = VALUES(rating)

-- gets the albums with the most songs
-- /api/home/popular-albums
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

-- gets songs in alphabetical order
-- /api/home/popular-singles
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

-- gets how many songs are on a user's listen list
-- /api/home/listen-list/toggle
SELECT COUNT(*) FROM listen_list WHERE userId = ? AND songId = ?

-- removes a song from a user's listen list
-- /api/home/listen-list/toggle
DELETE FROM listen_list WHERE userId = ? AND songId = ?

-- adds a song to a user's listen list
-- /api/home/listen-list/toggle
INSERT INTO listen_list (userId, songId) VALUES (?, ?)

-- gets artists who have the most songs and albums
-- /api/home/popular-artists
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

-- gets the reviews that have been posted, sorted by userIds
-- /api/home/recent-reviews
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

-- gets all the information that is used for the profile 
-- /api/profile/{username}
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

-- gets reviews written by the given user
-- /api/profile/{username}/reviews
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

-- updates an already written review
-- /api/profile/reviews/{songId}
UPDATE review SET comment = ?, rating = ? WHERE userId = ? AND songId = ?

-- gets the genres so users can search for songs by genre
-- / : it's at the root
SELECT DISTINCT genre FROM song WHERE genre IS NOT NULL

-- searches for a song and checks if a song is in the user's listen list
-- /search
SELECT s.songId, s.title, ar.artist_name, COALESCE(a.title, '') as albumTitle, s.genre,
               (CASE WHEN ll.songId IS NOT NULL THEN 1 ELSE 0 END) as isSaved
FROM song s 
JOIN artist ar ON ar.artistId = s.artistId 
LEFT JOIN album a ON a.albumId = s.albumId 
LEFT JOIN listen_list ll ON ll.songId = s.songId AND ll.userId = ?
WHERE 1=1 

-- searches for a specific user
-- /members
SELECT userId, username, firstName, lastName 
FROM user 
WHERE username LIKE ? OR firstName LIKE ? OR lastName LIKE ?
LIMIT 20

-- gets the songs on a user's listen list
-- /listenlist
SELECT s.songId, s.title, ar.artist_name 
FROM listen_list ll
JOIN song s ON ll.songId = s.songId
JOIN artist ar ON s.artistId = ar.artistId
WHERE ll.userId = ?

-- gets the details of an album, including the average rating of the songs on the album
-- /album/{albumId}
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

-- gets the songs on an album, including the average rating of each song
-- /album/{albumId}
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

-- gets the 10 most recent reviews for songs on an album, including the username of the reviewer
-- /album/{albumId}
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


-- gets the details of a song, including the average rating of the song
-- /song/{songId}
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

-- gets the 5 most recent reviews for a song, including the username of the reviewer
-- /song/{songId}
SELECT u.username, r.comment, r.rating
FROM review r
JOIN user u ON r.userId = u.userId
WHERE r.songId = ?
ORDER BY r.userId DESC -- Placeholder for recency
LIMIT 5