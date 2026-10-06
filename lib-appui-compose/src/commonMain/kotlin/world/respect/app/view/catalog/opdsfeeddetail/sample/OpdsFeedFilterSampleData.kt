package world.respect.app.view.catalog.opdsfeeddetail.sample

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectFilterDefinition
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.arabic
import world.respect.shared.generated.resources.audio
import world.respect.shared.generated.resources.book
import world.respect.shared.generated.resources.english
import world.respect.shared.generated.resources.grade
import world.respect.shared.generated.resources.grade_number
import world.respect.shared.generated.resources.hindi
import world.respect.shared.generated.resources.language
import world.respect.shared.generated.resources.mathematics
import world.respect.shared.generated.resources.science
import world.respect.shared.generated.resources.subject
import world.respect.shared.generated.resources.type
import world.respect.shared.generated.resources.video

// Temporary UI sample data; remove when real filter options are available.
@Composable
internal fun defaultOpdsFeedFilters(): List<RespectFilterDefinition> = listOf(
    RespectFilterDefinition(
        id = "language",
        label = stringResource(Res.string.language),
        options = listOf(
            stringResource(Res.string.english),
            stringResource(Res.string.hindi),
            stringResource(Res.string.arabic),
        ),
    ),
    RespectFilterDefinition(
        id = "grade",
        label = stringResource(Res.string.grade),
        options = (1..12).map { stringResource(Res.string.grade_number, it) },
    ),
    RespectFilterDefinition(
        id = "subject",
        label = stringResource(Res.string.subject),
        options = listOf(
            stringResource(Res.string.mathematics),
            stringResource(Res.string.science),
            stringResource(Res.string.english),
        ),
    ),
    RespectFilterDefinition(
        id = "type",
        label = stringResource(Res.string.type),
        options = listOf(
            stringResource(Res.string.book),
            stringResource(Res.string.audio),
            stringResource(Res.string.video),
        ),
    ),
)
