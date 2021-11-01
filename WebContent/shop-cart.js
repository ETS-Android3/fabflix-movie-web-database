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

function handleShoppingCart(resultData){

    console.log("handleShoppingCart: adding movie to cart");

    let checkoutTable = jQuery("#checkout_table_body");

};

// jQuery.ajax("api/movies", {
//     dataType: "json",
//     method: "POST",
//     data: { movieId: movieId},
//     success: resultDataString => { alert(`Added to cart.`); },
//     error: resultDataString => { alert(`Could not add to cart.`); }

let movieTitle = getParameterByName('cart_movie');
console.log(movieTitle);

// $.ajax({
//     dataType: "json",
//     method: "POST",
//     url: "api/shopping-cart?" + "cart_movie=" + movieTitle,
//     success: (resultData) => handleShoppingCart(resultData)
// })