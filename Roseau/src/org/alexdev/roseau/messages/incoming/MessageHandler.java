package org.alexdev.roseau.messages.incoming;

import java.util.HashMap;

import org.alexdev.roseau.game.player.Player;
import org.alexdev.roseau.messages.incoming.handshake.VERSIONCHECK;
import org.alexdev.roseau.messages.incoming.login.GETCREDITS;
import org.alexdev.roseau.messages.incoming.login.INFORETRIEVE;
import org.alexdev.roseau.messages.incoming.login.INITUNITUSER;
import org.alexdev.roseau.messages.incoming.login.LOGIN;
import org.alexdev.roseau.server.messages.ClientMessage;

import com.google.common.collect.Maps;

public class MessageHandler {
	
	private HashMap<String, MessageEvent> messages;

	public MessageHandler() {
		this.messages = Maps.newHashMap();
		this.register();
	}
	
	public void register() {
		this.messages.clear();
		this.registerHandshakePackets();
		this.registerLoginPackets();
	}

	private void registerHandshakePackets() {
		this.messages.put("VERSIONCHECK", new VERSIONCHECK());
	}
	
	private void registerLoginPackets() {
		this.messages.put("LOGIN", new LOGIN());
		this.messages.put("INFORETRIEVE", new INFORETRIEVE());
		this.messages.put("INITUNITUSER", new INITUNITUSER());
		this.messages.put("GETCREDITS", new GETCREDITS());
	}
	
	
	public void handleRequest(Player player, ClientMessage message) {
		if (messages.containsKey(message.getHeader())) {
			messages.get(message.getHeader()).handle(player, message);
		}
	}

	public HashMap<String, MessageEvent> getMessages() {
		return messages;
	}

}
