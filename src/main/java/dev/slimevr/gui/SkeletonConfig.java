package dev.slimevr.gui;

import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

import dev.slimevr.gui.swing.ButtonTimer;
import dev.slimevr.gui.swing.EJBagNoStretch;
import io.eiren.util.StringUtils;
import io.eiren.util.ann.ThreadSafe;
import io.eiren.vr.VRServer;
import io.eiren.vr.processor.HumanSkeleton;

public class SkeletonConfig extends EJBagNoStretch {

	private final VRServer server;
	private final VRServerGUI gui;
	private final AutoBoneWindow autoBone;
	private Map<String, SkeletonLabel> labels = new HashMap<>();

	public SkeletonConfig(VRServer server, VRServerGUI gui) {
		super(false, true);
		this.server = server;
		this.gui = gui;
		this.autoBone = new AutoBoneWindow(server, this);

		setAlignmentY(TOP_ALIGNMENT);
		server.humanPoseProcessor.addSkeletonUpdatedCallback(this::skeletonUpdated);
		skeletonUpdated(null);
	}

	@ThreadSafe
	public void skeletonUpdated(HumanSkeleton newSkeleton) {
		java.awt.EventQueue.invokeLater(() -> {
			removeAll();

			int row = 0;

			/**
			add(new JCheckBox("Extended pelvis model") {{
				addItemListener(new ItemListener() {
				    @Override
				    public void itemStateChanged(ItemEvent e) {
				        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended pelvis model", true);
				        	}
				        } else {
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended pelvis model", false);
				        	}
				        }
				    }
				});
				if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
	        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
	        		setSelected(hswl.getSkeletonConfigBoolean("Extended pelvis model"));
				}
			}}, s(c(0, row, 2), 3, 1));
			row++;
			//*/
			/*
			add(new JCheckBox("Extended knee model") {{
				addItemListener(new ItemListener() {
				    @Override
				    public void itemStateChanged(ItemEvent e) {
				        if(e.getStateChange() == ItemEvent.SELECTED) {//checkbox has been selected
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended knee model", true);
				        	}
				        } else {
				        	if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
				        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
				        		hswl.setSkeletonConfigBoolean("Extended knee model", false);
				        	}
				        }
				    }
				});
				if(newSkeleton != null && newSkeleton instanceof HumanSkeletonWithLegs) {
	        		HumanSkeletonWithLegs hswl = (HumanSkeletonWithLegs) newSkeleton;
	        		setSelected(hswl.getSkeletonConfigBoolean("Extended knee model"));
				}
			}}, s(c(0, row, 2), 3, 1));
			row++;
			//*/

			add(new TimedResetButton("Reset All", "All"), s(c(1, row, 2), 3, 1));
			add(new JButton("Auto") {{
				addMouseListener(new MouseInputAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						autoBone.setVisible(true);
						autoBone.toFront();
					}
				});
			}}, s(c(4, row, 2), 3, 1));
			row++;

			add(new JLabel("Chest"), c(0, row, 2));
			add(new AdjButton("+", "Chest", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Chest"), c(2, row, 2));
			add(new AdjButton("-", "Chest", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Chest"), c(4, row, 2));
			row++;

			add(new JLabel("Waist"), c(0, row, 2));
			add(new AdjButton("+", "Waist", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Waist"), c(2, row, 2));
			add(new AdjButton("-", "Waist", -0.01f), c(3, row, 2));
			add(new TimedResetButton("Reset", "Waist"), c(4, row, 2));
			row++;

			add(new JLabel("Hips width"), c(0, row, 2));
			add(new AdjButton("+", "Hips width", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Hips width"), c(2, row, 2));
			add(new AdjButton("-", "Hips width", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Hips width"), c(4, row, 2));
			row++;

			add(new JLabel("Legs length"), c(0, row, 2));
			add(new AdjButton("+", "Legs length", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Legs length"), c(2, row, 2));
			add(new AdjButton("-", "Legs length", -0.01f), c(3, row, 2));
			add(new TimedResetButton("Reset", "Legs length"), c(4, row, 2));
			row++;

			add(new JLabel("Knee height"), c(0, row, 2));
			add(new AdjButton("+", "Knee height", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Knee height"), c(2, row, 2));
			add(new AdjButton("-", "Knee height", -0.01f), c(3, row, 2));
			add(new TimedResetButton("Reset", "Knee height"), c(4, row, 2));
			row++;

			add(new JLabel("Foot length"), c(0, row, 2));
			add(new AdjButton("+", "Foot length", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Foot length"), c(2, row, 2));
			add(new AdjButton("-", "Foot length", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Foot length"), c(4, row, 2));
			row++;

			add(new JLabel("Foot offset"), c(0, row, 2));
			add(new AdjButton("+", "Foot offset", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Foot offset"), c(2, row, 2));
			add(new AdjButton("-", "Foot offset", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Foot offset"), c(4, row, 2));
			row++;

			add(new JLabel("Head offset"), c(0, row, 2));
			add(new AdjButton("+", "Head", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Head"), c(2, row, 2));
			add(new AdjButton("-", "Head", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Head"), c(4, row, 2));
			row++;

			add(new JLabel("Neck length"), c(0, row, 2));
			add(new AdjButton("+", "Neck", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Neck"), c(2, row, 2));
			add(new AdjButton("-", "Neck", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Neck"), c(4, row, 2));
			row++;

			add(new JLabel("Virtual waist"), c(0, row, 2));
			add(new AdjButton("+", "Virtual waist", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Virtual waist"), c(2, row, 2));
			add(new AdjButton("-", "Virtual waist", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Virtual waist"), c(4, row, 2));
			row++;

			add(new JLabel("Skeleton offset"), c(0, row, 2));
			add(new AdjButton("+", "Skeleton offset", 0.01f), c(1, row, 2));
			add(new SkeletonLabel("Skeleton offset"), c(2, row, 2));
			add(new AdjButton("-", "Skeleton offset", -0.01f), c(3, row, 2));
			add(new ResetButton("Reset", "Skeleton offset"), c(4, row, 2));
			row++;

			gui.refresh();
		});
	}

	@ThreadSafe
	public void refreshAll() {
		java.awt.EventQueue.invokeLater(() -> {
			labels.forEach((joint, label) -> {
				label.setText(StringUtils.prettyNumber(server.humanPoseProcessor.getSkeletonConfig(joint) * 100, 0));
			});
		});
	}

	private void change(String joint, float diff) {
		float current = server.humanPoseProcessor.getSkeletonConfig(joint);
		server.humanPoseProcessor.setSkeletonConfig(joint, current + diff);
		server.saveConfig();
		labels.get(joint).setText(StringUtils.prettyNumber((current + diff) * 100, 0));
	}

	private void reset(String joint) {
		server.humanPoseProcessor.resetSkeletonConfig(joint);
		server.saveConfig();
		if(!"All".equals(joint)) {
			float current = server.humanPoseProcessor.getSkeletonConfig(joint);
			labels.get(joint).setText(StringUtils.prettyNumber((current) * 100, 0));
		} else {
			labels.forEach((jnt, label) -> {
				float current = server.humanPoseProcessor.getSkeletonConfig(jnt);
				label.setText(StringUtils.prettyNumber((current) * 100, 0));
			});
		}
	}

	private class SkeletonLabel extends JLabel {

		public SkeletonLabel(String joint) {
			super(StringUtils.prettyNumber(server.humanPoseProcessor.getSkeletonConfig(joint) * 100, 0));
			labels.put(joint, this);
		}
	}

	private class AdjButton extends JButton {

		public AdjButton(String text, String joint, float diff) {
			super(text);
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					change(joint, diff);
				}
			});
		}
	}

	private class ResetButton extends JButton {

		public ResetButton(String text, String joint) {
			super(text);
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					reset(joint);
				}
			});
		}
	}

	private class TimedResetButton extends JButton {

		public TimedResetButton(String text, String joint) {
			super(text);
			addMouseListener(new MouseInputAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					ButtonTimer.runTimer(TimedResetButton.this, 3, text, () -> reset(joint));
				}
			});
		}
	}
}
