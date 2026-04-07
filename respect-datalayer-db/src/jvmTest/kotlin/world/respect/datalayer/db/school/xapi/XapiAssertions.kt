package world.respect.datalayer.db.school.xapi

import world.respect.datalayer.db.school.xapi.adapters.idStr
import world.respect.datalayer.school.xapi.model.XapiActivity
import world.respect.datalayer.school.xapi.model.XapiActivityStatementObject
import world.respect.datalayer.school.xapi.model.XapiActor
import world.respect.datalayer.school.xapi.model.XapiAgent
import world.respect.datalayer.school.xapi.model.XapiGroup
import world.respect.datalayer.school.xapi.model.XapiObjectType
import world.respect.datalayer.school.xapi.model.XapiStatement
import world.respect.datalayer.school.xapi.model.XapiStatementRef
import world.respect.datalayer.school.xapi.model.XapiVerb
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        is XapiActivityStatementObject -> {
            val actualObject = actual.`object`
            assertTrue(actualObject is XapiActivityStatementObject)
            assertEquals(
                expected = XapiObjectType.Activity,
                actual = actual.`object`.objectType ?: XapiObjectType.Activity,
                message = "When Xapi object is an Activity, then the objectType must be null or Activity"
            )

            val expectedDefinition = expectedStmtObject.definition
            if(expectedDefinition != null) {
                assertXapiActivityMatches(
                    expected = expectedDefinition,
                    actual = actualObject.definition!!
                )
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

fun assertXapiActivityMatches(
    expected: XapiActivity,
    actual: XapiActivity
) {
    fun assertInteractionListMatches(
        expected: List<XapiActivity.Interaction>?,
        actual: List<XapiActivity.Interaction>?,
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
        assertEquals(expected.member.size, actual.member.size,
            "Expected and actual member size should match")
        try {
            expected.member.forEach { expectedMember ->
                val memberInActual = actual.member.firstOrNull {
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
