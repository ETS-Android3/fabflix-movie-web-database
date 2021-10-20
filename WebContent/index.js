/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleMovieResult(resultData) {
    console.log("handleMovieResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    let starGenreTableBody = jQuery("#stars_genres_body")

    let movie_dup = "";

    let count = 20; //only the top 20 movies

    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        if (count > 0)
        {
            rowHTML += "<tr>";

            if (movie_dup.localeCompare(resultData[i]["movie_title"])) //not equal
            {
                rowHTML +=
                    "<th>" +
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
                    + resultData[i]["movie_title"] +     // display star_name for the link text
                    '</a>' +
                    "</th>";

                movie_dup = resultData[i]["movie_title"];

                rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
                rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
                rowHTML += "<th>" + resultData[i]["movie_ratings"] + "</th>";
                rowHTML += "<th>" + resultData[i]["movie_genres"] + "</th>";
                rowHTML +=
                    "<th>" +
                    // Add a link to single-movie.html with id passed with GET url parameter
                    '<a href="single-star.html?id=' + resultData[i]['star_id'] + '">'
                    + resultData[i]["movie_stars"] +
                    '</a>' +
                    "</th>";

                count--;

                rowHTML += "</tr>";
                movieTableBodyElement.append(rowHTML);
            }

        }

        else
        {
            break;
        }
    }






}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/stars", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});