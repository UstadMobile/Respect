package world.respect.lib.xapi.ext

import world.respect.lib.xapi.model.XapiActivity

fun List<XapiActivity>.addOrReplaceById(
    other: XapiActivity
) : List<XapiActivity> {
    val index = indexOfFirst { it.id == other.id }
    return if(index > 0) {
        this.toMutableList().also {
            it[index] = other
        }.toList()
    }else{
        this + other
    }
}
