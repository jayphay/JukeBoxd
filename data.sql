use jukeboxd_db;

insert into user values (1, "dsmith", "$2a$10$0bdF8iScIvp2qvvaCSnKlO5o7KoqfzTltCcYi8Jvb2LSfh/NMy0hq", "Dean", "Smith"); -- pw: dog
insert into user values (2, "tom98", "$2a$10$ZYzDuH7WLvgf6NOSh2zMBO6ZguIO91aLqzN6WmdkqLaC7CGzHaNvG", "Tom", "Howard"); -- pw: cat 
insert into user values (3, "pete", "$2a$10$klo6Ffk3EOfpOBfnJl1FDuU5e6uZX7x8RqHrTXxCXHFhzMMYAlqdm", "Peter", "Parker"); -- pw: fish

insert into review values (1, "36gcliMRX1vCpgnrZE3dFZ", "great song", "4");
insert into review values (1, "7aqfrAY2p9BUSiupwk3svU", "love the vibe", "5");
insert into review values (2, "5SHpuW2qjkQtFRpE6P9Nks", "not my jam", "2");
-- the next 2 reviews are for songs that are on the same album and that album is the one
-- that's at the top of the popular albums list
insert into review values (2, "0E0DRHf5PfMeor0ZCwB3oT", "me gusta", "4"); 
insert into review values (3, "1dm6z1fWB0cErMszU25dy2", "mas por favor", "5");




insert into listen_list values (1 , "2plbrEY59IikOBgBGLjaoe");
insert into listen_list values (1 , "3QaPy1KgI7nu9FJEQUgn6h");
