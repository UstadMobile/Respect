//Downloaded as per https://github.com/xapijs/xapi/blob/develop/example/browser/module/index.html
import XAPI from "./xapi_module.js";
const { endpoint, auth } = XAPI.getTinCanLaunchData();

//As per https://www.xapijs.dev/xapi-wrapper-library/helpers
const queryParamsObject = XAPI.getSearchQueryParamsAsObject(location.search);

const searchParams = new URLSearchParams(location.search);

const xapi = new XAPI({
    endpoint: endpoint,
    auth: auth
});

function onClickSendStatement() {
    const myStatement = {
        actor: queryParamsObject.actor,
        verb: {
            id: document.getElementById("verb_id").value,
        },
        object: {
            id: searchParams.get("activity_id")
        },
        result: {
            completion: true,
            success: true,
            score: {
                scaled: parseFloat(document.getElementById("score_text").value)
            }
        }
    };

    console.log("XapiAuJs: Assignable unit: sending statement ");

    document.getElementById("result").innerText = "";
    document.getElementById("error").innerText = "";

    xapi.sendStatement({
        statement: myStatement
    }).then((result) => {
        console.log("XapiAuJs: Assignable unit: got result.");
        if(Array.isArray(result.data)) {
            document.getElementById("result").innerText = "Statement sent: " + result.data.join();
        }else {
            document.getElementById("result").innerText = "Statement sent: " + result.data.toString();
        }

        console.log(result);
    }).catch((error) => {
        const errorStr = JSON.stringify(error.toJSON(), null, 2);
        console.log(errorStr);
        document.getElementById("error").innerText = errorStr;
    });
}


addEventListener("DOMContentLoaded", (event) => {
    document.getElementById("statement_button").addEventListener('click', onClickSendStatement);
    document.getElementById("actor_info").innerText = JSON.stringify(queryParamsObject.actor);
    document.getElementById("activity_id").innerText = searchParams.get("activity_id");
});

console.log("XapiAuJs: loaded");
