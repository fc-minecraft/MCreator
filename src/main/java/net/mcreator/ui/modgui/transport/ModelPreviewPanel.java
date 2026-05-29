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

import com.google.gson.*;
import net.mcreator.ui.laf.themes.Theme;
import net.mcreator.workspace.resources.Model;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.regex.*;

/**
 * Interactive 3D wireframe preview for transport seat placement.
 * <p>
 * Renders the entity model boxes parsed from a Java or JSON model file,
 * draws a red cross at the seat position and a cyan wireframe sitting player.
 * Mouse drag rotates the view, scroll wheel zooms.
 * <p>
 * Call {@link #setSeatOffset(double, double, double)} whenever the spinners change
 * and {@link #loadModel(Model)} when the user picks a model.
 */
public class ModelPreviewPanel extends JPanel {

	// ── Geometry ────────────────────────────────────────────────────────────

	private static class Box3D {
		final float[][] corners = new float[8][3];

		Box3D(float[][] worldCorners) {
			System.arraycopy(worldCorners, 0, corners, 0, 8);
		}

		Box3D(float x, float y, float z, float w, float h, float d) {
			corners[0] = new float[] { x,     y,     z     };
			corners[1] = new float[] { x + w, y,     z     };
			corners[2] = new float[] { x + w, y + h, z     };
			corners[3] = new float[] { x,     y + h, z     };
			corners[4] = new float[] { x,     y,     z + d };
			corners[5] = new float[] { x + w, y,     z + d };
			corners[6] = new float[] { x + w, y + h, z + d };
			corners[7] = new float[] { x,     y + h, z + d };
		}
	}

	// ── State ────────────────────────────────────────────────────────────────

	private final List<Box3D> boxes = new ArrayList<>();
	private float seatX = 0, seatY = 0, seatZ = 0;

	private float yaw   = -0.8f;
	private float pitch =  0.4f;
	private float zoom  =  5.0f;
	private Point lastMouse;

	/** Callback invoked when keyboard nudge changes the seat offset (dx, dy, dz in blocks). */
	private TriDoubleConsumer nudgeCallback;

	// ── Constructor ──────────────────────────────────────────────────────────

