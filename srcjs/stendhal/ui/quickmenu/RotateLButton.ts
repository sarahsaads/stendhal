/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { ButtonBase } from "./ButtonBase";
import { FacingHandler } from "../../util/FacingHandler";


export class RotateLButton extends ButtonBase {

	constructor() {
		super("rotate-l");
	}

	protected override onClick(evt: Event) {
		FacingHandler.get().turnCounterClockwise();
	}
}