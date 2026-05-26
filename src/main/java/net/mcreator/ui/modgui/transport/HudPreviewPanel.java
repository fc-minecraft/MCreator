/*
 * MCreator (https://mcreator.net/)
 * Copyright (C) 2012-2020, Pylo
 * Copyright (C) 2020-2021, Pylo, opensource contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.mcreator.ui.modgui.transport;

import net.mcreator.element.types.Transport;
import net.mcreator.ui.init.L10N;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.function.Supplier;

/**
 * WYSIWYG Minecraft HUD preview panel.
 *
 * <ul>
 *   <li>Renders a mock Minecraft screen (sky gradient, crosshair, hotbar).</li>
 *   <li>Draws each {@link Transport.HudElement} at its configured anchor+offset.</li>
 *   <li>Allows selecting an element by clicking on it.</li>
 *   <li>Allows repositioning by dragging.</li>
 *   <li>Shows a warning when a custom overlay is bound.</li>
 * </ul>
 *
 * <p>Drag updates are written directly into {@code el.xOffset} / {@code el.yOffset}
 * and reflected in the spinners via {@link #setSpinnerSyncCallback(SpinnerSyncCallback)}.
 */
public class HudPreviewPanel extends JPanel {

	/** Virtual screen size (matches Minecraft default 854×480 scaled ÷ 2). */
	private static final int VW = 427;
	private static final int VH = 240;

	// ─────────────────────────────────────────────────────────────────────────
	// External state references (no field ownership — no coupling)
	// ─────────────────────────────────────────────────────────────────────────

	private final DefaultListModel<Transport.HudElement> listModel;
	private final Supplier<Transport.HudElement>          selectionSupplier;
	private final Supplier<Boolean>                       overlayActiveSupplier;

	/** Called when a drag changes xOffset/yOffset so spinners can update. */
	private @Nullable SpinnerSyncCallback spinnerSync;

	/** Called when the user clicks an element in the canvas (to sync list selection). */
	private @Nullable ElementClickCallback elementClick;

	// ─────────────────────────────────────────────────────────────────────────
	// Drag state
	// ─────────────────────────────────────────────────────────────────────────

	private @Nullable Transport.HudElement dragEl;
	private @Nullable Point                dragOrigin; // virtual offset within element

	// ─────────────────────────────────────────────────────────────────────────
	// Construction
	// ─────────────────────────────────────────────────────────────────────────

