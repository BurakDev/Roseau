package org.alexdev.roseau.messages.outgoing.room.pool;

import org.alexdev.roseau.messages.outgoing.OutgoingMessageComposer;
import org.alexdev.roseau.server.messages.Response;

public class OPEN_UIMAKOPPI implements OutgoingMessageComposer {

	@Override
	public void write(Response response) {
		response.init("OPEN_UIMAKOPPI");
	}

}
