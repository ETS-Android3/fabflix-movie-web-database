/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}


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
let movieGenre = getParameterByName('genre');
let searchChar = getParameterByName('char');

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?genre=" + movieGenre + '&' + 'char=' + searchChar, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});