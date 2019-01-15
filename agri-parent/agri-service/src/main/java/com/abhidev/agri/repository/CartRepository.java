package com.abhidev.agri.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.abhidev.agri.model.Cart;

@Repository
public class CartRepository {
	
	@Autowired MongoTemplate mongoTemplate;
    
    public void createCartDocument(final Cart form) {
    		mongoTemplate.save(form,"Cart");
    }
    
    public Cart getCart(final String id) {
    		return mongoTemplate.findById(id,Cart.class,"Cart");
    }
}
