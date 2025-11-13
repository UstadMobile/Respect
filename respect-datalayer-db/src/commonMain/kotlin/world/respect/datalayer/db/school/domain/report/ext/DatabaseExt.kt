package world.respect.datalayer.db.school.domain.report.ext

import androidx.room.RoomDatabase
import androidx.room.TransactionScope
import androidx.room.Transactor.SQLiteTransactionType
import androidx.room.useWriterConnection

suspend fun <R> RoomDatabase.withWriterTransaction(
    txType:  SQLiteTransactionType = SQLiteTransactionType.IMMEDIATE,
    block: suspend TransactionScope<R>.() -> R
): R {
    return useWriterConnection { con ->
        con.withTransaction(txType, block)
    }
}
