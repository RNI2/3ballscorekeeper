package com.nodetoursstudio.threeball.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "active_game")
data class ActiveEntity(@PrimaryKey val slot: Int = 1, val gameId: String, val payload: String,
    val revision: Int, @ColumnInfo(defaultValue = "0") val updatedAt: Long)
@Entity(tableName = "completed_games")
data class CompletedEntity(@PrimaryKey val gameId: String, val payload: String,
    @ColumnInfo(defaultValue = "0") val completedAt: Long)
@Entity(tableName = "quarantined_records")
data class QuarantinedEntity(@PrimaryKey val gameId: String, val payload: String, val detectedAt: Long)

@Dao
interface GameDao {
    @Query("SELECT * FROM active_game WHERE slot = 1") suspend fun active(): ActiveEntity?
    @Query("SELECT * FROM completed_games ORDER BY completedAt DESC, gameId") suspend fun history(): List<CompletedEntity>
    @Query("SELECT * FROM completed_games WHERE gameId = :id") suspend fun completed(id: String): CompletedEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putActive(row: ActiveEntity)
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun archive(row: CompletedEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun quarantine(row: QuarantinedEntity)
    @Query("DELETE FROM active_game WHERE slot = 1") suspend fun clearActive()
    @Query("SELECT COUNT(*) FROM quarantined_records") suspend fun quarantineCount(): Int
}

@Database(entities = [ActiveEntity::class, CompletedEntity::class, QuarantinedEntity::class], version = 2, exportSchema = true)
abstract class GameDatabase : RoomDatabase() {
    abstract fun games(): GameDao
    companion object {
        // v1 was the pre-release payload-only schema. Never use destructive fallback.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE active_game ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE completed_games ADD COLUMN completedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS quarantined_records (gameId TEXT NOT NULL, payload TEXT NOT NULL, detectedAt INTEGER NOT NULL, PRIMARY KEY(gameId))")
            }
        }
        fun open(context: Context): GameDatabase = Room.databaseBuilder(context.applicationContext,
            GameDatabase::class.java, "three-ball.db").addMigrations(MIGRATION_1_2).build()
    }
}
