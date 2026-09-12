package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity
import world.respect.datalayer.db.shared.InstantAsTimestampString

@Dao
interface XapiActivityProfileDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiActivityProfileDocumentEntity)

    @Query(
        """
        SELECT * 
          FROM xapi_activity_profile_document 
         WHERE activity_iri = :activityIri 
           AND profile_id = :profileId
        """
    )
    suspend fun findByActivityIriAndProfileId(
        activityIri: String,
        profileId: String,
    ): XapiActivityProfileDocumentEntity?

    @Query(
        """
        SELECT * 
          FROM xapi_activity_profile_document 
         WHERE id = :id
        """
    )
    suspend fun findById(id: String): XapiActivityProfileDocumentEntity?

    @Query(
        """
        SELECT profile_id 
          FROM xapi_activity_profile_document 
         WHERE activity_iri = :activityIri 
           AND (:since IS NULL OR last_modified > :since)
        """
    )
    suspend fun getProfileIds(activityIri: String, since: InstantAsTimestampString? = null): List<String>

    @Query(
        """
        DELETE FROM xapi_activity_profile_document
         WHERE activity_iri = :activityIri 
           AND profile_id = :profileId
        """
    )
    suspend fun deleteByActivityIriAndProfileId(
        activityIri: String,
        profileId: String,
    )
}