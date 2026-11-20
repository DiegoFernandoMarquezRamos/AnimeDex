package com.ud4.animedex.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ud4.animedex.data.dao.AnimeDao
import com.ud4.animedex.data.entity.AnimeCharacter
import com.ud4.animedex.data.entity.AnimeCompany
import com.ud4.animedex.data.entity.AnimeEntity
import com.ud4.animedex.data.entity.AnimeGenreCrossRef
import com.ud4.animedex.data.entity.AnimeStaff
import com.ud4.animedex.data.entity.CharacterEntity
import com.ud4.animedex.data.entity.CharacterVoiceActor
import com.ud4.animedex.data.entity.CompanyEntity
import com.ud4.animedex.data.entity.GenreEntity
import com.ud4.animedex.data.entity.PersonEntity

@Database(
    entities = [
        AnimeEntity::class,
        GenreEntity::class,
        AnimeGenreCrossRef::class,
        CompanyEntity::class,
        CharacterEntity::class,
        PersonEntity::class,
        AnimeCompany::class,
        AnimeCharacter::class,
        AnimeStaff::class,
        CharacterVoiceActor::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao

    companion object {
        @Volatile
        private var instance: AnimeDatabase? = null

        fun get(context: Context): AnimeDatabase =
            instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDatabase::class.java,
                    "anime.db"
                )
                    .createFromAsset("databases/basededatos.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
    }
}
