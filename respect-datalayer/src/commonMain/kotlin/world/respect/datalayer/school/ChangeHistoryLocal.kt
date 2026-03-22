package world.respect.datalayer.school

import world.respect.datalayer.school.model.ChangeHistoryEntry
import world.respect.datalayer.shared.LocalModelDataSource

interface ChangeHistoryLocal: ChangeHistoryDataSource, LocalModelDataSource<ChangeHistoryEntry>
