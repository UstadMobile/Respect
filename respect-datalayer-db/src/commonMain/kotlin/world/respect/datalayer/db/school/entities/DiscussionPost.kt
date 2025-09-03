package world.respect.datalayer.db.school.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class DiscussionPost() {

    @PrimaryKey(autoGenerate = true)
    var discussionPostUid: Long = 0

    /**
     * If this message is a top level post on the form, then discussionPostReplyToPostUid = 0.
     * Otherwise this is the discussionPostUid of the top level message.
     */
    var discussionPostReplyToPostUid: Long = 0

    var discussionPostTitle: String? = null

    //This is the HTML message
    var discussionPostMessage: String? = null

    var discussionPostStartDate: Long = 0

    /**
     * The CourseBlock uid of the discussion post
     */
    var discussionPostCourseBlockUid: Long = 0

    var dpDeleted: Boolean = false

    //The person who started this post
    var discussionPostStartedPersonUid: Long = 0

    // The Course Uid
    var discussionPostClazzUid: Long = 0

    var discussionPostLct: Long = 0

    @Deprecated("No longer used - will be removed Aug/24")
    var discussionPostVisible: Boolean = false

    @Deprecated("No longer used - will be removed Aug/24")
    var discussionPostArchive: Boolean = false

    companion object{
        const val TABLE_ID = 132
    }
}