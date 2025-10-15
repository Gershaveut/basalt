package dev.code_offline.basalt_server.websocket;

import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleAfterDelete;
import org.springframework.data.rest.core.annotation.HandleAfterSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class BasaltRepositoryEventHandler {
	private final BasaltSocketHandler socketHandler;
	
	public BasaltRepositoryEventHandler(BasaltSocketHandler socketHandler) {
		this.socketHandler = socketHandler;
	}
	
	@HandleAfterCreate
	@HandleAfterSave
	@HandleAfterDelete
	public void sync(Object o) {
		socketHandler.sync();
	}
}
