package world.respect.datalayer.db.personPassword

import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.entities.PersonPasswordEntity
import world.respect.libxxhash.XXStringHasher

class GetPersonPasswordDbImpl(
    val respectSchoolDatabase: RespectSchoolDatabase,
    val xxHash: XXStringHasher
) : GetPersonPassword {
    override suspend fun getPersonPassword(personGuid: String): PersonPasswordEntity? {
        val personGuidHash = xxHash.hash(personGuid)
        return respectSchoolDatabase.getPersonPasswordEntityDao().findByUid(personGuidHash)
    }
}