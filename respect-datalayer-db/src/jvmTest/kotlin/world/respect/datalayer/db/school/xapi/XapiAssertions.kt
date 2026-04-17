package world.respect.datalayer.db.school.xapi

import world.respect.datalayer.school.xapi.ext.idStr
import world.respect.datalayer.school.xapi.model.XapiActivityDefinition
import world.respect.datalayer.school.xapi.model.XapiActivity
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiContextActivities
import world.respect.datalayer.school.xapi.model.XapiGroup
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiStatementRef
import world.respect.datalayer.school.xapi.model.XapiVerb
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


fun assertContextActivitiesMatches(
    expected: XapiContextActivities?,
    actual: XapiContextActivities?,
) {
    fun assertContextActivityMatch(
        expected: List<XapiActivity>?,
        actual: List<XapiActivity>?,
    ) {
        if(expected != null) {
            assertNotNull(actual)
            assertEquals(expected.size, actual.size)

            expected.forEach { expectedActivity ->
                val actualActivity = actual.firstOrNull {
                    it.id == expectedActivity.id
                }
                assertNotNull(actualActivity)

                //The statement received might have only the id. The canonical response (actual)
                //will include the definition if available, so we do not assert that if the
                //expected activity definition is null that the actual activity definition will
                //also be null
                expectedActivity.definition?.also {
                    val actualDefinition = actualActivity.definition
                    assertNotNull(actualDefinition)
                    assertXapiActivityDefinitionMatches(it, actualDefinition)
                }
            }
        }else {
            assertNull(actual)
        }
    }

    if(expected != null) {
        assertNotNull(actual)
        assertContextActivityMatch(expected.parent, actual.parent)
        assertContextActivityMatch(expected.grouping, actual.grouping)
        assertContextActivityMatch(expected.category, actual.category)
        assertContextActivityMatch(expected.other, actual.other)
    }else {
        assertNull(actual)
    }
}

/*
 * Exact equality checks as provided by the data classes don't make sense here: e.g. the member list
 * is unordered as per https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
 * so an assertEquals would fail when actually the result is entirely compliant with the spec. Or a
 * statement may include a useless Result object with no properties (as they are all optional).
 *
 */
fun assertXapiStatementMatches(
    expected: XapiStatement,
    actual : XapiStatement,
    messagePrefix: String = "",
) {

    assertEquals(expected.id, actual.id)
    assertXapiActorMatches(expected.actor, actual.actor)
    assertXapiVerbMatches(expected.verb, actual.verb)
    val expectedStmtObject = expected.`object`

    when(expectedStmtObject) {
        is XapiActivity -> {
            val actualObject = actual.`object`
            assertTrue(actualObject is XapiActivity)
            assertEquals(
                expected = XapiObjectType.Activity,
                actual = actual.`object`.objectType ?: XapiObjectType.Activity,
                message = "When Xapi object is an Activity, then the objectType must be null or Activity"
            )

            val expectedDefinition = expectedStmtObject.definition
            if(expectedDefinition != null) {
                val actualDefinition = actualObject.definition
                assertNotNull(actualDefinition)
                assertXapiActivityDefinitionMatches(expectedDefinition, actualDefinition)
            }else {
                assertNull(actualObject.definition)
            }
        }

        is XapiStatement -> {
            assertEquals(
                XapiObjectType.SubStatement, actual.`object`.objectType,
                message = "$messagePrefix When Xapi object is a statement, then objectType must be a SubStatement"
            )

            assertXapiStatementMatches(
                expected = expectedStmtObject,
                actual = actual.`object` as XapiStatement,
            )
        }

        is XapiAgent -> {
            assertXapiActorMatches(
                expected = expectedStmtObject,
                actual = actual.`object` as XapiAgent
            )
        }

        is XapiGroup -> {
            assertXapiActorMatches(
                expected = expectedStmtObject,
                actual = actual.`object` as XapiGroup
            )
        }

        is XapiStatementRef -> {
            val actualStatementObject = actual.`object`
            assertTrue(actualStatementObject is XapiStatementRef)
            assertEquals(expectedStmtObject.id, actualStatementObject.id)
        }
    }

    val expectedResult = expected.result
    val actualResult = actual.result
    if(expectedResult != null) {
        assertNotNull(actualResult)
        assertEquals(expectedResult.completion, actualResult.completion)
        assertEquals(expectedResult.success, actualResult.success)
        assertEquals(expectedResult.score, actualResult.score)
        assertEquals(expectedResult.duration, actualResult.duration)
        assertEquals(expectedResult.response, actualResult.response)
        assertEquals(expectedResult.extensions, actualResult.extensions)
    }else {
        assertNull(actual.result)
    }

    val expectedContext = expected.context
    val actualContext = actual.context
    if(expectedContext != null) {
        assertNotNull(actualContext)

        val expectedInstructor = expectedContext.instructor
        if(expectedInstructor != null) {
            val actualInstructor = actualContext.instructor
            assertNotNull(actualInstructor)
            assertXapiActorMatches(expectedInstructor, actualInstructor)
        }else {
            assertNull(actualContext.instructor)
        }

        assertEquals(expectedContext.registration, actualContext.registration)
        assertEquals(expectedContext.language, actualContext.language)
        assertEquals(expectedContext.platform, actualContext.platform)
        assertEquals(expectedContext.revision, actualContext.revision)

        val expectedTeam = expectedContext.team
        if(expectedTeam != null) {
            val actualTeam = actualContext.team
            assertNotNull(actualTeam)
            assertXapiActorMatches(expectedTeam, actualTeam)
        }else {
            assertNull(actualContext.team)
        }

        assertContextActivitiesMatches(
            expected = expectedContext.contextActivities,
            actual = actualContext.contextActivities
        )
    }else {
        assertNull(actualContext)
    }

    val expectedAuthority = expected.authority
    if(expectedAuthority != null) {
        val actualAuthority = actual.authority
        assertNotNull(actualAuthority)
        assertXapiActorMatches(expectedAuthority, actualAuthority)
    }else {
        assertNull(actual.authority)
    }
    assertEquals(expected.version, actual.version)
}

