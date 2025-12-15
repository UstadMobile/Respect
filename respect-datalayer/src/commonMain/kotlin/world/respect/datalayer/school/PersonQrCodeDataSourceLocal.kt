package world.respect.datalayer.school

import world.respect.datalayer.school.model.PersonQrCode
import world.respect.datalayer.shared.LocalModelDataSource

interface PersonQrCodeDataSourceLocal: PersonQrDataSource, LocalModelDataSource<PersonQrCode>