package dev.code_offline.basalt_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class BasaltController {
	@GetMapping
	public ResponseEntity<String> getVersion() {
		return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
	}
}
