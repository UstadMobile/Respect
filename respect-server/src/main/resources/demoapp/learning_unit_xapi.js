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

function sendAndSetResultText(statementObj, resultDomId) {
    const logPrefix = "lesson_xapi: #" + resultDomId;

    document.getElementById(resultDomId).innerText = "";
    console.log(logPrefix + " sending statement");
    xapi.sendStatement({
        statement: statementObj
    }).then((result) => {
         console.log(logPrefix + " statement sent successfully.");
         if(Array.isArray(result.data)) {
             document.getElementById(resultDomId).innerText = "Statement sent: " + result.data.join();
         }else {
             document.getElementById(resultDomId).innerText = "Statement sent: " + result.data.toString();
         }

         console.log(result);
    }).catch((error) => {
         const errorStr = JSON.stringify(error.toJSON(), null, 2);
         console.error("ERROR: " + errorStr);
         document.getElementById(resultDomId).innerText = errorStr;
    });
}


function onClickSendResult() {
    const verbId = document.getElementById("verb_id").value;
    const statementObj = {
        actor: queryParamsObject.actor,
        verb: {
            id: verbId
        },
        object: {
            id: searchParams.get("activity_id")
        },
        result: {
            completion: true,
            success: verbId == "http://adlnet.gov/expapi/verbs/passed",
            score: {
                scaled: parseFloat(document.getElementById("score_text").value)
            }
        }
    };

    sendAndSetResultText(statementObj, "send_result_result");
}

function onClickSendCompleted() {
    const statementObj = {
        actor: queryParamsObject.actor,
        verb: {
            id: "http://adlnet.gov/expapi/verbs/completed"
        },
        object: {
            id: searchParams.get("activity_id")
        },
        result: {
            completion: true,
        }
    };

    sendAndSetResultText(statementObj, "send_completed_result");
}

function onClickSendProgressed() {
    const statementObj = {
        actor: queryParamsObject.actor,
        verb: {
            id: "http://adlnet.gov/expapi/verbs/completed"
        },
        object: {
            id: searchParams.get("activity_id")
        },
        result: {
            completion: true,
            extensions: {
                "https://w3id.org/xapi/cmi5/result/extensions/progress": parseInt(document.getElementById("progress_text").value)
            }
        }
    };

    sendAndSetResultText(statementObj, "send_progress_result");
}

addEventListener("DOMContentLoaded", (event) => {
    document.getElementById("send_result_button").addEventListener('click', onClickSendResult);
    document.getElementById("send_completed_button").addEventListener('click', onClickSendCompleted);
    document.getElementById("send_progress_button").addEventListener('click', onClickSendProgressed);

    document.getElementById("actor_info").innerText = "Actor: " + JSON.stringify(queryParamsObject.actor);
    document.getElementById("activity_id").innerText = "Activity ID: " + searchParams.get("activity_id");
});

console.log("lesson_xapi: loaded");
