package world.respect.datalayer.school

import world.respect.datalayer.school.model.PersonBadge
import world.respect.datalayer.shared.LocalModelDataSource

interface PersonQrCodeDataSourceLocal: PersonQrDataSource, LocalModelDataSource<PersonBadge>