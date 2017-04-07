package org.alexdev.roseau.game.player;

import java.util.List;
import java.util.stream.Collectors;

import org.alexdev.roseau.Roseau;
import org.alexdev.roseau.game.entity.EntityType;
import org.alexdev.roseau.game.inventory.Inventory;
import org.alexdev.roseau.game.entity.Entity;
import org.alexdev.roseau.game.room.Room;
import org.alexdev.roseau.game.room.entity.RoomUser;
import org.alexdev.roseau.messages.OutgoingMessageComposer;
import org.alexdev.roseau.server.IPlayerNetwork;

public class Player implements Entity {

	private String machineId;
	private PlayerDetails details;
	private IPlayerNetwork network;
	private RoomUser roomEntity;
	private Inventory inventory;
	
	private Player createdFlat = null;
	private Room lastCreatedRoom;

	public Player(IPlayerNetwork network) {
		this.network = network;
		this.details = new PlayerDetails(this);
		this.roomEntity = new RoomUser(this);
		this.inventory = new Inventory(this);
		this.lastCreatedRoom = null;
	}
	
	public void login() {
		
	}
	
	public Player getPrivateRoomPlayer() {
		
		try {
			return Roseau.getGame()
					.getPlayerManager()
					.getPlayers()
					.values().stream()
					.filter(s -> s.getDetails().getId() == this.details.getId() && 
					s.getNetwork().getServerPort() == (Roseau.getServerPort() - 1)).findFirst().get();
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public void dispose() {

		if (this.roomEntity != null) {
			if (this.roomEntity.getRoom() != null) {
				this.roomEntity.getRoom().leaveRoom(this, false);
			}
		}
		
		this.inventory.dispose();
	}
	
	public void send(OutgoingMessageComposer response) {
		this.network.send(response);
	}
	
	public void kick() {
		this.network.close();
	}
	
	public void kickAllConnections() {
		
		try {
			List<Player> players = Roseau.getGame().getPlayerManager().getPlayers().values().stream().filter(s -> s.getDetails().getId() == this.details.getId()).collect(Collectors.toList());
			
			for (Player player : players) {
				player.kick();
			}
			
		} catch (Exception e) {
			return;
		}
	}
		
	public void setMachineId(String machineId) {
		this.machineId = machineId;
	}

	public String getMachineId() {
		return machineId;
	}

	public PlayerDetails getDetails() {
		return details;
	}

	public IPlayerNetwork getNetwork() {
		return network;
	}


	public Player getCreatedFlat() {
		return createdFlat;
	}

	public void setCreatedFlat(Player player) {
		this.createdFlat = player;
	}

	public List<Room> getRooms() {
		return Roseau.getDataAccess().getRoom().getPlayerRooms(this.details, true);
	}
	
	@Override
	public EntityType getType() {
		return EntityType.PLAYER;
	}
	
	@Override
	public RoomUser getRoomUser() {
		return this.roomEntity;
	}

	public Room getLastCreatedRoom() {
		return lastCreatedRoom;
	}

	public void setLastCreatedRoom(Room lastCreatedRoom) {
		this.lastCreatedRoom = lastCreatedRoom;
	}

	public Inventory getInventory() {
		return inventory;
	}


}
