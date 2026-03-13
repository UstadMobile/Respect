package world.respect.datalayer.school

import world.respect.datalayer.school.model.Bookmark
import world.respect.datalayer.shared.LocalModelDataSource


interface BookmarkDataSourceLocal: BookmarkDataSource, LocalModelDataSource<Bookmark>



