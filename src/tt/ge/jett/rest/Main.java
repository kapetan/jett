package tt.ge.jett.rest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

public class Main {
	public static void main(String... args) throws IOException, InterruptedException {
		Logger logger = Logger.getLogger("tt.ge.jett");
		Handler handler = new ConsoleHandler();
		
		handler.setLevel(Level.ALL);
		handler.setFormatter(new SimpleFormatter());
		
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.ALL);
		logger.addHandler(handler);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		
		final User user = User.login("mirza+test@ge.tt", "x17980", "trkkx27wybbo3whfrp8gf9l2jll3di");
		
		user.connect();
		
		final FileTableModel fileModel = new FileTableModel();
		final JTable table = new JTable(fileModel);
		
		table.setPreferredScrollableViewportSize(new Dimension(700, 150));
        table.setFillsViewportHeight(true);
		
		final JFrame window = new JFrame("Ge.tt");
		final JFileChooser chooser = new JFileChooser();
		JPanel actions = new JPanel();
		
		chooser.setMultiSelectionEnabled(true);
		
		JButton addFile = new JButton("Add file to");
		final JComboBox box = new JComboBox();
		JButton destroyFile = new JButton("Destroy selected files");
		
		user.addFileListener(new FileProxyListener.Adapter() {
			@Override
			public void uploadStart(File file) {
				fileModel.fileUpdated(file);
			}

			@Override
			public void uploadEnd(File file) {
				fileModel.fileUpdated(file);
			}

			@Override
			public void download(File file, boolean increment) {
				fileModel.fileUpdated(file);
			}
		});
		
		updateComboBox(box, user, user.getShares());
		
		addFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					int result = chooser.showOpenDialog(window);
					
					if(result == JFileChooser.APPROVE_OPTION) {
						java.io.File[] files = chooser.getSelectedFiles();
						//Share share = user.createShare();
						ShareItem item = (ShareItem) box.getSelectedItem();
						Share share = item.getShare();
						
						for(java.io.File file : files) {
							File f = share.uploadFile(file);
							
							fileModel.addFile(f);
						}
						
						updateComboBox(box, user, user.getPool().getShares());
						
						//fileModel.setFiles(user.getPool().getFiles());
					}
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		});
		
		destroyFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] selected = table.getSelectedRows();
				File[] files = new File[selected.length];
				
				for(int i = 0; i < files.length; i++) {
					files[i] = fileModel.getFile(selected[i]);
				}
				
				for(File file : files) {
					try {
						file.destroy();
					} catch (IOException e1) {
						return;
					}
					
					fileModel.removeFile(file);
				}
			}
		});
		
		actions.add(addFile);
		actions.add(box);
		actions.add(destroyFile);
		
		window.add(actions, BorderLayout.NORTH);
		window.add(new JScrollPane(table), BorderLayout.CENTER);
		
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				window.pack();
				window.setVisible(true);
			}
		});
	}
	
	private static void updateComboBox(JComboBox box, User user, List<Share> shares) {
		box.removeAllItems();
		box.addItem(new CreateShareItem(user));
		
		for(Share share : shares) {
			box.addItem(new ShareItem(share));
		}
	}
	
	static class FileTableModel extends AbstractTableModel {
		private static final String[] FIELDS = {
			"Filename", "Share", "State", "Downloads", "Created"
		};

		private List<File> files = new ArrayList<File>();
		
		public void setFiles(List<File> files) {
			this.files = files;
			fireTableDataChanged();
		}
		
		public void fileUpdated(File file) {
			if(files == null) {
				return;
			}
			
			int i = files.indexOf(file);
			
			if(i < 0) {
				return;
			}
			
			fireTableRowsUpdated(i, i);
		}
		
		public void addFile(File file) {
			files.add(file);
			
			fireTableRowsInserted(files.size() - 1, files.size() - 1);
		}
		
		public void removeFile(File file) {
			int i = files.indexOf(file);
			
			files.remove(file);
			fireTableRowsDeleted(i, i);
		}
		
		public File getFile(int row) {
			return files.get(row);
		}
		
		@Override
		public int getRowCount() {
			if(files == null) {
				return 0;
			}
			
			return files.size();
		}

		@Override
		public int getColumnCount() {
			return FIELDS.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			File file = files.get(rowIndex);
			
			switch(columnIndex) {
			case 0:
				return file.getFilename();
			case 1:
				return file.getShare().getGettTitle();
			case 2:
				return file.getReadystate();
			case 3:
				return file.getDownloads();
			case 4:
				return file.getCreated();
			}
			
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return FIELDS[column];
		}
	}
	
	static class ShareItem {
		private Share share;
		
		public ShareItem() {}
		
		public ShareItem(Share share) {
			this.share = share;
		}
		
		public Share getShare() throws IOException {
			return share;
		}
		
		@Override
		public String toString() {
			return share.getGettTitle();
		}
	}
	
	static class CreateShareItem extends ShareItem {
		private User user;
		
		public CreateShareItem(User user) {
			this.user = user;
		}
		
		@Override
		public Share getShare() throws IOException {
			return user.createShare();
		}
		
		@Override
		public String toString() {
			return "New share";
		}
	}
}
