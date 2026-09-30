package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByEventIdAndUserId(Long eventId, Long userId);

    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    @Query("SELECT r.event.id, " +
            "SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END) " +
            "FROM Rating r " +
            "WHERE r.event.id IN :eventIds " +
            "GROUP BY r.event.id")
    List<Object[]> findRatingsByEventIds(@Param("eventIds") List<Long> eventIds);

    @Query("SELECT SUM(CASE WHEN r.isLike = true THEN 1 ELSE -1 END) " +
            "FROM Rating r " +
            "WHERE r.event.id = :eventId")
    Long findRatingByEventId(@Param("eventId") Long eventId);
}