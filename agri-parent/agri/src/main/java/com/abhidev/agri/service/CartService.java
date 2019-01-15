package com.abhidev.agri.service;

import com.abhidev.agri.model.Cart;

public interface CartService {
	void createCart(Cart cart);
	Cart getCart(String id);
	
}
