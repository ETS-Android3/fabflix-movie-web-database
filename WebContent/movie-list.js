/**
 * This example is following frontend and backend separation.
 *
 * Before this .js is loaded, the html skeleton is created.
 *
 * This .js performs two steps:
 *      1. Use jQuery to talk to backend API to get the json data.
 *      2. Populate the data to correct html elements.
 */
 var lastPage = false;

 function getSelectedItemCount(){
 
     jQuery("#item-count a").on("click", function (){
         maxPgCount = $(this).text();
         console.log("selected:" + maxPgCount);
         $("#max-results").text(maxPgCount);
     })
 
     let reload= new URL(window.location);
     reload.searchParams.set("mvct", maxPgCount);
     console.log(reload);
     window.location.assign(reload);
 }
 
 function getSelectedSort(){
     jQuery("#sort-first a").on("click", function (){
         sortBy = $(this).text();
         $("#sort-btn").text(sortBy);
     })
 
     let reload= new URL(window.location);
     reload.searchParams.set("sort", sortBy);
     console.log(reload);
     window.location.assign(reload);
 }
 
 function getTitleSort(){
     jQuery("#sort-title a").on("click", function (){
         sortTitle = $(this).text();
         $("#title-btn").text(sortTitle);
     })
     let reload= new URL(window.location);
     reload.searchParams.set("title_order", sortTitle);
     console.log(reload);
     window.location.assign(reload);
 }
 
 function getRatingSort(){
     jQuery("#sort-rating a").on("click", function (){
         sortRating = $(this).text();
         $("#rating-btn").text(sortRating);
     })
     let reload= new URL(window.location);
     reload.searchParams.set("rating_order", sortRating);
     console.log(reload);
     window.location.assign(reload);
 }
 
 function incrementPage(){
 
     page = parseInt($("#page-num").text());
     console.log(lastPage, page);
     if(!lastPage){
         console.log("incrementing: ",page);
         page++; // increment page
         jQuery("#page-num").text(page);
         let reload= new URL(window.location);
         reload.searchParams.set("page", page.toString());
         window.location.assign(reload);
     }
 
 }
 function decrementPage(){
     page = parseInt($("#page-num").text());
     console.log("decrementing: ",page);
     if (page > 1){
         page--; // decrement page
         jQuery("#page-num").text(page);
         let reload= new URL(window.location);
         reload.searchParams.set("page", page.toString());
         window.location.assign(reload);
     }
 
 }
 
 // TO-DO : fix cart handling
 function handleCartInfo(movieTitle){
 
     // $.ajax("api/shopping-cart", {
     //     dataType: "json",
     //     method: "POST",
     //     data: { movieTitle: movieTitle},
     //     success: resultDataString => { alert(`Added to cart.`); },
     //     error: resultDataString => { alert(`Could not add to cart.`); }
     // });
 
     console.log("shop-cart.html?cart_movie=" + movieTitle);
     location.href = "shop-cart.html?cart_movie=" + movieTitle;
 
     // reload.searchParams.set("cart_movie", movieTitle);
     // console.log(reload);
     // window.location.assign(reload);
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
 function handleMovieResult(resultData) {
     console.log("handleMovieResult: populating star table from resultData");
 
     // testing purposes
     console.log(window.location.href);
     console.log(resultData);
 
     lastPage = false;
 
     // Populate the star table
     // Find the empty table body by id "movie_table_body"
     let movieTableBodyElement = jQuery("#movie_table_body");
 
     // let movie_dup = "";
 
     let count = resultData[0]["count"]; //default
     if(resultData.length < count){
         lastPage = true;
     }
 
     for (let i = 0; i < Math.min(count, resultData.length); i++) {
         let rowHTML = "";
 
         if (count > 0)
         {
             rowHTML += "<tr>";
 
             rowHTML += "<th>" + '<button onclick="handleCartInfo(\'' + resultData[i]['movie_title'] + '\')">' + "Add to Cart" +  // display star_name for the link text
             '</button>' + "</th>";
             // rowHTML += "<th>" + '<button href="shop-cart.html?cart_movie="' + resultData[i]['movie_title'] + '">Add to Cart' +  // display star_name for the link text
             // '</button>' + "</th>";
             rowHTML +=
                 "<th>" +
                 // Add a link to single-movie.html with id passed with GET url parameter
                 '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
                 + resultData[i]["movie_title"] +     // display star_name for the link text
                 '</a>' +
                 "</th>";
 
             // movie_dup = resultData[i]["movie_title"];
 
             rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
             rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
             rowHTML += "<th>" + resultData[i]["movie_ratings"] + "</th>";
             let genres = [];
             for (let j = 0; j < Math.min(3,resultData[i]['movie_genres'].length); j++){
                 genres.push('<a href="movie-list.html?genre=' + resultData[i]['movie_genres'][j]["name"] + '">'
                     + resultData[i]["movie_genres"][j]["name"] + '</a>');
             }
             rowHTML += "<th>" + genres.join(", ") + "</th>";
             let stars = [];
             for (let j = 0; j < Math.min(3,resultData[i]['movie_stars'].length); j++){
                 // Add a link to single-star.html with id passed with GET url parameter
                 stars.push('<a href="single-star.html?id=' + resultData[i]['movie_stars'][j]['star_id'] + '">'
                     + resultData[i]["movie_stars"][j]['star'] +
                     '</a>')
             }
             rowHTML += "<th>" + stars.join(", ") + "</th>";
 
             rowHTML += "</tr>";
             movieTableBodyElement.append(rowHTML)
 
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
 
 // let mvListURL = buildURLQuery();
 // console.log(mvListURL);
 
 let movieGenre = getParameterByName('genre');
 let searchChar = getParameterByName('char');
 let searchTitle = getParameterByName('search_title');
 let searchYear = getParameterByName('search_year');
 let searchDirector = getParameterByName('search_director');
 let searchStar = getParameterByName('search_star');
 let mvCount = getParameterByName('mvct') || 10;
 let page = getParameterByName('page') || 1;
 let fulltxt = getParameterByName('fulltxt');
 
 let sort = getParameterByName('sort') || 'Default';
 let tOrder = getParameterByName('title_order') || 'Default';
 let rOrder = getParameterByName('rating_order') || 'Default';
 
 let maxPgCount;
 let sortBy;
 let sortTitle;
 let sortRating;
 
 // get user selected item-count
 jQuery("#item-count a").on("click", function (){
     maxPgCount = $(this).text();
 })
 jQuery("#max-results").text(mvCount);
 
 
 // // set user selected sort
 jQuery("#sort-first a").on("click", function (){
     sortBy = $(this).text();
 })
 $('#sort-btn').text(sort);
 
 
 //get user selected title sort
 jQuery("#sort-title a").on("click", function (){
     sortTitle = $(this).text();
 })
 jQuery("#title-btn").text(tOrder);
 
 // get user selceted rating sort
 jQuery("#sort-rating a").on("click", function (){
     sortRating = $(this).text();
 })
 jQuery("#rating-btn").text(rOrder);
 
 // // update page number
 jQuery("#prev-btn").on("click", function(){
     $(this).text();
 });
 
 jQuery("#page-num").text(page);
 
 if(tOrder == 'Default' || tOrder == "A ➜ Z"){
     tOrder = "asc";
 }
 else if(tOrder == "Z ➜ A"){
     tOrder = "desc";
 }
 if(rOrder == 'Default' || rOrder == "Low to High"){
     rOrder = "asc";
 }
 else if(rOrder == "High to Low"){
     rOrder = "desc";
 }
 
 let query = "genre=" + movieGenre + "&";
 query += "char=" + searchChar + "&";
 query += "search_title=" + searchTitle + "&";
 query += "search_year=" + searchYear + "&";
 query += "search_director=" + searchDirector + "&";
 query += "search_star=" + searchStar + "&";
 query += "mvct=" + mvCount + "&";
 query += "page=" + page + "&"; // starting item on page
 query += "sort=" + sort + "&";
 query += "title_order=" + tOrder + "&";
 query += "rating_order=" + rOrder + "&";
 query += "fulltxt=" + fulltxt;
 
 // Makes the HTTP GET request and registers on success callback function handleStarResult
 
 jQuery.ajax({
     dataType: "json", // Setting return data type
     method: "GET", // Setting request method
     url: "api/movies?" + query, // Setting request url, which is mapped by StarsServlet in Stars.java
     success: (resultData) => handleMovieResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
 }); 