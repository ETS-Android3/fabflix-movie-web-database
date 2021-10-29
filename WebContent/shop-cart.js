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

let movieId = getParameterByName('id');

$.ajax({
    dataType: "json",
    method: "POST",
    url: "api/shopping-cart?" + "movieId=" + movieId,
    success: (resultData) => handleShoppingCart(resultData)
})