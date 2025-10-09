
function isSetString(value) {
    return typeof value == "string" && value.length > 0 && value != "undefined";
}

function isSetUrl(value) {
    return isSetString(value) && value.startsWith("http");
}

/*
 * Call the shutdown URL on the test server directly.
 */
if(isSetUrl(TESTCONTROLLER_URL)) {
    const stopUrl = output.SCHOOL_URL + "shutdown";

    const testControllerResponse = http.get(stopUrl);
    console.log("teardown.js: status="  + testControllerResponse.status + " (" + stopUrl + ")");
}