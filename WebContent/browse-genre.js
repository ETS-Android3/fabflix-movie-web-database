function handleGenreList(resultData){

    console.log("handleGenreList: adding genres to list from resultData");
    let genreBody = jQuery("#genre_list")

    for (let i = 0; i < resultData.length; i++){
        let ulHTML = "";
        ulHTML += "<li>" +
        '<a href="movie-list.html?genre=' + resultData[i]['genre'] + '">'
        + resultData[i]["genre"] +
        '</a>' +"</li>";

        genreBody.append(ulHTML);
    }
}
// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/genre", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleGenreList(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});