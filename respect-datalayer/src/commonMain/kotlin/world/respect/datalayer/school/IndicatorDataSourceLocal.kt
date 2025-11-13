package world.respect.datalayer.school

import world.respect.datalayer.school.model.Indicator
import world.respect.datalayer.shared.LocalModelDataSource

interface IndicatorDataSourceLocal : IndicatorDataSource, LocalModelDataSource<Indicator>