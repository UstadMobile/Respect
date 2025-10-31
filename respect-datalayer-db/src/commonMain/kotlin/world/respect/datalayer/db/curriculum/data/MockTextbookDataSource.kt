package world.respect.datalayer.db.curriculum.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import world.respect.datalayer.db.curriculum.entities.TextbookMapping

class MockTextbookDataSource {
    private val textbooks = MutableStateFlow<List<TextbookMapping>>(emptyList())

    fun getTextbooks(): Flow<List<TextbookMapping>> = textbooks

    fun insertOrUpdate(textbook: TextbookMapping) {
        val current = textbooks.value.toMutableList()

        val index = current.indexOfFirst { it.uid == textbook.uid }
        if (index >= 0) {
            current[index] = textbook
        } else {
            val newId = (current.maxOfOrNull { it.uid } ?: 0) + 1
            current.add(textbook.copy(uid = newId))
        }

        textbooks.value = current
    }
}