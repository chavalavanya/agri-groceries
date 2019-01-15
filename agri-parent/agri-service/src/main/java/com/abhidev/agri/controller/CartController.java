package com.abhidev.agri.controller;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.abhidev.agri.model.Cart;
import com.abhidev.agri.service.CartService;

@RestController
@RequestMapping("/api/v1/")
public class CartController {
	
	@Autowired CartService cartService;
	
	@GetMapping(path="/cart/{id}")
	public ResponseEntity<Cart> retrieveCart(@PathVariable String id){
		return new ResponseEntity<>(cartService.getCart(id),HttpStatus.OK);
	}
	
	@PutMapping(path="/cart")
	public ResponseEntity<Cart> addCart(@RequestBody Cart cart){
		cartService.createCart(cart);
		
		HttpHeaders headers = new HttpHeaders();
		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/id").buildAndExpand(cart.getId()).toUri();
		headers.setLocation(location);
		return new ResponseEntity<>(cart,headers,HttpStatus.OK);
	}
	
}
