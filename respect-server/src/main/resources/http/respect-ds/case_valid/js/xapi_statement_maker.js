//Downloaded as per https://github.com/xapijs/xapi/blob/develop/example/browser/module/index.html
import XAPI from "./xapi_module.js";
const { endpoint, auth } = XAPI.getTinCanLaunchData();

//As per https://www.xapijs.dev/xapi-wrapper-library/helpers
const queryParamsObject = XAPI.getSearchQueryParamsAsObject(location.search);

const xapi = new XAPI({
    endpoint: endpoint,
    auth: auth
});

function onClickMakeStatement() {
    console.log("Try to make a statement");

    const myStatement = JSON.parse(document.getElementById("statement").value);
    const statementWithActor = {
        ...myStatement,
        actor: XAPI.getSearchQueryParamsAsObject(location.search).actor
    };

    xapi.sendStatement({
        statement: statementWithActor
    });
}

addEventListener("DOMContentLoaded", (event) => {
    document.getElementById("statement_button").addEventListener('click', onClickMakeStatement);
});
