package com.ud4.animedex.data.dao

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.ud4.animedex.data.entity.AnimeEntity
import com.ud4.animedex.data.entity.AnimeGenreCrossRef
import com.ud4.animedex.data.entity.GenreEntity
import kotlinx.coroutines.flow.Flow

data class AnimeWithGenres(
    @Embedded val anime: AnimeEntity,
    @Relation(
        parentColumn = "anime_id",
        entityColumn = "genre_id",
        associateBy = Junction(
            value = AnimeGenreCrossRef::class,
            parentColumn = "anime_id",
            entityColumn = "genre_id",
        )
    )
    val genres: List<GenreEntity>
)

data class StudioAnimeCount(
    val name: String?,
    val count: Int
)

data class CharacterWithVoice(
    val name: String?,
    val imageUrl: String?,
    val role: String,
    val voiceActor: String?
)

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime ORDER BY rank LIMIT :limit")
    fun topAnime(limit: Int): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE anime_id = :id")
    fun byId(id: Int): Flow<AnimeEntity?>

    @Transaction
    @Query("SELECT * FROM anime WHERE anime_id = :id")
    fun detail(id: Int): Flow<AnimeWithGenres?>

    @Query("SELECT * FROM anime WHERE title LIKE '%' || :text || '%' ORDER BY rank")
    fun searchByTitle(text: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE title LIKE '%' || :text || '%' ORDER BY rank")
    fun search(text: String): Flow<List<AnimeEntity>>

    @Query(
        """
        SELECT * FROM anime
        WHERE title LIKE '%' || :text || '%' AND score >= :minScore
        ORDER BY
          CASE WHEN :sort = 'SCORE' THEN score END DESC,
          CASE WHEN :sort = 'POPULARITY' THEN popularity END ASC,
          rank ASC
        """
    )
    fun search(text: String, minScore: Float, sort: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE anime_id IN (:ids) ORDER BY rank")
    fun byIds(ids: List<Int>): Flow<List<AnimeEntity>>

    @Query(
        """
        SELECT anime.* FROM anime 
        INNER JOIN anime_genre ON anime.anime_id = anime_genre.anime_id 
        INNER JOIN genre ON anime_genre.genre_id = genre.genre_id 
        WHERE genre.name = :genre 
        ORDER BY anime.rank
        """
    )
    fun animeByGenre(genre: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE score >= :minScore ORDER BY score DESC")
    fun animeByMinScore(minScore: Double): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE type = :type ORDER BY rank")
    fun animeByType(type: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE episodes IS NULL ORDER BY rank")
    fun animeWithoutEpisodes(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime ORDER BY members DESC LIMIT 3")
    fun top3ByMembers(): Flow<List<AnimeEntity>>

    @Query(
        """
        SELECT company.name, COUNT(anime_company.anime_id) AS count
        FROM company
        INNER JOIN anime_company ON company.company_id = anime_company.company_id
        WHERE anime_company.role = 'Studio'
        GROUP BY company.company_id
        ORDER BY count DESC
        LIMIT 1
        """
    )
    fun topStudio(): Flow<StudioAnimeCount?>

    @Query(
        """
        SELECT company.name, COUNT(anime_company.anime_id) AS count
        FROM company
        INNER JOIN anime_company ON company.company_id = anime_company.company_id
        WHERE anime_company.role = 'Studio'
        GROUP BY company.company_id
        ORDER BY count DESC
        """
    )
    fun studiosByAnimeCount(): Flow<List<StudioAnimeCount>>

    @Query(
        """
        SELECT company.name
        FROM company
        INNER JOIN anime_company ON company.company_id = anime_company.company_id
        WHERE anime_company.anime_id = :id AND anime_company.role = 'Studio'
        LIMIT 1
        """
    )
    fun studio(id: Int): Flow<String?>

    @Query(
        """
        SELECT 
            c.name AS name,
            c.image_url AS imageUrl,
            ac.role AS role,
            p.name AS voiceActor
        FROM anime_character ac
        INNER JOIN character c ON ac.character_id = c.character_id
        LEFT JOIN character_voice_actor cva ON c.character_id = cva.character_id
        LEFT JOIN person p ON cva.person_id = p.person_id
        WHERE ac.anime_id = :id AND ac.role = 'Main'
        """
    )
    fun mainCharactersWithVoice(id: Int): Flow<List<CharacterWithVoice>>
}
