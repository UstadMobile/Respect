package world.respect.datalayer.db.school.xapi.entities

enum class StatementContextActivityJoinTypeEnum(val dbFlag: Int) {

    PARENT(1), GROUPING(2), CATEGORY(4), OTHER(8);

    companion object {

        fun fromDbFlag(value: Int): StatementContextActivityJoinTypeEnum {
            return entries.first { it.dbFlag == value }
        }
    }

}