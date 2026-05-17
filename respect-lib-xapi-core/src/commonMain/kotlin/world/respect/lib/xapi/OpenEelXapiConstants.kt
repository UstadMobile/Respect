package world.respect.lib.xapi

object OpenEelXapiConstants {

    const val ASSIGNMENT_XAPI_SEGMENT = "openeel_assignment"

    const val HEADER_XAPI_VERSION = "X-Experience-API-Version"

    /**
     * Required header on get statement results
     *
     * As per
     * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
     */
    const val HEADER_XAPI_CONSISTENT_THROUGH = "X-Experience-API-Consistent-Through"


}