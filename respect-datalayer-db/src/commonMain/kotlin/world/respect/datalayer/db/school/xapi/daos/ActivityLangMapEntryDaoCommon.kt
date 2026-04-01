package world.respect.datalayer.db.school.xapi.daos

object ActivityLangMapEntryDaoCommon {

    const val INTO_LANG_MAP_WHERE_INTERACTION_ENTITY_EXISTS = """
        INTO ActivityLangMapEntry(almeActivityUid, almeProperty, almeLangCode, almeValue)
        SELECT :almeActivityUid AS almeActivityUid,
               :almeProperty AS almeProperty,
               :almeLangCode AS almeLangCode,
               :almeValue AS almeValue
         WHERE EXISTS(SELECT 1
                        FROM ActivityInteractionEntity
                       WHERE ActivityInteractionEntity.aieActivityUid = :almeActivityUid
                         AND ActivityInteractionEntity.aieProp = :aieProp)
    """

}