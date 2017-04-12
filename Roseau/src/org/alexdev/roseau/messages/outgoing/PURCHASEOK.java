package org.alexdev.roseau.messages.outgoing;

import org.alexdev.roseau.messages.OutgoingMessageComposer;
import org.alexdev.roseau.server.messages.Response;

public class PURCHASEOK implements OutgoingMessageComposer {

	@Override
	public void write(Response response) {
		response.init("PURCHASE_OK");

	}

}
