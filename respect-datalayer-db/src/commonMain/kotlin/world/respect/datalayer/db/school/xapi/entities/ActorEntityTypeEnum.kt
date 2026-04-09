package world.respect.datalayer.db.school.xapi.entities

enum class ActorEntityTypeEnum(val flag: Int) {

    AGENT(XapiEntityObjectTypeFlags.AGENT),
    GROUP(XapiEntityObjectTypeFlags.GROUP);

    companion object {

        fun fromFlag(value: Int): ActorEntityTypeEnum {
            return entries.first { it.flag == value }
        }
    }

}