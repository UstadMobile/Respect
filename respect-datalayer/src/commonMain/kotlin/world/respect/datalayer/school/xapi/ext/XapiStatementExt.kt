package world.respect.datalayer.school.xapi.ext

import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import world.respect.lib.xapi.model.XAPI_PROGRESSED_EXTENSIONS
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb

val XapiStatement.resultProgressExtension: Int?
    get() = result?.extensions?.let { extensions ->
        XAPI_PROGRESSED_EXTENSIONS.firstNotNullOfOrNull { extensionKey ->
            extensions.get(extensionKey)?.jsonPrimitive?.intOrNull
        }
    }


/**
 * Shorthand to determine if this statement contains completion or progress data.
 */
fun XapiStatement.isCompletionOrProgress(): Boolean {
    return result?.completion != null || resultProgressExtension != null
}

/**
 * Get a list of all actors referenced in this statement or its substatement including:
 * The statement actor itself
 * The object if it is an actor
 * Actors from a substatement
 * The team and instructor actor from context
 * The authority actor
 */
fun XapiStatement.allActors(): List<XapiActor> {
    return buildList {
        add(actor)
        (`object` as? XapiActor)?.also { add(it) }
        (`object` as? XapiStatement)?.also {
            addAll(it.allActors())
        }
        context?.team?.also { add(it) }
        context?.instructor?.also { add(it) }
        authority?.also { add(it) }
    }
}

/**
 * Get a list of all the activities with a definition related to the statement (e.g. those that
 * will need to be stored in the database's Activity table).
 */
fun XapiStatement.allDefinedActivities(): List<XapiActivity> {
    return buildList {
        fun addAllDefinedActivitiesInternal(list: List<XapiActivity>?) {
            list?.filter { it.definition != null }?.also { addAll(it) }
        }

        (`object` as? XapiActivity)?.takeIf { it.definition != null }?.also { add(it) }

        (`object` as? XapiStatement)?.also { subStatement ->
            addAll(subStatement.allDefinedActivities())
        }

        addAllDefinedActivitiesInternal(context?.contextActivities?.parent)
        addAllDefinedActivitiesInternal(context?.contextActivities?.grouping)
        addAllDefinedActivitiesInternal(context?.contextActivities?.category)
        addAllDefinedActivitiesInternal(context?.contextActivities?.other)
    }
}

fun XapiStatement.allDefinedVerbs(): List<XapiVerb> {
    return buildList {
        verb.display?.also { add(verb) }
        (`object` as? XapiStatement)?.also {
            addAll(it.allDefinedVerbs())
        }
    }
}
