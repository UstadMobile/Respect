package world.respect.datalayer.db.school.xapi.entities

enum class XapiStatementContextActivityJoinTypeEnum(val dbFlag: Int) {

    PARENT(1), GROUPING(2), CATEGORY(4), OTHER(8);

    companion object {

        fun fromDbFlag(value: Int): XapiStatementContextActivityJoinTypeEnum {
            return entries.first { it.dbFlag == value }
        }
    }

}