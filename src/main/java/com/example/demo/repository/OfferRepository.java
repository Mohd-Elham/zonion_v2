package com.example.demo.repository;

import com.example.demo.models.offer.Offer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OfferRepository extends MongoRepository<Offer, String> {

    @Query("{ $and: ["
            + "{ 'active': true }, "
            + "{ 'startDate': { $lte: ?0 } }, "
            + "{ 'endDate': { $gte: ?1 } }"
            + "] }")
    List<Offer> findByActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime startDate, LocalDateTime endDate);
}
