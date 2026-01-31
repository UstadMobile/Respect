

function isSetString(value) {
    return typeof value == "string" && value.length > 0 && value != "undefined";
}

function isSetUrl(value) {
    return isSetString(value) && value.startsWith("http");
}

// --- 1. Validate Zammad URL ---
// We check if ZAMMAD_URL (passed from YAML env) is valid
var baseUrl;
if (typeof ZAMMAD_URL !== 'undefined' && isSetUrl(ZAMMAD_URL)) {
    baseUrl = ZAMMAD_URL;
} else if (isSetUrl(output.zammadUrl)) {
    // Fallback: check if it exists in output object
    baseUrl = output.zammadUrl;
} else {
    throw "ZAMMAD_URL not set or invalid. Run with -e zammadUrl=https://... or check YAML env mapping.";
}

// --- 2. Validate Token ---
var token;
if (typeof ZAMMAD_TOKEN !== 'undefined' && isSetString(ZAMMAD_TOKEN)) {
    token = ZAMMAD_TOKEN;
} else if (isSetString(output.zammadToken)) {
    token = output.zammadToken;
} else {
    throw "ZAMMAD_TOKEN not set. Run with -e zammadToken=... or check YAML env mapping.";
}

var caseNumber = CASE_ID;

console.log("baseUrl:", baseUrl);
console.log("caseNumber:", caseNumber);

const maxAttempts = 4;

for (var i = 0; i < maxAttempts; i++) {
  try {

    const requestUrl = baseUrl + "/" + caseNumber;

    const response = http.get(
      requestUrl,
      {
        headers: {
          Authorization: "Token token=" + token
        }
      }
    );

    if (!response.body || response.body.length === 0) {
      throw "Ticket body empty";
    }
    //console.log(response.body);
    console.log("Ticket verified successfully");
    break;

  } catch (err) {
    console.log("Attempt " + (i + 1) + " failed: " + err);

    if (i === maxAttempts - 1) {
        throw new Error("Failed to verify ticket after " + maxAttempts + " attempts. Last error: " + err);
    }

  }
}