package world.respect.datalayer.school.adapters

import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails

fun Person.asListDetails(): PersonListDetails {
    return PersonListDetails(guid, givenName, familyName, username, email, phoneNumber)
}
