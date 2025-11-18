package dev.code_offline.basalt_server.controller;

import dev.code_offline.basalt_share.Util;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class SpringApplicationController {
	@GetMapping
	public ResponseEntity<Byte> getVersion() {
		return new ResponseEntity<>(Util.NETWORK_VERSION, HttpStatus.OK);
	}
}
