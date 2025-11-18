package world.respect.datalayer.school

import world.respect.datalayer.school.model.Invite
import world.respect.datalayer.shared.LocalModelDataSource

interface InviteDataSourceLocal: InviteDataSource, LocalModelDataSource<Invite>
