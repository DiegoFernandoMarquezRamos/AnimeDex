package com.ud4.animedex

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
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

@Dao
interface AnimeDao {

    @Query("SELECT * FROM anime ORDER BY rank LIMIT :limit")
    fun topAnime(limit: Int): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE anime_id = :id")
    fun byId(id: Int): Flow<AnimeEntity?>

    @Transaction
    @Query("SELECT * FROM anime WHERE anime_id = :id")
    fun detail(id: Int): Flow<AnimeWithGenres?>

    // 1. Buscar por título (LIKE '%' || :text || '%'), ordenado por rank
    @Query("SELECT * FROM anime WHERE title LIKE '%' || :text || '%' ORDER BY rank")
    fun searchByTitle(text: String): Flow<List<AnimeEntity>>

    // 2. Anime de un género (necesita JOIN con anime_genre y genre)
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

    // 3. Anime con puntuación mínima
    @Query("SELECT * FROM anime WHERE score >= :minScore ORDER BY score DESC")
    fun animeByMinScore(minScore: Double): Flow<List<AnimeEntity>>

    // 4. Anime de un tipo
    @Query("SELECT * FROM anime WHERE type = :type ORDER BY rank")
    fun animeByType(type: String): Flow<List<AnimeEntity>>

    // 5. Anime sin número de episodios (IS NULL)
    @Query("SELECT * FROM anime WHERE episodes IS NULL ORDER BY rank")
    fun animeWithoutEpisodes(): Flow<List<AnimeEntity>>

    // 6. Los tres anime con más miembros
    @Query("SELECT * FROM anime ORDER BY members DESC LIMIT 3")
    fun top3ByMembers(): Flow<List<AnimeEntity>>

    // 7. Estudio con más anime (tabla company, rol Studio)
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
}
