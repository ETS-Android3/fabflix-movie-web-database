let new_star_form = $("#new_star_form");

function handleAddStar(resultDataString){
    console.log("added star in backend");
    let resultDataJson = JSON.parse(resultDataString);
    console.log(resultDataJson);

    let rs_msg = document.getElementById('add_star_msg');
    rs_msg.innerHTML = resultDataJson["message"];
}

function submitNewStar(formSubmitEvent){
    console.log("submit new star");
    $.ajax(
        "api/add-star",{
            method: "GET",
            data: new_star_form.serialize(),
            success: (resultData) => handleAddStar(resultData)
        }
    );
}

new_star_form.submit(submitNewStar);