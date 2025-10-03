package com.rev.app

import androidx.room.*

@Entity(tableName = "bookmark")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val threadId: Long,
    val createdAt: Long
)

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BookmarkEntity)

    @Query("SELECT * FROM bookmark ORDER BY createdAt DESC LIMIT :limit")
    suspend fun list(limit: Int): List<BookmarkEntity>
}

@Database(entities = [BookmarkEntity::class], version = 1)
abstract class RevDb : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}
