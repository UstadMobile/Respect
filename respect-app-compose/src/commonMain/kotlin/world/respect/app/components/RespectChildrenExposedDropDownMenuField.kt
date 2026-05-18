package world.respect.app.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import world.respect.datalayer.db.school.ext.fullName
import world.respect.datalayer.school.model.Person
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.select_child
import world.respect.shared.generated.resources.required
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText

@Composable
fun RespectChildrenExposedDropDownMenuField(
    value: Person?,
    options: List<Person>,
    onValueChanged: (Person) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    enabled: Boolean = true,
    errorText: UiText?,
) {
    RespectExposedDropDownMenuField(
        value = value,
        options = options,
        onOptionSelected = onValueChanged,
        modifier = modifier,
        itemText = { person ->
            person.fullName()
        },
        label = {
            Text(uiTextStringResource(Res.string.select_child.asUiText()) )
        },
        supportingText = {
            Text(uiTextStringResource(errorText ?: Res.string.required.asUiText()))
        },
        isError = isError,
        enabled = enabled,
    )
}