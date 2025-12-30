package com.example.demo.repository;

import com.example.demo.models.Orders;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends MongoRepository<Orders, String> {
    List<Orders> findByUserId(String id);

    List<Orders> findByUserIdAndStatusNot(String id, String delivered);

    @Query("{ 'userId' : ?0, 'status' : { $in: ?1 } }")
    List<Orders> findByUserIdAndStatusIn(String userId, List<String> statuses);


    List<Orders> findByOrderDateBetween(LocalDateTime orderDateAfter, LocalDateTime orderDateBefore);

    List<Orders> findByOrderDateBetweenAndStatus(LocalDateTime orderDateAfter, LocalDateTime orderDateBefore, String status);

}


