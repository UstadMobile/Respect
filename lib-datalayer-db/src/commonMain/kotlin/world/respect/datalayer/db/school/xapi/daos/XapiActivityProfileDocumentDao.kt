package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.composites.XapiActivityProfileDocumentAndShaEntities
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity
import world.respect.datalayer.db.shared.ETagAndLastModifiedAsStrings
import world.respect.datalayer.db.shared.InstantAsTimestampString

@Dao
interface XapiActivityProfileDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiActivityProfileDocumentEntity)

    @Query(
        """
        SELECT xapi_activity_profile_document.*, 
               xapi_activity_profile_document_sha.sha1_digest AS sha1
          FROM xapi_activity_profile_document 
               JOIN xapi_activity_profile_document_sha
                    ON xapi_activity_profile_document_sha.doc_id = xapi_activity_profile_document.id
         WHERE activity_iri = :activityIri 
           AND profile_id = :profileId
        """
    )
    suspend fun findByActivityIriAndProfileId(
        activityIri: String,
        profileId: String,
    ): XapiActivityProfileDocumentAndShaEntities?

    @Query("""
        SELECT xapi_activity_profile_document.last_modified AS lastModified,
               xapi_activity_profile_document_sha.sha1_digest AS etag
          FROM xapi_activity_profile_document 
          LEFT JOIN xapi_activity_profile_document_sha
            ON xapi_activity_profile_document.id = xapi_activity_profile_document_sha.doc_id
         WHERE activity_iri = :activityIri 
           AND profile_id = :profileId
    """)
    suspend fun findETagAndLastModifiedByActivityIriAndProfileId(
        activityIri: String,
        profileId: String,
    ): ETagAndLastModifiedAsStrings?

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