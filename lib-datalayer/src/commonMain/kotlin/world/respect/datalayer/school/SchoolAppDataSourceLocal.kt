package world.respect.datalayer.school

import world.respect.datalayer.school.model.SchoolApp
import world.respect.datalayer.shared.LocalModelDataSource

interface SchoolAppDataSourceLocal: SchoolAppDataSource, LocalModelDataSource<SchoolApp>