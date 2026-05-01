create database if not exists jukeboxd_db;
use jukeboxd_db;

create table if not exists user (
    userId int auto_increment primary key,
    username varchar(255) not null unique,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null
);

create table if not exists song (
    songId varchar(255) primary key,
    artistId int not null,
    title varchar(255) not null,
    albumId varchar(255),
    genre varchar(255),
    foreign key (artistId) references artist(artistId) on delete cascade,
    foreign key (albumId) references album(albumId) on delete cascade
);

create table if not exists  album (
    albumId varchar(255) primary key,
    artistId int not null,
    title varchar(255),
    release_year int,
    foreign key (artistId) references artist(artistId) on delete cascade
);

create table if not exists artist (
    artistId int auto_increment primary key,
    artist_name varchar(255) not null
);

create table if not exists listen_list (
    userId int not null,
    songId varchar(255) not null,
    primary key (userId, songId),
    foreign key (userId) references user(userId) on delete cascade,
    foreign key (songId) references song(songId) on delete cascade
);

create table if not exists review (
    userId int not null,
    songId varchar(255) not null,
    comment text not null,
    rating ENUM('1', '2', '3', '4', '5'),
    primary key (userId, songId),
    foreign key (songId) references song(songId) on delete cascade,
    foreign key (userId) references user(userId) on delete cascade
);