fun assertXapiVerbMatches(
    expected: XapiVerb,
    actual: XapiVerb
) {
    assertEquals(expected.id, actual.id)
    assertLangMapEquals(expected.display, actual.display)
}

fun assertLangMapEquals(
    expected: Map<String, String>?,
    actual: Map<String, String>?,
) {
    //As per the xAPI spec: empty objects SHOULD be avoided. Therefor we will treat an empty object
    //and a missing object (null) as the same.
    assertEquals(expected?.size ?: 0, actual?.size ?: 0)
    expected?.forEach { (key, value) ->
        assertEquals(value, actual?.get(key))
    }
}

fun assertXapiActivityDefinitionMatches(
    expected: XapiActivityDefinition,
    actual: XapiActivityDefinition
) {
    fun assertInteractionListMatches(
        expected: List<XapiActivityDefinition.Interaction>?,
        actual: List<XapiActivityDefinition.Interaction>?,
    ) {
        assertEquals(expected?.size, actual?.size)
        expected?.forEachIndexed { index, interaction ->
            assertEquals(interaction.id, actual!![index].id)
            assertLangMapEquals(interaction.description, actual[index].description)
        }
    }


    assertLangMapEquals(expected.name, actual.name)
    assertLangMapEquals(expected.description, actual.description)
    assertEquals(expected.type, actual.type)
    assertEquals(expected.extensions, actual.extensions)
    assertEquals(expected.moreInfo, actual.moreInfo)
    assertEquals(expected.interactionType, actual.interactionType)
    assertEquals(expected.correctResponsesPattern, actual.correctResponsesPattern)
    assertInteractionListMatches(expected.choices, actual.choices)
    assertInteractionListMatches(expected.scale, actual.scale)
    assertInteractionListMatches(expected.source, actual.source)
    assertInteractionListMatches(expected.target, actual.target)
    assertInteractionListMatches(expected.steps, actual.steps)
}

fun assertXapiActorCommonPropsMatch(
    expected: XapiActor,
    actual: XapiActor,
) {
    assertEquals(expected.name, actual.name)
    assertEquals(expected.mbox, actual.mbox)
    assertEquals(expected.mbox_sha1sum, actual.mbox_sha1sum)
    assertEquals(expected.openid, actual.openid)
    assertEquals(expected.objectType, actual.objectType)
    assertEquals(expected.account?.name, actual.account?.name)
    assertEquals(expected.account?.homePage, actual.account?.homePage)
}

fun assertXapiActorMatches(
    expected: XapiActor,
    actual: XapiActor
) {
    assertXapiActorCommonPropsMatch(expected, actual)

    assertEquals(expected is XapiAgent, actual is XapiAgent)
    assertEquals(expected is XapiGroup, actual is XapiGroup)
    if(expected is XapiGroup && actual is XapiGroup) {
        /**
         * As per the xAPI spec : an identified group can be referenced without including the members
         * themselves.
         */
        expected.member?.size?.also { expectedMemberSize ->
            assertEquals(expectedMemberSize, actual.member?.size,
                "Expected and actual member size should match in group ${expected.idStr}")
        }

        try {
            /**
             * As per the spec: order does not matter:
             * An LRS returning a Statement MAY return the list of Group members in any order.
             * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#2422-when-the-actor-objecttype-is-group
             */
            expected.member?.forEach { expectedMember ->
                val memberInActual = actual.member?.firstOrNull {
                    it.idStr == expectedMember.idStr
                } ?: throw AssertionError("Member $expectedMember not found in other group")
                assertXapiActorMatches(
                    expected = expectedMember,
                    actual = memberInActual
                )
            }
        }catch(e: Throwable) {
            e.printStackTrace()
            throw e
        }

    }
}
