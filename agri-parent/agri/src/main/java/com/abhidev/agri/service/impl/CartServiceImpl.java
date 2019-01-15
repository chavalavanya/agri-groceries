package com.abhidev.agri.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abhidev.agri.model.Cart;
import com.abhidev.agri.repository.CartRepository;
import com.abhidev.agri.service.CartService;
import com.abhidev.agri.util.POJOConverter;

@Service
public class CartServiceImpl implements CartService{
	
	@Autowired CartRepository cartRepository;
	
	@Override
	public void createCart(Cart cart) {
		cartRepository.createCartDocument(cart);
	}

	@Override
	public Cart getCart(String id) {
		return cartRepository.getCart(id);
	}

}
