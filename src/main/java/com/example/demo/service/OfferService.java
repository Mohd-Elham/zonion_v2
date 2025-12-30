package com.example.demo.service;

import com.example.demo.models.offer.Offer;
import com.example.demo.repository.OfferRepository;
import org.springframework.data.mongodb.core.convert.ObjectPath;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OfferService {


    private final OfferRepository offerRepository;

    public OfferService(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    public List<Offer> findAll() {
        return offerRepository.findAll();
    }

    public void saveOffer(Offer newOffer) {
        offerRepository.save(newOffer);
    }

    public List<Offer> findActiveOffers() {
        LocalDateTime now = LocalDateTime.now();
    return offerRepository.findByActiveTrueAndStartDateBeforeAndEndDateAfter(now,now);
    }

    public Optional<Offer> findById(String id) {



        return offerRepository.findById(id);
    }
}
