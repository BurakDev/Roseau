package org.alexdev.roseau.game.item;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.alexdev.roseau.Roseau;
import org.alexdev.roseau.game.entity.Entity;
import org.alexdev.roseau.game.pathfinder.AffectedTile;
import org.alexdev.roseau.game.player.Player;
import org.alexdev.roseau.game.room.Room;
import org.alexdev.roseau.game.room.RoomTile;
import org.alexdev.roseau.game.room.model.Position;
import org.alexdev.roseau.log.Log;
import org.alexdev.roseau.messages.outgoing.ACTIVEOBJECT_UPDATE;
import org.alexdev.roseau.messages.outgoing.SHOWPROGRAM;
import org.alexdev.roseau.messages.outgoing.UPDATEWALLITEM;
import org.alexdev.roseau.server.messages.Response;
import org.alexdev.roseau.server.messages.SerializableObject;

import com.google.common.collect.Lists;

public class Item implements SerializableObject {

	private int ID;
	private int roomID;
	private int targetTeleporterID = 0;

	private Position position;

	private String itemData;
	private String customData;
	private String wallPosition;

	private int definitionID;
	private int ownerID;

	public Item(int ID, int roomID, int ownerID, String x, int y, double z, int rotation, int definitionID, String itemData, String customData) {

		this.ID = ID;
		this.roomID = roomID;
		this.ownerID = ownerID;

		this.definitionID = definitionID;

		this.itemData = itemData;	
		this.customData = customData;

		if (this.getDefinition().getBehaviour().isOnWall()) {
			this.wallPosition = x;
			this.position = new Position(-1, -1);
		} else {
			//this.x = Integer.valueOf(x);
			this.position = new Position(Integer.valueOf(x), y, z);
			this.position.setRotation(rotation);
		}

		this.setTeleporterID();
	}

	private void setTeleporterID() {
		if (this.getDefinition().getBehaviour().isTeleporter()) {
			try {
				this.targetTeleporterID = Integer.valueOf(this.customData);
			} catch (NumberFormatException e) {  }
		}
	}

	@Override
	public void serialise(Response response) {

		if (this.getDefinition().getBehaviour().isInvisible()) {
			return;
		}

		if (this.getDefinition().getBehaviour().isPassiveObject()) {

			response.appendNewArgument(Integer.toString(this.ID));
			response.appendArgument(this.getDefinition().getSprite());
			response.appendArgument(Integer.toString(this.position.getX()));
			response.appendArgument(Integer.toString(this.position.getY()));
			response.appendArgument(Integer.toString((int)this.position.getZ()));
			response.appendArgument(Integer.toString(this.position.getRotation()));
			return;
		}

		if (this.getDefinition().getBehaviour().isOnFloor()) {
			response.appendNewArgument(this.getPacketID());
			response.appendArgument(this.getDefinition().getSprite(), ',');
			response.appendArgument(Integer.toString(this.position.getX()));
			response.appendArgument(Integer.toString(this.position.getY()));
			response.appendArgument(Integer.toString(this.getDefinition().getLength()));
			response.appendArgument(Integer.toString(this.getDefinition().getWidth()));
			response.appendArgument(Integer.toString(this.position.getRotation()));
			response.appendArgument(Integer.toString((int)this.position.getZ()));
			response.appendArgument(this.getDefinition().getColor());
			response.appendArgument(this.getDefinition().getName(), '/');
			response.appendArgument(this.getDefinition().getDescription(), '/');

			if (this.targetTeleporterID > 0) {
				response.appendArgument("extr=", '/');
				response.appendArgument(Integer.toString(this.targetTeleporterID), '/');
			}

			if (this.customData != null && this.getDefinition().getDataClass() != null) {
				response.appendArgument(this.getDefinition().getDataClass(), '/');
				response.appendArgument(this.customData, '/');
			}

			return;
		} 

		if (this.getDefinition().getBehaviour().isOnWall()) {
			response.append(Integer.toString(this.ID));
			response.appendArgument(this.getDefinition().getSprite(), ';');
			response.appendArgument("Alex", ';');
			response.appendArgument(this.wallPosition, ';');
			response.appendNewArgument(this.customData);
			return;
		}
	}

	public List<Position> getAffectedTiles() {

		return AffectedTile.getAffectedTilesAt(
				this.getDefinition().getLength(), 
				this.getDefinition().getWidth(), 
				this.position.getX(), 
				this.position.getY(),
				this.position.getRotation());
	}

	public boolean canWalk(Entity player) {

		boolean tile_valid = false;

		if (this.getDefinition().getBehaviour().isCanSitOnTop()) {
			tile_valid = true;
		}

		if (this.getDefinition().getBehaviour().isCanLayOnTop()) {
			tile_valid = true;
		}

		if (this.getDefinition().getBehaviour().isCanStandOnTop()) {
			tile_valid = true;
		}

		if (this.getDefinition().getBehaviour().isTeleporter()) {
			if (this.getDefinition().getDataClass().equals("DOOROPEN")) {

				if (this.customData.equals("TRUE")) {
					tile_valid = true;
				}
			}
		}

		if (this.getDefinition().getSprite().equals("poolBooth")) {
			tile_valid = true;
		}

		if (this.getDefinition().getSprite().equals("poolEnter")) {
			tile_valid = player.getDetails().getPoolFigure().length() > 0;
		}

		if (this.getDefinition().getSprite().equals("poolExit")) {
			tile_valid = player.getDetails().getPoolFigure().length() > 0;
		}

		return tile_valid; 
	}

