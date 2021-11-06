USE moviedb;

DROP PROCEDURE IF EXISTS add_movie;

DELIMITER $$

CREATE PROCEDURE add_movie (IN title varchar(100), IN year int, IN director varchar(100), IN singleStar varchar(100), IN singleGenre varchar(32), OUT msg VARCHAR(1000))

BEGIN

DECLARE movie_exists INT;
DECLARE genre_exists INT;
DECLARE star_exists INT;

DECLARE movie_id VARCHAR(10);
DECLARE star_id VARCHAR(10);
DECLARE genre_id INT;

SET movie_exists = (SELECT count(*) FROM movies WHERE movies.title = title and movies.year = year and movies.director = director);
SET genre_exists= (SELECT count(*) FROM genres WHERE genres.name = singleGenre);
SET star_exists = (SELECT count(*) FROM stars WHERE stars.name = singleStar);

IF (movie_exists = 0) THEN
	SET movie_id = CONCAT('tt', (CONVERT(SUBSTRING((SELECT max(id) from movies), 3), UNSIGNED)) + 1);
    INSERT INTO movies VALUES(movie_id, title, year, director);
    SET msg = 'movie ';
	SET msg = CONCAT(msg, movie_id);
    SET msg = CONCAT(msg, ' added.');
    
    IF (star_exists = 0) THEN
		SET star_id = CONCAT('nm', (CONVERT(SUBSTRING((SELECT max(id) from stars), 3), UNSIGNED)) + 1);
        INSERT INTO stars(id, name) VALUES(star_id, singleStar);
		SET msg = 'star ';
		SET msg = CONCAT(msg, star_id);
		SET msg = CONCAT(msg, ' added.');
	ELSE 
		SET star_id = (SELECT id from STARS WHERE name = singleStar LIMIT 1);
		SET msg = 'star ';
		SET msg = CONCAT(msg, movie_id);
		SET msg = CONCAT(msg, ' exists.');
	END IF;
    
    INSERT INTO stars_in_movies VALUES(star_id, movie_id);
	SET msg = "(stars_in_movies) star: ";
	SET msg = CONCAT(msg, star_id);
    SET msg = CONCAT(msg, ' movie');
    SET msg = CONCAT(msg, movie_id);
    SET msg = CONCAT(msg, ' added');

	IF (genre_exists = 0) THEN
		INSERT INTO genres(name) VALUES(singleGenre);
        SET genre_id = (SELECT max(id) FROM genres);
        SET msg = "genre ";
        SET msg = CONCAT(result, genre_id);
        SET msg = CONCAT(result, " added.");
	ELSE
		SET genre_id = (SELECT id FROM genres WHERE name = singleGenre);
	END IF;
	INSERT INTO genres_in_movies VALUES(genre_id, movie_id);
	SET msg = "(genres_in_movies) genre: ";
	SET msg = CONCAT(msg, genre_id);
    SET msg = CONCAT(msg, ' movie');
    SET msg = CONCAT(msg, movie_id);
    SET msg = CONCAT(msg, ' added');

    
    ELSE
    SET msg = "Movie already exists. Try again";
    END IF;
    
END
$$

DELIMITER ;