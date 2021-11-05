let new_star_form = $("#new_star_form");

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

function handleAddStar(resultDataString){
    console.log("added star in backend");
    let resultDataJson = JSON.parse(resultDataString);
    console.log(resultDataJson);

    console.log(resultDataJson["message"]);

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

function handleMetadata(resultData){
    console.log("getting metadata");
    console.log(resultData);

    let table_body = document.getElementById("table_names");

    let column_body = document.getElementById("column_names");
    
    column_body.innerHTML = "";

    table_body.innerHTML = "";

    table_body.innerHTML += "<br>";

    let tables = "";
    for (let i = 0; i < resultData.length; i++){
        tables += '<a href="_dashboard.html?type=';
        tables += resultData[i];
        tables += '">';
        tables += resultData[i];
        tables += '</a>';
        tables += "<br>";  
    }
    table_body.innerHTML = tables;
}

function handleMetadataType(resultData){
    console.log(resultData);

    let table_body = document.getElementById("column_names");
    
    table_body.innerHTML = "";

    table_body.innerHTML += "<h2>";
    table_body.innerHTML += resultData[0];
    table_body.innerHTML += "</h2>";
    
    let tables = "";
    for (let i = 1; i < resultData.length; i++){
        tables += resultData[i];
        tables += "<br>";
    }

    table_body.innerHTML = tables;
    
}

function submitMetadata(formSubmitEvent){
    console.log("request metadata info");
    $.ajax(
        "metadata",{
            method: "GET",
            success: (resultData) => handleMetadata(resultData)
        }
    );
}

let type = getParameterByName("type") || null;

if (type != null){
    console.log("getting column names");
    $.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "metadata?type=" + type, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMetadataType(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    }); 
}


new_star_form.submit(submitNewStar);
document.getElementById("metadata-btn").addEventListener('click', function(){
    submitMetadata()});