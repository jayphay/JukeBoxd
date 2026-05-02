CREATE TABLE IF NOT EXISTS artist (
    artistId INT AUTO_INCREMENT PRIMARY KEY,
    artist_name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS album (
    albumId VARCHAR(255) PRIMARY KEY,
    artistId INT NOT NULL,
    title VARCHAR(255),
    release_year INT,
    FOREIGN KEY (artistId) REFERENCES artist(artistId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS song (
    songId VARCHAR(255) PRIMARY KEY,
    artistId INT NOT NULL,
    albumId VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(255),
    FOREIGN KEY (artistId) REFERENCES artist(artistId) ON DELETE CASCADE,
    FOREIGN KEY (albumId) REFERENCES album(albumId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user (
    userId INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    firstName VARCHAR(255) NOT NULL,
    lastName VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS listen_list (
    userId INT NOT NULL,
    songId VARCHAR(255) NOT NULL,
    PRIMARY KEY (userId, songId),
    FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE,
    FOREIGN KEY (songId) REFERENCES song(songId) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS review (
    userId INT NOT NULL,
    songId VARCHAR(255) NOT NULL,
    comment TEXT NOT NULL,
    rating ENUM('1', '2', '3', '4', '5'),
    PRIMARY KEY (userId, songId),
    FOREIGN KEY (songId) REFERENCES song(songId) ON DELETE CASCADE,
    FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE
);

