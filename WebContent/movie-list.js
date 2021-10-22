/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */
function getSelectedPagination(){
    let maxPgCount;
    // get user selected pagination
    jQuery("#pagination a"). on("click", function (){
        maxPgCount = $(this).text();
        $("#max-results").text(maxPgCount);
    })

    return maxPgCount;
}

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
function handleMovieResult(resultData, pagination) {
    console.log("handleMovieResult: populating star table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    let movie_dup = "";

    let count = 25; //only the top 20 movies

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
let searchTitle = getParameterByName('search_title');
let searchYear = getParameterByName('search_year');
let searchDirector = getParameterByName('search_director');
let searchStar = getParameterByName('search_star');

let pagination = getSelectedPagination();

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies?genre=" + movieGenre + '&' + 'char=' + searchChar + '&'+ "search_title=" + searchTitle +
    "&search_year=" + searchYear + "&search_director=" + searchDirector + "&search_star=" + searchStar, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleMovieResult(resultData, pagination) // Setting callback function to handle data returned successfully by the StarsServlet
});