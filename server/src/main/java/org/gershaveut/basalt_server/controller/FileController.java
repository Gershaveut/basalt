package org.gershaveut.basalt_server.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Secured({"ROLE_MEMBER"})
@RequestMapping("/files")
public class FileController extends AbstractFileController {
}
