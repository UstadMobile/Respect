package world.respect.datalayer.school

import world.respect.datalayer.school.model.Assignment
import world.respect.datalayer.shared.LocalModelDataSource

interface AssignmentDataSourceLocal: AssignmentDataSource, LocalModelDataSource<Assignment>