	public RoomTile getTileInstance() {
		return this.getRoom().getMapping().getTile(this.position.getX(), this.position.getY());
	}

	public void lockTiles() {
		this.getRoom().getMapping().getTile(this.position.getX(), this.position.getY()).setOverrideLock(true);

		try {
			if (this.customData != null) {
				for (String coordinate : this.customData.split(" ")) {
					int x = Integer.valueOf(coordinate.split(",")[0]);
					int y = Integer.valueOf(coordinate.split(",")[1]);

					this.getRoom().getMapping().getTile(x, y).setOverrideLock(true);
				}
			}
		} catch (NumberFormatException e) {	}
	}

	public void unlockTiles() {
		this.getRoom().getMapping().getTile(this.position.getX(), this.position.getY()).setOverrideLock(false);

		try {
			if (this.customData != null) {
				for (String coordinate : this.customData.split(" ")) {
					int x = Integer.valueOf(coordinate.split(",")[0]);
					int y = Integer.valueOf(coordinate.split(",")[1]);

					this.getRoom().getMapping().getTile(x, y).setOverrideLock(false);
				}
			}
		} catch (NumberFormatException e) {	}
	}

	public void updateEntities() {

		List<Entity> affected_players = Lists.newArrayList();;

		Room room = this.getRoom();

		if (room == null) {
			return;
		}

		for (Entity entity : this.getRoom().getEntities()) {

			if (entity.getRoomUser().getCurrentItem() != null) {
				if (entity.getRoomUser().getCurrentItem().getID() == this.ID) {

					if (!hasEntityCollision(entity.getRoomUser().getPosition().getX(), entity.getRoomUser().getPosition().getY())) {
						Log.println("ITEM DEBUG 1");
						entity.getRoomUser().setCurrentItem(null);
					}

					affected_players.add(entity);
				}
			}

			// Moved item inside a player
			else if (hasEntityCollision(entity.getRoomUser().getPosition().getX(), entity.getRoomUser().getPosition().getY())) {
				Log.println("ITEM DEBUG 2");

				entity.getRoomUser().setCurrentItem(this);
				affected_players.add(entity);
			}
		}

		for (Entity entity : affected_players) {
			entity.getRoomUser().currentItemTrigger();
		}
	}

	private boolean hasEntityCollision(int x, int y) {

		if (this.position.getX() == x && this.position.getY() == y) {
			return true;
		}
		else {
			for (Position tile : this.getAffectedTiles()) {
				if (tile.getX() == x && tile.getY() == y) {
					return true;
				}
			}
		}

		return false;

	}

	public void showProgram(String data) {

		if (this.getRoom() == null) {
			return;
		}

		this.getRoom().send(new SHOWPROGRAM(new String[] { this.itemData, data }));
	}

	public void updateStatus() {

		if (this.getRoom() == null) {
			return;
		}

		if (this.getDefinition().getBehaviour().isOnFloor()) {
			this.getRoom().send(new ACTIVEOBJECT_UPDATE(this)); 
		} else {
			this.getRoom().send(new UPDATEWALLITEM(this)); 
		}
	}

	public void save() {
		Roseau.getDao().getItem().saveItem(this);
	}

	public void delete() {
		Roseau.getDao().getItem().deleteItem(this.ID);
	}

	public String getPacketID() {

		int paddingLength = 11; // The magic number, this just works
		int furnIDLength = String.valueOf(this.ID).length();
		
		// Add the length of the ID, twice
		paddingLength = paddingLength + ((furnIDLength * 2) * 2);
		
		return String.format("%0" + paddingLength + "d", this.ID);

	}

	public int getID() {
		return this.ID;
	}

	public ItemDefinition getDefinition() {
		return Roseau.getGame().getItemManager().getDefinition(this.definitionID);
	}

	public Room getRoom() {

		return Roseau.getGame().getRoomManager().getRoomByID(roomID);
	}

	public String getItemData() {
		return itemData;
	}

	public void setItemData(String itemData) {
		this.itemData = itemData;
	}

	public String getCustomData() {
		return customData;
	}

	public void setCustomData(String customData) {
		this.customData = customData;
		this.setTeleporterID();
	}

	public String getWallPosition() {
		return wallPosition;
	}

	public void setWallPosition(String wallPosition) {
		this.wallPosition = wallPosition;
	}

	public int getRoomID() {
		return roomID;
	}

	public void setRoomID(int roomID) {
		this.roomID = roomID;
	}

	public int getTargetTeleporterID() {
		return targetTeleporterID;
	}

	public void setTargetTeleporterID(int targetTeleporterID) {
		this.targetTeleporterID = targetTeleporterID;
	}

	public Position getPosition() {
		return this.position;
	}

	public void leaveTeleporter(final Player player) {

		if (!this.getDefinition().getBehaviour().isTeleporter()) {
			return;
		}

		if (this.getRoom() == null) {
			return;
		}

		final Item item = this;

		Runnable task = new Runnable() {
			@Override
			public void run() {

				item.setCustomData("TRUE");
				item.updateStatus();

				player.getRoomUser().setCanWalk(true);
				player.getRoomUser().walkTo(item.getPosition().getSquareInFront());
			}
		};

		Roseau.getGame().getScheduler().schedule(task, 1000, TimeUnit.MILLISECONDS);
	}

	public int getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}

}
