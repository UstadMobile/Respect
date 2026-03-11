/*
 * Validates the Zammad environment variables (URL and Token) and verifies that the
 * support ticket (Case ID) was successfully created on the server via the API.
 */

function isSetString(value) {
    return typeof value === "string" && value.length > 0 && value !== "undefined";
}

function isSetUrl(value) {
    return isSetString(value) && value.startsWith("http");
}

// Validate Zammad URL

var baseUrl;
if (typeof zammadUrl !== 'undefined' && isSetUrl(zammadUrl)) {
    baseUrl = zammadUrl;
} else if (isSetUrl(output.zammadUrl)) {
    baseUrl = output.zammadUrl;
} else {
    throw "zammadUrl not set or invalid.";
}

// Validate Token

var token;
if (typeof zammadToken !== 'undefined' && isSetString(zammadToken)) {
    token = zammadToken;
} else if (isSetString(output.zammadToken)) {
    token = output.zammadToken;
} else {
    throw "zammadToken not set.";
}

var caseNumber = CASE_ID;

console.log("baseUrl:", baseUrl);
console.log("caseNumber:", caseNumber);


// Retry logic
const maxAttempts = 4;

for (var i = 0; i < maxAttempts; i++) {
    try {
        const requestUrl =
            baseUrl.replace(/\/$/, "") + "/" + caseNumber;

        const response = http.get(requestUrl, {
            headers: {
                Authorization: "Token token=" + token
            }
        });

        if (!response.body || response.body.length === 0) {
            throw "Ticket body empty";
        }

        console.log("Ticket verified successfully");
        break;

    } catch (err) {
        if (i === maxAttempts - 1) {
            throw "Failed after " + maxAttempts + " attempts. Error: " + err;
        }
    }
}

