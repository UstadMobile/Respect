package world.respect.datalayer.school.opds

import world.respect.datalayer.shared.LocalModelDataSource
import world.respect.lib.opds.model.OpdsFeed

interface OpdsFeedDataSourceLocal: OpdsFeedDataSource, LocalModelDataSource<OpdsFeed>
