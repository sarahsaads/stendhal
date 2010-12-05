/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.group.Group;
import games.stendhal.server.entity.player.GagManager;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

import org.apache.log4j.Logger;

/**
 * handles the management of player groups.
 *
 * @author hendrik
 */
public class GroupManagementAction implements ActionListener {
	private static Logger logger = Logger.getLogger(GroupManagementAction.class);

	/**
	 * registers the trade action
	 */
	public static void register() {
		CommandCenter.register("group_management", new GroupManagementAction());
	}

	/**
	 * processes the requested action.
	 * 
	 * @param player the caller of the action
	 * @param action the action to be performed
	 */
	public void onAction(final Player player, final RPAction action) {

		// vaidate parameters
		String actionStr = action.get("action");
		String params = action.get("params");
		if ((actionStr == null) || (params == null)) {
			logger.warn("missing action attribute in RPAction " + action);
			return;
		}

		new GameEvent(player.getName(), "group", params, actionStr).raise();

		// get target player
		Player targetPlayer = null;
		if (!actionStr.equals("part") && !actionStr.equals("status")) {
			targetPlayer = SingletonRepository.getRuleProcessor().getPlayer(params);
			if (targetPlayer == null) {
				player.sendPrivateText(NotificationType.ERROR, "Player " + params + " is not online");
				return;
			}
		}

		// dispatch sub actions
		if (actionStr.equals("invite")) {
			invite(player, targetPlayer);
		} else if (actionStr.equals("join")) {
			join(player, targetPlayer);
		} else if (actionStr.equals("leader")) {
			leader(player, targetPlayer);
		} else if (actionStr.equals("kick")) {
			kick(player, targetPlayer);
		} else if (actionStr.equals("part")) {
			part(player);
		} else if (actionStr.equals("status")) {
			status(player);
		} else {
			unknown(player, actionStr, params);
		}
	}

	/**
	 * invited a player to join a group
	 *
	 * @param player Player who invites
	 * @param targetPlayer player who is invited
	 */
	private void invite(Player player, Player targetPlayer) {

		// TODO: make checks reusable, some of these checks are used at many places

		if (!player.getChatBucket().checkAndAdd()) {
			return;
		}

		if (GagManager.checkIsGaggedAndInformPlayer(player)) {
			return;
		}

		if (targetPlayer.getIgnore(player.getName()) != null) {
			return;
		}

		if (targetPlayer.getAwayMessage() != null) {
			player.sendPrivateText("Sorry, " + targetPlayer.getName() + " is away.");
			return;
		}

		if (targetPlayer.getGrumpyMessage() != null) {
			player.sendPrivateText(targetPlayer.getName() + " has a closed mind, and is seeking solitude from all but close friends");
			return;
		}

		// check if the target player is already in a group
		Group group = SingletonRepository.getGroupManager().getGroup(targetPlayer.getName());
		if (group != null) {
			player.sendPrivateText(NotificationType.ERROR, targetPlayer.getName() + " is already in a group.");
			return;
		}

		// get group, create it, if it does not exist
		SingletonRepository.getGroupManager().createGroup(player.getName());
		group = SingletonRepository.getGroupManager().getGroup(player.getName());

		// check leader
		if (!group.hasLeader(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "Only the group leader may invite people.");
			return;
		}

		// check if there is space left in the group
		if (group.isFull()) {
			player.sendPrivateText(NotificationType.ERROR, "Your group is already full.");
			return;
		}

		// invite
		group.invite(player, targetPlayer);
		player.sendPrivateText("You have invited " + targetPlayer.getName() + " to join your group.");
	}

	/**
	 * joins a group
	 *
	 * @param player Player who wants to join a group
	 * @param targetPlayer leader of the group
	 */
	private void join(Player player, Player targetPlayer) {

		// check if the target player is already in a group
		Group group = SingletonRepository.getGroupManager().getGroup(targetPlayer.getName());
		if ((group == null) || !group.hasBeenInvited(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "You have not been invited into this group or the invite expired.");
			return;
		}

		// check if there is space left in the group
		if (group.isFull()) {
			player.sendPrivateText(NotificationType.ERROR, "The group is already full.");
			return;
		}

		group.addMember(player.getName());
	}


	/**
	 * changes the leadership of the group
	 *
	 * @param player Player who invites
	 * @param targetPlayer new leader
	 */
	private void leader(Player player, Player targetPlayer) {

		// check if the player is already in a group
		Group group = SingletonRepository.getGroupManager().getGroup(targetPlayer.getName());
		if (group == null) {
			player.sendPrivateText(NotificationType.ERROR, "You are not a member of a group.");
			return;
		}

		// check leader
		group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (!group.hasLeader(player.getName())) {
			player.sendPrivateText(NotificationType.ERROR, "Only the group leader may define a new leader.");
			return;
		}

		// check if the target player is a member of this group
		if (!group.hasMember(targetPlayer.getName())) {
			player.sendPrivateText(NotificationType.ERROR, targetPlayer.getName() + " is not a member of your group.");
			return;
		}

		// set leader
		group.setLeader(targetPlayer.getName());
	}


	/**
	 * leave the group
	 *
	 * @param player player who wants to leave
	 */
	private void part(Player player) {
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group == null) {
			player.sendPrivateText(NotificationType.ERROR, "You are not a member of a group.");
			return;
		}

		group.removeMember(player.getName());
	}


	/**
	 * status report about the group
	 *
	 * @param player player who wants a status report
	 */
	private void status(Player player) {
		Group group = SingletonRepository.getGroupManager().getGroup(player.getName());
		if (group == null) {
			player.sendPrivateText(NotificationType.ERROR, "You are not a member of a group.");
			return;
		}

		group.sendGroupChangeEvent(player);
	}


	private void kick(Player player, Player targetPlayer) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * sends an error messages on invalid an actions
	 * 
	 * @param player Player who executed the action
	 * @param action name of action
	 * @param params params for the action
	 */
	private void unknown(Player player, String action, String params) {
		player.sendPrivateText(NotificationType.ERROR, "Unknown group action: " + action + " with parameters " + params);
	}
}
