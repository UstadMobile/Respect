package world.respect.datalayer.school.adapters

import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.composites.PersonListDetails

fun Person.asListDetails(): PersonListDetails {
    return PersonListDetails(guid, givenName, familyName, username, email, phoneNumber)
}
fun PersonListDetails.asPerson(): Person {
    return Person(
        guid = guid,
        givenName = givenName,
        familyName = familyName,
        username = username,
        email = email,
        phoneNumber = phoneNumber,
        gender = PersonGenderEnum.UNSPECIFIED,
        roles = emptyList()
    )
}