	public ModelPreviewPanel() {
		setBackground(new Color(30, 30, 30));
		setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60), 1));
		setPreferredSize(new Dimension(400, 300));
		setFocusable(true);
		setRequestFocusEnabled(true);

		MouseAdapter mouse = new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) { requestFocusInWindow(); lastMouse = e.getPoint(); }
			@Override public void mouseDragged(MouseEvent e) {
				if (lastMouse != null) {
					yaw   -= (e.getX() - lastMouse.x) * 0.01f;
					pitch  = Math.max(-1.4f, Math.min(1.4f, pitch + (e.getY() - lastMouse.y) * 0.01f));
					lastMouse = e.getPoint();
					repaint();
				}
			}
			@Override public void mouseWheelMoved(MouseWheelEvent e) {
				zoom = Math.max(1.0f, Math.min(50.0f, zoom * (e.getWheelRotation() < 0 ? 1.1f : 0.9f)));
				repaint();
			}
		};
		addMouseListener(mouse);
		addMouseMotionListener(mouse);
		addMouseWheelListener(mouse);

		// Keyboard seat nudging
		InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap am = getActionMap();
		bindNudge(im, am, KeyEvent.VK_W,     0,                        0,  0, -0.05);
		bindNudge(im, am, KeyEvent.VK_S,     0,                        0,  0,  0.05);
		bindNudge(im, am, KeyEvent.VK_A,     0,                        0.05, 0, 0);
		bindNudge(im, am, KeyEvent.VK_D,     0,                       -0.05, 0, 0);
		bindNudge(im, am, KeyEvent.VK_SPACE, 0,                        0,  0.05, 0);
		bindNudge(im, am, KeyEvent.VK_Q,     0,                        0, -0.05, 0);
		bindNudge(im, am, KeyEvent.VK_UP,    0,                        0,  0, -0.05);
		bindNudge(im, am, KeyEvent.VK_DOWN,  0,                        0,  0,  0.05);
		bindNudge(im, am, KeyEvent.VK_LEFT,  0,                        0.05, 0, 0);
		bindNudge(im, am, KeyEvent.VK_RIGHT, 0,                       -0.05, 0, 0);
	}

	private void bindNudge(InputMap im, ActionMap am, int vk, int mod, double dx, double dy, double dz) {
		String key = "nudge_" + vk + "_" + mod;
		im.put(KeyStroke.getKeyStroke(vk, mod), key);
		am.put(key, new AbstractAction() {
			@Override public void actionPerformed(ActionEvent e) {
				if (nudgeCallback != null) nudgeCallback.accept(dx, dy, dz);
			}
		});
	}

	// ── Public API ───────────────────────────────────────────────────────────

	/** Register callback called on keyboard nudge. Receives (dx, dy, dz) in blocks. */
	public void setNudgeCallback(TriDoubleConsumer callback) {
		this.nudgeCallback = callback;
	}

	public void setSeatOffset(double x, double y, double z) {
		seatX = (float) x;
		seatY = (float) y;
		seatZ = (float) z;
		repaint();
	}

	public void loadModel(@Nullable Model model) {
		boxes.clear();
		if (model == null || model.getReadableName().equals("Biped") || model.getReadableName().equals("Zombie")) {
			addBipedBoxes();
		} else {
			try {
				File file = model.getFile();
				if (file != null && file.isFile()) {
					String content = new String(Files.readAllBytes(file.toPath()));
					if (file.getName().endsWith(".java"))      parseJavaModel(content);
					else if (file.getName().endsWith(".json")) parseJsonModel(content);
				}
			} catch (Exception ignored) {}
			if (boxes.isEmpty()) boxes.add(new Box3D(-8, -8, -8, 16, 16, 16));
		}
		repaint();
	}

	// ── Default biped boxes ──────────────────────────────────────────────────

	private void addBipedBoxes() {
		boxes.add(new Box3D(-4, -8,  -4, 8, 8,  8));  // Head
		boxes.add(new Box3D(-4,  0,  -2, 8, 12, 4));  // Torso
		boxes.add(new Box3D(-8,  0,  -2, 4, 12, 4));  // Right Arm
		boxes.add(new Box3D( 4,  0,  -2, 4, 12, 4));  // Left Arm
		boxes.add(new Box3D(-4, 12,  -2, 4, 12, 4));  // Right Leg
		boxes.add(new Box3D( 0, 12,  -2, 4, 12, 4));  // Left Leg
	}

	// ── Java model parser ────────────────────────────────────────────────────

	private final Map<String, float[]> boneOffsets   = new HashMap<>();
	private final Map<String, float[]> boneRotations = new HashMap<>();
	private final Map<String, String>  boneParents   = new HashMap<>();

	private void parseJavaModel(String content) {
		boneOffsets.clear();
		boneRotations.clear();
		boneParents.clear();

		Pattern bonePat = Pattern.compile(
				"(?:PartDefinition\\s+)?([a-zA-Z0-9_]+)\\s*=\\s*(?:[a-zA-Z0-9_]+)\\.addOrReplaceChild\\s*\\(");
		Matcher boneM = bonePat.matcher(content);
		while (boneM.find()) {
			String varName = boneM.group(1);
			if (varName.equals("meshdefinition") || varName.equals("partdefinition")) continue;

			String before = content.substring(Math.max(0, boneM.start()), boneM.end());
			Pattern parentPat = Pattern.compile("=\\s*([a-zA-Z0-9_]+)\\.addOrReplaceChild");
			Matcher parentM = parentPat.matcher(before);
			String parentVar = "partdefinition";
			if (parentM.find()) parentVar = parentM.group(1);

			// grab argument block
			int depth = 1, pos = boneM.end();
			while (pos < content.length() && depth > 0) {
				char c = content.charAt(pos++);
				if (c == '(') depth++; else if (c == ')') depth--;
			}
			String args = content.substring(boneM.end(), pos - 1);

			Pattern posePat = Pattern.compile(
					"PartPose\\s*\\.\\s*(ZERO|offsetAndRotation|rotation|offset)\\s*(?:\\(([^)]+)\\))?");
			Matcher poseM = posePat.matcher(args);
			float ox = 0, oy = 0, oz = 0, rx = 0, ry = 0, rz = 0;
			if (poseM.find()) {
				String method = poseM.group(1);
				String poseArgs = poseM.group(2);
				if (poseArgs != null) {
					String[] a = poseArgs.split(",");
					try {
						if ("offset".equals(method) && a.length >= 3) {
							ox = pF(a[0]); oy = pF(a[1]); oz = pF(a[2]);
						} else if ("rotation".equals(method) && a.length >= 3) {
							rx = pF(a[0]); ry = pF(a[1]); rz = pF(a[2]);
						} else if ("offsetAndRotation".equals(method) && a.length >= 6) {
							ox = pF(a[0]); oy = pF(a[1]); oz = pF(a[2]);
							rx = pF(a[3]); ry = pF(a[4]); rz = pF(a[5]);
						}
					} catch (NumberFormatException ignored) {}
				}
			}
			boneOffsets.put(varName, new float[] { ox, oy, oz });
			boneParents.put(varName, parentVar);
			if (rx != 0 || ry != 0 || rz != 0) boneRotations.put(varName, new float[] { rx, ry, rz });
		}

		// addBox pass
		int ms = content.indexOf("createBodyLayer");
		if (ms < 0) ms = 0;
		int bs = content.indexOf('{', ms);
		int me = content.length();
		if (bs >= 0) {
			int d = 1, p = bs + 1;
			while (p < content.length() && d > 0) {
				char c = content.charAt(p++);
				if (c == '{') d++; else if (c == '}') d--;
			}
			me = p;
		}
		String body = content.substring(Math.max(0, bs), Math.min(content.length(), me));

		Pattern assignPat = Pattern.compile("\\b([a-zA-Z0-9_]+)\\s*=\\s*[a-zA-Z0-9_]+\\.(?:addOrReplaceChild|addChild)");
		Pattern boxPat = Pattern.compile(
				"addBox\\(\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?\\s*," +
				"\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?\\s*,\\s*([-0-9.Ee]+)F?");

		String currentBone = null;
		for (String stmt : body.split(";")) {
			stmt = stmt.trim();
			Matcher am = assignPat.matcher(stmt);
			if (am.find()) {
				String v = am.group(1);
				if (boneOffsets.containsKey(v)) currentBone = v;
			}
			if (stmt.contains("addBox")) {
				Matcher bm = boxPat.matcher(stmt);
				while (bm.find()) {
					try {
						float x = pF(bm.group(1)), y = pF(bm.group(2)), z = pF(bm.group(3));
						float w = pF(bm.group(4)), h = pF(bm.group(5)), dd = pF(bm.group(6));
						float[][] lc = {
							{x,y,z},{x+w,y,z},{x+w,y+h,z},{x,y+h,z},
							{x,y,z+dd},{x+w,y,z+dd},{x+w,y+h,z+dd},{x,y+h,z+dd}
						};
						float[][] wc = new float[8][3];
						for (int i = 0; i < 8; i++) wc[i] = toWorld(lc[i], currentBone);
						boxes.add(new Box3D(wc));
					} catch (NumberFormatException ignored) {}
				}
			}
		}
	}

	private static float pF(String s) { return Float.parseFloat(s.trim().replaceAll("(?i)f$", "")); }

	private float[] toWorld(float[] pos, String bone) {
		if (bone == null || "partdefinition".equals(bone) || "meshdefinition".equals(bone)) return pos;
		float[] r = boneRotations.containsKey(bone) ? rotate(pos, boneRotations.get(bone)) : pos;
		float[] o = boneOffsets.getOrDefault(bone, new float[3]);
		return toWorld(new float[] { r[0]+o[0], r[1]+o[1], r[2]+o[2] }, boneParents.get(bone));
	}

	private static float[] rotate(float[] p, float[] rot) {
		float x = p[0], y = p[1], z = p[2];
		float cz = (float) Math.cos(rot[2]), sz = (float) Math.sin(rot[2]);
		float x1 = x*cz - y*sz, y1 = x*sz + y*cz, z1 = z;
		float cy = (float) Math.cos(rot[1]), sy = (float) Math.sin(rot[1]);
		float x2 = x1*cy + z1*sy, y2 = y1, z2 = -x1*sy + z1*cy;
		float cx = (float) Math.cos(rot[0]), sx = (float) Math.sin(rot[0]);
		return new float[] { x2, y2*cx - z2*sx, y2*sx + z2*cx };
	}

	// ── JSON model parser ────────────────────────────────────────────────────

	private void parseJsonModel(String content) {
		try {
			JsonObject json = JsonParser.parseString(content).getAsJsonObject();
			if (!json.has("elements")) return;
			for (JsonElement el : json.getAsJsonArray("elements")) {
				JsonObject obj = el.getAsJsonObject();
				JsonArray from = obj.getAsJsonArray("from"), to = obj.getAsJsonArray("to");
				float x1 = from.get(0).getAsFloat(), y1 = from.get(1).getAsFloat(), z1 = from.get(2).getAsFloat();
				float x2 = to.get(0).getAsFloat(),   y2 = to.get(1).getAsFloat(),   z2 = to.get(2).getAsFloat();
				boxes.add(new Box3D(x1-8f, 24f-y2, z1-8f, x2-x1, y2-y1, z2-z1));
			}
		} catch (Exception ignored) {}
	}

	// ── Rendering ────────────────────────────────────────────────────────────

	@Override protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int w = getWidth(), h = getHeight();

		// Floor grid
		g2.setColor(new Color(60, 60, 60));
		for (int i = -4; i <= 4; i++) {
			line3D(g2, i*16f, 24f, -64f, i*16f, 24f, 64f, w, h);
			line3D(g2, -64f, 24f, i*16f, 64f, 24f, i*16f, w, h);
		}

		// Model wireframe
		g2.setColor(Theme.current().getForegroundColor());
		for (Box3D b : boxes) box3D(g2, b, w, h);

		// Seat marker (red cross)
		float sx = -seatX * 16f, sy = 24f - seatY * 16f, sz = -seatZ * 16f;
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(2));
		line3D(g2, sx-4, sy, sz, sx+4, sy, sz, w, h);
		line3D(g2, sx, sy-4, sz, sx, sy+4, sz, w, h);
		line3D(g2, sx, sy, sz-4, sx, sy, sz+4, w, h);

		// Sitting player (cyan)
		g2.setColor(new Color(0, 180, 220));
		g2.setStroke(new BasicStroke(1));
		box3D(g2, new Box3D(sx-2,    sy-14, sz-2,   4,  4, 4), w, h); // Head
		box3D(g2, new Box3D(sx-2.5f, sy-10, sz-1.5f,5,  6, 3), w, h); // Torso
		box3D(g2, new Box3D(sx-2.5f, sy-4,  sz-6,   5,  2, 6), w, h); // Thighs
		box3D(g2, new Box3D(sx-2.5f, sy-2,  sz-6,   5,  6, 2), w, h); // Shins
	}

	private void box3D(Graphics2D g2, Box3D b, int w, int h) {
		Point[] p = new Point[8];
		for (int i = 0; i < 8; i++) p[i] = project(b.corners[i][0], b.corners[i][1], b.corners[i][2], w, h);
		g2.setColor(new Color(150, 150, 150, 20));
		fillFace(g2, p[0], p[1], p[2], p[3]);
		fillFace(g2, p[4], p[5], p[6], p[7]);
		fillFace(g2, p[0], p[4], p[7], p[3]);
		fillFace(g2, p[1], p[5], p[6], p[2]);
		fillFace(g2, p[0], p[1], p[5], p[4]);
		fillFace(g2, p[3], p[2], p[6], p[7]);
		g2.setColor(Theme.current().getForegroundColor());
		edge(g2, p[0], p[1]); edge(g2, p[1], p[2]); edge(g2, p[2], p[3]); edge(g2, p[3], p[0]);
		edge(g2, p[4], p[5]); edge(g2, p[5], p[6]); edge(g2, p[6], p[7]); edge(g2, p[7], p[4]);
		edge(g2, p[0], p[4]); edge(g2, p[1], p[5]); edge(g2, p[2], p[6]); edge(g2, p[3], p[7]);
	}

	private void fillFace(Graphics2D g2, Point a, Point b, Point c, Point d) {
		if (a == null || b == null || c == null || d == null) return;
		g2.fillPolygon(new int[]{a.x,b.x,c.x,d.x}, new int[]{a.y,b.y,c.y,d.y}, 4);
	}

	private void edge(Graphics2D g2, Point a, Point b) {
		if (a != null && b != null) g2.drawLine(a.x, a.y, b.x, b.y);
	}

	private void line3D(Graphics2D g2, float x1, float y1, float z1, float x2, float y2, float z2, int w, int h) {
		Point p1 = project(x1, y1, z1, w, h), p2 = project(x2, y2, z2, w, h);
		if (p1 != null && p2 != null) g2.drawLine(p1.x, p1.y, p2.x, p2.y);
	}

	private Point project(float x, float y, float z, int w, int h) {
		float cy = y - 12f, cz = z;
		float rotX = (float)(x * Math.cos(yaw) - cz * Math.sin(yaw));
		float rotZ = (float)(x * Math.sin(yaw) + cz * Math.cos(yaw));
		float rotY = (float)(cy * Math.cos(pitch) - rotZ * Math.sin(pitch));
		float pZ   = (float)(cy * Math.sin(pitch) + rotZ * Math.cos(pitch));
		float f = 120f / (120f + pZ);
		return new Point((int)(w/2 + rotX * zoom * f), (int)(h/2 + rotY * zoom * f));
	}

	// ── Callback interface ───────────────────────────────────────────────────

	@FunctionalInterface
	public interface TriDoubleConsumer {
		void accept(double dx, double dy, double dz);
	}
}
