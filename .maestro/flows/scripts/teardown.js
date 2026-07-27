
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
    const stopUrl = output.SCHOOL_URL + "api/shutdown";

    const testControllerResponse = http.get(
        TESTCONTROLLER_URL + "testcontroller/stop?port=" + output.SCHOOL_PORT
    );
    console.log("test server controller stop response: " + testControllerResponse.status);
}