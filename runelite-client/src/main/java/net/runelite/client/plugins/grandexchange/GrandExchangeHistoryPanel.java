/*
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
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
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GrandExchangeOffer;
import net.runelite.api.GrandExchangeOfferState;
import net.runelite.api.ItemComposition;
import net.runelite.api.SpriteID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.PluginErrorPanel;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

public class GrandExchangeHistoryPanel extends JPanel
{
	private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String OFFERS_PANEL = "HISTORY_PANEL";

	private final GridBagConstraints constraints = new GridBagConstraints();
	private final CardLayout cardLayout = new CardLayout();

	/*  The offers container, this will hold all the individual ge offers panels */
	private final JPanel historyPanel = new JPanel();

	/*  The error panel, this displays an error message */
	private final PluginErrorPanel errorPanel = new PluginErrorPanel();

	/*  The center panel, this holds either the error panel or the offers container */
	private final JPanel container = new JPanel(cardLayout);

	private final Client client;
	private final ItemManager itemManager;
	private final ScheduledExecutorService executor;

	// TODO: If we want pages, this should be sorted set by date of non-swing components (just information
	// about the history item), and then have a method to draw current page of offers
	private final List<GrandExchangeHistoryEntry> historyEntries = new ArrayList<>();

	private final GrandExchangeOfferSnapshot[] previousState = new GrandExchangeOfferSnapshot[Constants.MAX_GRAND_EXCHANGE_OFFERS];

	public GrandExchangeHistoryPanel(Client client, ItemManager itemManager, ScheduledExecutorService executor)
	{
		this.client = client;
		this.itemManager = itemManager;
		this.executor = executor;
		init();
	}

	void init()
	{
		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.weightx = 1;
		constraints.gridx = 0;
		constraints.gridy = 0;

		/* This panel wraps the offers panel and limits its height */
		JPanel offersWrapper = new JPanel(new BorderLayout());
		offersWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		offersWrapper.add(historyPanel, BorderLayout.NORTH);

		historyPanel.setLayout(new GridBagLayout());
		historyPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		historyPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		/* This panel wraps the error panel and limits its height */
		JPanel errorWrapper = new JPanel(new BorderLayout());
		errorWrapper.setBackground(ColorScheme.DARK_GRAY_COLOR);
		errorWrapper.add(errorPanel, BorderLayout.NORTH);

		errorPanel.setBorder(new EmptyBorder(50, 20, 20, 20));
		// TODO: Log into RuneLite error message
		errorPanel.setContent("No history available", "No grand exchange offers were found on your account.");

		container.add(offersWrapper, OFFERS_PANEL);
		container.add(errorWrapper, ERROR_PANEL);

		add(container, BorderLayout.CENTER);

		resetHistory();
	}

	void resetHistory()
	{
		historyPanel.removeAll();
		historyEntries.clear();
		updateEmptyHistoryPanel();
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
						/*BufferedImage offerTypeIcon = spriteManager.getSprite(getOfferTypeSprite(snapshot.getState()), 0);
						GrandExchangeHistoryEntry entry = new GrandExchangeHistoryEntry(offerTypeIcon, snapshot.getItemIcon(),
								snapshot.getItem().getName(), snapshot.getQuantity(), snapshot.getPrice());
						// TODO: add to current page, remove last entry on page if first page
						//historyItemsPanel.add(entry, 0);
						*/
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

	void updateOffer(ItemComposition item, BufferedImage itemImage, GrandExchangeOffer newOffer, int slot)
	{
		/* If slot was previously filled, and is now empty, remove it from the list */
		if (newOffer == null || newOffer.getState() == GrandExchangeOfferState.EMPTY)
		{
			if (offerSlotPanels[slot] != null)
			{
				offerPanel.remove(offerSlotPanels[slot]);
				offerSlotPanels[slot] = null;
				revalidate();
				repaint();
			}

			removeTopMargin();
			updateEmptyOffersPanel();
			return;
		}

		/* If slot was empty, and is now filled, add it to the list */
		if (offerSlotPanels[slot] == null)
		{
			GrandExchangeOfferSlot newSlot = new GrandExchangeOfferSlot();
			offerSlotPanels[slot] = newSlot;
			offerPanel.add(newSlot, constraints);
			constraints.gridy++;
		}

		offerSlotPanels[slot].updateOffer(item, itemImage, newOffer);

		removeTopMargin();

		revalidate();
		repaint();

		updateEmptyOffersPanel();
	}

	/**
	 * Reset the border for the first offer slot.
	 */
	private void removeTopMargin()
	{

		if (offerPanel.getComponentCount() <= 0)
		{
			return;
		}

		JPanel firstItem = (JPanel) offerPanel.getComponent(0);
		firstItem.setBorder(null);
	}

	/**
	 * If no history is available, this method shows the error panel
	 */
	private void updateEmptyHistoryPanel()
	{
		if (historyEntries.isEmpty())
		{
			historyPanel.removeAll();
			cardLayout.show(container, ERROR_PANEL);
		}
		else
		{
			cardLayout.show(container, OFFERS_PANEL);
		}

	}

}
