package im.bigs.pg.application.pg.port.out

data class PgFailedResult(
    val code: Long,
    val errorCode: String,
    val message: String,
    val referenceId: String
)
