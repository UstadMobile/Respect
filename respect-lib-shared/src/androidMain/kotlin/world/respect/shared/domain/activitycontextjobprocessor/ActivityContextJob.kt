package world.respect.shared.domain.activitycontextjobprocessor

import android.app.Activity

fun interface ActivityContextJob {

    fun doJob(activity: Activity)

}
