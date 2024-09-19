package ssafy.ddada.domain.match.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ssafy.ddada.domain.match.entity.MatchType;
import ssafy.ddada.domain.match.entity.RankType;
import ssafy.ddada.domain.match.entity.Match;
import ssafy.ddada.domain.match.entity.MatchStatus;

import java.util.Optional;
import java.util.Set;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @EntityGraph(attributePaths = {"team1", "team2"})
    @Query("""
        SELECT m
        FROM Match m
        WHERE m.id = :matchId
    """)
    Optional<Match> findByIdWithTeams(@Param("matchId") Long matchId);

    @EntityGraph(attributePaths = {"court", "team1", "team2"})
    @Query("""
        SELECT m
        FROM Match m
        WHERE (:keyword IS NULL OR m.court.name LIKE CONCAT('%', CAST(:keyword AS string), '%') OR m.court.address LIKE CONCAT('%', CAST(:keyword AS string), '%')) AND
            (:rankType IS NULL OR m.rankType = :rankType) AND
            (:matchTypes IS NULL OR m.matchType IN :matchTypes) AND
            (:statuses IS NULL OR m.status IN :statuses)
    """)
    Page<Match> findMatchesByKeywordAndTypeAndStatus(
            @Param("keyword") String keyword,
            @Param("rankType") RankType rankType,
            @Param("matchTypes") Set<MatchType> matchTypes,
            @Param("statuses") Set<MatchStatus> statuses,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"court", "team1", "team2", "manager"})
    @Query("""
        SELECT m
        FROM Match m
        WHERE (m.manager.id = :managerId) AND
            (:keyword IS NULL OR m.court.name LIKE CONCAT('%', CAST(:keyword AS string), '%') OR m.court.address LIKE CONCAT('%', CAST(:keyword AS string), '%')) AND
            (:todayOnly = FALSE OR m.matchDate = CURRENT_DATE) AND
            (:statuses IS NULL OR m.status IN :statuses)
    """)
    Page<Match> findFilteredMatches(
            @Param("managerId") Long managerId,
            @Param("keyword") String keyword,
            @Param("todayOnly") boolean todayOnly,
            @Param("statuses") Set<MatchStatus> statuses,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"court", "manager", "team1", "team2", "sets"})
    @Query("""
        SELECT m
        FROM Match m
        WHERE m.id = :matchId
    """)
    Optional<Match> findByIdWithInfos(@Param("matchId") Long matchId);

    @EntityGraph(attributePaths = {"scores"})
    @Query("""
        SELECT s
        FROM Set s
        WHERE s.match.id = :matchId AND s.setNumber = :setNumber
    """)
    Optional<ssafy.ddada.domain.match.entity.Set> findSetsByIdWithInfos(
            @Param("matchId") Long matchId,
            @Param("setNumber") Integer setNumber
    );

}
