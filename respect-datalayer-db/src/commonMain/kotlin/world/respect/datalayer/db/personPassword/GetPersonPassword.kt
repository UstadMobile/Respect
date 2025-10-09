package world.respect.datalayer.db.personPassword

import world.respect.datalayer.db.school.entities.PersonPasswordEntity

interface GetPersonPassword {
    suspend fun getPersonPassword(personGuid: String): PersonPasswordEntity?
}

