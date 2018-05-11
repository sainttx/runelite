/*
 * Copyright (c) 2018, Seth <https://github.com/sethtroll>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.grandexchange;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemComposition;
import net.runelite.api.SpriteID;
import net.runelite.client.game.SpriteManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
class GrandExchangeHistoryPanel extends JPanel
{
	private final SpriteManager spriteManager;

	private JPanel container = new JPanel();
	private JPanel historyItemsPanel = new JPanel();

	private GrandExchangeOfferSnapshot[] previousState = new GrandExchangeOfferSnapshot[GrandExchangePanel.MAX_OFFERS];

	GrandExchangeHistoryPanel(SpriteManager spriteManager)
	{
		this.spriteManager = spriteManager;
		init();
	}

	void init()
	{
		setLayout(new BorderLayout());
		container.setLayout(new BorderLayout());

		// Items Panel
		historyItemsPanel.setLayout(new GridLayout(0, 1, 0, 3));
		historyItemsPanel.setBorder(new EmptyBorder(3, 0, 0, 0));

		container.add(historyItemsPanel, BorderLayout.SOUTH);
		add(container, BorderLayout.NORTH);
	}

	void updateHistory(ItemComposition entryItem, BufferedImage itemIcon, GrandExchangeOffer offer, int slot)
	{
		switch (offer.getState())
		{
			case EMPTY:
				if (previousState[slot] != null)
				{
					GrandExchangeOfferSnapshot snapshot = previousState[slot];

					SwingUtilities.invokeLater(() ->
					{
						BufferedImage offerTypeIcon = spriteManager.getSprite(getOfferTypeSprite(snapshot.getState()), 0);
						GrandExchangeHistoryEntry entry = new GrandExchangeHistoryEntry(offerTypeIcon, snapshot.getItemIcon(),
							snapshot.getItem().getName(), snapshot.getQuantity(), snapshot.getPrice());
						historyItemsPanel.add(entry, 0);
					});

					// Clear current offer - claimed and put into history
					previousState[slot] = null;
				}
				break;

			case CANCELLED_BUY:
			case CANCELLED_SELL:
			case BOUGHT:
			case SOLD:
				// Ignore offers that have been cancelled with nothing bought or sold
				if (offer.getQuantitySold() > 0)
				{
					previousState[slot] = new GrandExchangeOfferSnapshot(entryItem, itemIcon, offer.getQuantitySold(),
						offer.getPrice(), offer.getState());
				}
				break;
		}
	}

	private int getOfferTypeSprite(GrandExchangeOfferState state)
	{
		switch (state)
		{
			case CANCELLED_BUY:
			case BUYING:
			case BOUGHT:
				return SpriteID.GE_MAKE_OFFER_BUY;
			case CANCELLED_SELL:
			case SELLING:
			case SOLD:
				return SpriteID.GE_MAKE_OFFER_SELL;
			default:
				throw new IllegalArgumentException("GrandExchangeOfferState " + state + " has no associated sprite");
		}
	}

	@Getter
	@AllArgsConstructor
	private class GrandExchangeOfferSnapshot
	{

		ItemComposition item;
		BufferedImage itemIcon;
		int quantity;
		int price;
		GrandExchangeOfferState state;

	}
}