	public HudPreviewPanel(DefaultListModel<Transport.HudElement> listModel,
			Supplier<Transport.HudElement> selectionSupplier,
			Supplier<Boolean>              overlayActiveSupplier) {
		this.listModel             = listModel;
		this.selectionSupplier     = selectionSupplier;
		this.overlayActiveSupplier = overlayActiveSupplier;

		setBackground(new Color(18, 18, 18));
		setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));
		setToolTipText(L10N.t("elementgui.transport.hud.canvas_tooltip"));

		MouseAdapter ma = new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {
				if (Boolean.TRUE.equals(overlayActiveSupplier.get())) return;
				Rectangle r = screenRect();
				if (!r.contains(e.getPoint())) return;
				int vx = toV(e.getX() - r.x, r.width,  VW);
				int vy = toV(e.getY() - r.y, r.height, VH);
				for (int i = listModel.size() - 1; i >= 0; i--) {
					Transport.HudElement el = listModel.get(i);
					Rectangle b = elBounds(el);
					if (b.contains(vx, vy)) {
						dragEl     = el;
						dragOrigin = new Point(vx - b.x, vy - b.y);
						if (elementClick != null) elementClick.onElementClicked(i);
						break;
					}
				}
			}
			@Override public void mouseReleased(MouseEvent e) { dragEl = null; dragOrigin = null; }
			@Override public void mouseDragged(MouseEvent e) {
				if (dragEl == null || dragOrigin == null) return;
				Rectangle r = screenRect();
				int vx = toV(e.getX() - r.x, r.width,  VW) - dragOrigin.x;
				int vy = toV(e.getY() - r.y, r.height, VH) - dragOrigin.y;
				dragEl.xOffset = anchorOffX(dragEl.anchor, vx);
				dragEl.yOffset = anchorOffY(dragEl.anchor, vy);
				if (spinnerSync != null) spinnerSync.sync(dragEl.xOffset, dragEl.yOffset);
				repaint();
			}
		};
		addMouseListener(ma);
		addMouseMotionListener(ma);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Callbacks (set by HudEditorPanel after construction to avoid coupling)
	// ─────────────────────────────────────────────────────────────────────────

	public void setSpinnerSyncCallback(SpinnerSyncCallback cb) { this.spinnerSync  = cb; }
	public void setElementClickCallback(ElementClickCallback cb) { this.elementClick = cb; }

	// ─────────────────────────────────────────────────────────────────────────
	// Paint
	// ─────────────────────────────────────────────────────────────────────────

	@Override protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Rectangle r = screenRect();

		// Outer bg
		g2.setColor(new Color(10, 10, 10));
		g2.fillRect(0, 0, getWidth(), getHeight());

		// Sky gradient
		g2.setPaint(new GradientPaint(r.x, r.y, new Color(30, 60, 120),
				r.x, r.y + r.height, new Color(20, 35, 60)));
		g2.fillRect(r.x, r.y, r.width, r.height);

		// Overlay-bound warning
		if (Boolean.TRUE.equals(overlayActiveSupplier.get())) {
			g2.setColor(new Color(220, 60, 60));
			g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
			String msg = L10N.t("elementgui.transport.hud.overlay_warning");
			FontMetrics fm = g2.getFontMetrics();
			g2.drawString(msg, r.x + (r.width - fm.stringWidth(msg)) / 2, r.y + r.height / 2);
			return;
		}

		// Crosshair
		int cx = r.x + r.width / 2, cy = r.y + r.height / 2;
		g2.setColor(new Color(255, 255, 255, 140));
		g2.drawLine(cx-6, cy, cx+6, cy);
		g2.drawLine(cx, cy-6, cx, cy+6);

		// Hotbar
		int hbW = (int)(r.width * 0.5), hbH = Math.max(8, (int)(r.height * 0.075));
		int hbX = r.x + (r.width - hbW) / 2, hbY = r.y + r.height - hbH - 3;
		g2.setColor(new Color(0, 0, 0, 110));
		g2.fillRoundRect(hbX, hbY, hbW, hbH, 4, 4);
		g2.setColor(new Color(255, 255, 255, 50));
		g2.drawRoundRect(hbX, hbY, hbW, hbH, 4, 4);

		double sx = (double) r.width  / VW;
		double sy = (double) r.height / VH;
		Font monoFont = new Font("Monospaced", Font.PLAIN, Math.max(7, (int)(7.5 * sy)));
		Transport.HudElement sel = selectionSupplier.get();

		for (int i = 0; i < listModel.size(); i++) {
			Transport.HudElement el = listModel.get(i);
			boolean isSel = el == sel;
			drawElement(g2, el, isSel, r, sx, sy, monoFont);
		}

		// Anchor dots
		g2.setColor(new Color(255, 255, 255, 22));
		int[] axs = { r.x, r.x + r.width/2, r.x + r.width  };
		int[] ays = { r.y, r.y + r.height/2, r.y + r.height };
		for (int ax : axs) for (int ay : ays) g2.fillOval(ax-2, ay-2, 4, 4);
	}

	private void drawElement(Graphics2D g2, Transport.HudElement el, boolean selected,
			Rectangle r, double sx, double sy, Font font) {
		Rectangle vb = elBounds(el);
		int ex = r.x + (int)(vb.x * sx);
		int ey = r.y + (int)(vb.y * sy);
		int ew = Math.max(4, (int)(vb.width  * sx));
		int eh = Math.max(4, (int)(vb.height * sy));

		// Background
		g2.setColor(selected ? new Color(255, 165, 0, 45) : new Color(255, 255, 255, 12));
		g2.fillRect(ex, ey, ew, eh);
		g2.setColor(selected ? new Color(255, 165, 0, 210) : new Color(200, 200, 200, 60));
		g2.drawRect(ex, ey, ew, eh);

		Color elColor = el.color != null ? el.color : Color.WHITE;
		g2.setColor(elColor);
		g2.setFont(font);
		FontMetrics fm = g2.getFontMetrics();

		String preview = buildPreviewText(el);

		if ("PROGRESS_BAR".equals(el.type)) {
			// label
			if (preview != null && !preview.isEmpty())
				g2.drawString(preview, ex + 2, ey + fm.getAscent());
			// bar track + fill
			int pad = 2;
			int barY = ey + eh - Math.max(2, (int)(el.barHeight * sy)) - pad;
			int barW = ew - pad * 2;
			int barH = Math.max(2, (int)(el.barHeight * sy));
			g2.setColor(new Color(0, 0, 0, 120));
			g2.fillRect(ex + pad, barY, barW, barH);
			g2.setColor(elColor);
			g2.fillRect(ex + pad, barY, (int)(barW * 0.65), barH);
		} else {
			if (preview != null && !preview.isEmpty())
				g2.drawString(preview, ex + 2, ey + fm.getAscent() + 1);
		}
	}

	private static String buildPreviewText(Transport.HudElement el) {
		String p = el.textContent != null ? el.textContent : "";
		return switch (el.type != null ? el.type : "") {
			case "TEXT"          -> p.isEmpty() ? "<text>" : p;
			case "VEHICLE_VALUE" -> p + switch (el.valueExpression != null ? el.valueExpression : "") {
				case "SPEED"         -> "42 m/s";
				case "FUEL"          -> "67%";
				case "THROTTLE"      -> "80%";
				case "ENGINE_STATUS" -> "ON";
				case "ALTITUDE"      -> "120 m";
				case "HEALTH"        -> "20 ♥";
				default              -> "…";
			};
			case "PROGRESS_BAR"  -> switch (el.valueExpression != null ? el.valueExpression : "") {
				case "FUEL"     -> L10N.t("elementgui.transport.hud.val_fuel");
				case "THROTTLE" -> L10N.t("elementgui.transport.hud.val_throttle");
				case "HEALTH"   -> L10N.t("elementgui.transport.hud.val_health");
				default         -> el.label != null ? el.label : "";
			};
			default -> "";
		};
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Coordinate helpers
	// ─────────────────────────────────────────────────────────────────────────

	private Rectangle screenRect() {
		int pw = getWidth(), ph = getHeight();
		double a = (double) VW / VH;
		int w = pw, h = (int)(pw / a);
		if (h > ph) { h = ph; w = (int)(ph * a); }
		return new Rectangle((pw - w) / 2, (ph - h) / 2, w, h);
	}

	private static int toV(int screen, int screenSpan, int virtual) {
		return (int)((double) screen / screenSpan * virtual);
	}

	/** Absolute virtual top-left from anchor + offset */
	private static int absX(Transport.HudElement el) {
		return switch (el.anchor != null ? el.anchor : "TOP_LEFT") {
			case "TOP_CENTER", "CENTER", "BOTTOM_CENTER" -> VW / 2 + el.xOffset;
			case "TOP_RIGHT",  "RIGHT",  "BOTTOM_RIGHT"  -> VW     + el.xOffset;
			default                                       ->           el.xOffset;
		};
	}
	private static int absY(Transport.HudElement el) {
		return switch (el.anchor != null ? el.anchor : "TOP_LEFT") {
			case "LEFT", "CENTER", "RIGHT"                   -> VH / 2 + el.yOffset;
			case "BOTTOM_LEFT", "BOTTOM_CENTER", "BOTTOM_RIGHT" -> VH     + el.yOffset;
			default                                              ->           el.yOffset;
		};
	}
	private static int anchorOffX(String anchor, int absX) {
		return switch (anchor != null ? anchor : "TOP_LEFT") {
			case "TOP_CENTER", "CENTER", "BOTTOM_CENTER" -> absX - VW / 2;
			case "TOP_RIGHT",  "RIGHT",  "BOTTOM_RIGHT"  -> absX - VW;
			default                                       -> absX;
		};
	}
	private static int anchorOffY(String anchor, int absY) {
		return switch (anchor != null ? anchor : "TOP_LEFT") {
			case "LEFT", "CENTER", "RIGHT"                   -> absY - VH / 2;
			case "BOTTOM_LEFT", "BOTTOM_CENTER", "BOTTOM_RIGHT" -> absY - VH;
			default                                              -> absY;
		};
	}

	private static Rectangle elBounds(Transport.HudElement el) {
		int ew = "PROGRESS_BAR".equals(el.type) ? Math.max(el.barWidth,  8) : 90;
		int eh = "PROGRESS_BAR".equals(el.type) ? Math.max(el.barHeight + 12, 14) : 10;
		return new Rectangle(absX(el), absY(el), ew, eh);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Callback interfaces
	// ─────────────────────────────────────────────────────────────────────────

	@FunctionalInterface public interface SpinnerSyncCallback {
		/** Called when drag changes xOffset/yOffset. */
		void sync(int newX, int newY);
	}

	@FunctionalInterface public interface ElementClickCallback {
		/** Called when the user clicks element at given list index. */
		void onElementClicked(int listIndex);
	}
}
