
if(typeof SCHOOL_ADMIN_PASSWORD != "string" || SCHOOL_ADMIN_PASSWORD.length < 1 ||
        SCHOOL_ADMIN_PASSWORD == "undefined"
) {
    throw "SCHOOL_ADMIN_PASSWORD not set. e.g. maestro test -e SCHOOL_ADMIN_PASSWORD=adminpassword . See README";
}

/*
 * If TESTCONTROLLER_URL is set, then use test controller to add a new school.
 */
if(typeof TESTCONTROLLER_URL == "string" && TESTCONTROLLER_URL.startsWith("http")) {
    console.log("Test controller= " + TESTCONTROLLER_URL);

    const testControllerResponse = http.get(
        TESTCONTROLLER_URL + "testcontroller/start?waitForUrl=" + encodeURIComponent("api/directory/school")
    );

    console.log("Response body = " + testControllerResponse.body);


    const serverUrl = json(testControllerResponse.body).url;

    console.log("RESPECT server started on url " + serverUrl);
    console.log("RESPECT dir admin Auth: " + DIR_ADMIN_AUTH_HEADER);

    const timeNow = new Date().toISOString();

    const newSchoolResponse = http.post(serverUrl + "api/directory/school", {
        headers: {
            'Content-Type': 'application/json',
            "Authorization": DIR_ADMIN_AUTH_HEADER
        },
        body: JSON.stringify(
            [
                {
                    "school": {
                        "name": "Test School",
                        "self": serverUrl,
                        "xapi": (schoolUrl + "api/school/xapi"),
                        "oneRoster": (schoolUrl + "api/school/oneroster"),
                        "respectExt": (schoolUrl + "api/school/respect"),
                        "rpId": null,
                        "lastModified": timeNow,
                        "stored": timeNow
                    },
                    "dbUrl": "school.db",
                    "adminUsername": "admin",
                    "adminPassword": SCHOOL_ADMIN_PASSWORD
                }
            ]
        )
    });
    console.log("New school server URL requested: status = " + newSchoolResponse.status);

    output.SCHOOL_URL = serverUrl;
}else if(typeof SCHOOL_URL == "string" && SCHOOL_URL.startsWith("http")) {
    output.SCHOOL_URL = SCHOOL_URL;
}else {
    throw "SCHOOL_URL not set AND TESTCONTROLLER_URL not set. See README";
}



