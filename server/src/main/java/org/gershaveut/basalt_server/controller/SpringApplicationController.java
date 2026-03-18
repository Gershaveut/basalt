package org.gershaveut.basalt_server.controller;

import org.gershaveut.basalt_share.Util;
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
