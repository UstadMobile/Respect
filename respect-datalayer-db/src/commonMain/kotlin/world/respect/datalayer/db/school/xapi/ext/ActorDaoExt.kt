package world.respect.datalayer.db.school.xapi.ext

import world.respect.datalayer.db.school.xapi.daos.ActorDao
import world.respect.datalayer.db.school.xapi.entities.ActorEntity
import world.respect.libutil.util.time.systemTimeInMillis

suspend fun ActorDao.insertOrUpdateActorsIfNameChanged(actors: List<ActorEntity>) {
    insertOrIgnoreListAsync(actors)
    val timeNow = systemTimeInMillis()
    actors.forEach {
        updateIfNameChanged(
            uid = it.actorUid,
            name = it.actorName,
            updateTime = timeNow
        )
    }
}