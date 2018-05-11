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

import net.runelite.client.util.StackFormatter;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class GrandExchangeHistoryEntry extends JPanel
{
	private static final Dimension ICON_SIZE = new Dimension(32, 32);
	private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	GrandExchangeHistoryEntry(BufferedImage offerTypeIcon, BufferedImage itemImage, String name, int amount, int price)
	{
		BorderLayout layout = new BorderLayout();
		layout.setHgap(5);
		setLayout(layout);
		setToolTipText(name);

		setBorder(new CompoundBorder
			(
				new LineBorder(getBackground().brighter(), 1),
				new EmptyBorder(5, 5, 5, 5)
			));

		// Icons
		JPanel iconPanel = new JPanel(new GridLayout(1, 2));
		iconPanel.setOpaque(false);

		// Buy or sell icon
		JLabel typeIcon = new JLabel();
		typeIcon.setPreferredSize(ICON_SIZE);
		if (offerTypeIcon != null)
		{
			typeIcon.setIcon(new ImageIcon(offerTypeIcon));
		}
		iconPanel.add(typeIcon, BorderLayout.LINE_START);

		// Item Icon
		JLabel itemIcon = new JLabel();
		itemIcon.setPreferredSize(ICON_SIZE);
		if (itemImage != null)
		{
			itemIcon.setIcon(new ImageIcon(itemImage));
		}
		iconPanel.add(itemIcon, BorderLayout.LINE_END);

		add(iconPanel, BorderLayout.LINE_START);

		// Item details panel
		JPanel rightPanel = new JPanel(new GridLayout(4, 1));
		rightPanel.setOpaque(false);

		// Item name
		JLabel itemName = new JLabel();
		itemName.setText(amount + " x " + name);
		rightPanel.add(itemName);

		// Total price
		JLabel totalPriceLabel = new JLabel();
		totalPriceLabel.setText(StackFormatter.formatNumber(price * amount) + " coins");
		totalPriceLabel.setForeground(Color.GREEN);
		rightPanel.add(totalPriceLabel);

		// Price each
		JLabel singlePriceLabel = new JLabel();
		singlePriceLabel.setText("= " + StackFormatter.formatNumber(price) + " each");
		singlePriceLabel.setForeground(Color.yellow);
		rightPanel.add(singlePriceLabel);

		// Time
		JLabel timeLabel = new JLabel();
		timeLabel.setText(TIME_FORMAT.format(new Date()));
		rightPanel.add(timeLabel);

		add(rightPanel, BorderLayout.CENTER);
	}

}
