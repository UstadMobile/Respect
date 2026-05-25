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
            score: {
                scaled: parseFloat(document.getElementById("score_text").value)
            }
        }
    };

    xapi.sendStatement({
        statement: myStatement
    }).then((result) => {
        console.log(result);
    });
}


addEventListener("DOMContentLoaded", (event) => {
    document.getElementById("statement_button").addEventListener('click', onClickSendStatement);
    document.getElementById("actor_info").innerText = JSON.stringify(queryParamsObject.actor);
    document.getElementById("activity_id").innerText = searchParams.get("activity_id");
});
