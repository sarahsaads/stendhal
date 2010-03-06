/* $Id$ */
package games.stendhal.server.script;

import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.mapstuff.portal.LevelCheckingPortal;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.player.Player;

import java.util.List;

/**
 * Enables admins to create portals.
 * 
 * @author hendrik
 */
public class AdminPortal extends ScriptImpl {


	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() >= 2 && args.size() != 4) {
			try {
				createPortal(admin, args);
			} catch (RuntimeException e) {
				admin.sendPrivateText(e.toString());
			}
		} else {
			// syntax error, print help text
			sandbox.privateText(
					admin,
					"This script creates portals:\n" + 
					"/script AdminPortal.class <destination-zone> <destination-ref>\n" + 
					"/script AdminPortal.class <name> <destination-zone> <destination-ref>\n" + 
					"/script AdminPortal.class <name> <destination-zone> <destination-ref> level <min-level> <max-level> <reject-message>" + 
					"");
		}
	}

	private void createPortal(final Player admin, final List<String> args) {
		sandbox.setZone(sandbox.getZone(admin));
		int x = admin.getX();
		int y = admin.getY();

		Portal portal = instantiatePortal(args);
		setPortalName(args, portal);
		portal.setPosition(x, y);
		int destinationOffset = getDestinationOffset(args);
		portal.setDestination(args.get(destinationOffset), args.get(destinationOffset + 1));

		// add sign to game
		sandbox.add(portal);
	}

	private int getDestinationOffset(List<String> args) {
		if (args.size() > 2) {
			return 1;
		} else {
			return 0;
		}
	}

	private Portal instantiatePortal(List<String> args) {
		if (args.size() < 3) {
			return new Portal();
		} else if (args.get(3).equals("level")) {
			String rejectMessage = null;
			if (args.size() == 7) {
				rejectMessage = args.get(6);
			}
			return new LevelCheckingPortal(Integer.parseInt(args.get(4)), Integer.parseInt(args.get(5)), rejectMessage);
		}
		throw new IllegalArgumentException("Invalid portal type.");
	}

	private void setPortalName(final List<String> args, Portal portal) {
		if (args.size() > 2) {
			portal.setIdentifier(args.get(0));
		}
	}
}
