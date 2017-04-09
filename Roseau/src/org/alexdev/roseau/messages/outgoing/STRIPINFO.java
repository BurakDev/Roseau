package org.alexdev.roseau.messages.outgoing;

import java.util.List;

import org.alexdev.roseau.game.item.Item;
import org.alexdev.roseau.messages.OutgoingMessageComposer;
import org.alexdev.roseau.server.messages.Response;

public class STRIPINFO implements OutgoingMessageComposer {

	private List<Item> items;

	public STRIPINFO(List<Item> items) {
		this.items = items;
	}

	@Override
	public void write(Response response) {
		response.init("STRIPINFO");

		for (Item item : this.items) {
			response.appendNewArgument("roseau");
			response.appendArgument(String.valueOf(item.getId()), ';');
			response.appendArgument("0", ';');

			if (item.getDefinition().getBehaviour().isSTUFF()) {
				response.appendArgument("S", ';');
			} else if (item.getDefinition().getBehaviour().isITEM()) {
				response.appendArgument("I", ';');
			}
			
			response.appendArgument(String.valueOf(item.getId()), ';');
			response.appendArgument(item.getDefinition().getSprite(), ';');
			response.appendArgument(item.getDefinition().getName(), ';');
			
			if (item.getDefinition().getBehaviour().isSTUFF()) {
				
				response.appendArgument(item.getCustomData(), ';');
				response.appendArgument(String.valueOf(item.getDefinition().getLength()), ';');
				response.appendArgument(String.valueOf(item.getDefinition().getWidth()), ';');
				response.appendArgument(item.getDefinition().getColor(), ';');
				
			} else if (item.getDefinition().getBehaviour().isITEM()) {
				response.appendArgument(item.getCustomData(), ';');
				response.appendArgument(item.getCustomData(), ';');
			}
			
			response.appendArgument("", '/');
		}
	}

}
