package tt.ge.jett.rest;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.JButton;
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
		
		//table.setDefaultRenderer(File.class, new ActionRenderer());
		
		table.setPreferredScrollableViewportSize(new Dimension(700, 150));
        table.setFillsViewportHeight(true);
		
		final JFrame window = new JFrame("Ge.tt");
		final JFileChooser chooser = new JFileChooser();
		JPanel actions = new JPanel();
		
		chooser.setMultiSelectionEnabled(true);
		
		JButton addFile = new JButton("Add file");
		JButton destroyFile = new JButton("Destroy selected files");
		
		addFile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				try {
					int result = chooser.showOpenDialog(window);
					
					if(result == JFileChooser.APPROVE_OPTION) {
						java.io.File[] files = chooser.getSelectedFiles();
						Share share = user.createShare();
						
						for(java.io.File file : files) {
							share.uploadFile(file);
						}
						
						fileModel.setFiles(user.getPool().getFiles());
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
				
				for(int i : selected) {
					files[i] = fileModel.getFile(i);
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
		
		/*java.io.File f = new java.io.File("tmp/mastodon.mp3");
		Map<String, String> headers = new HashMap<String, String>();
		
		headers.put("Content-Length", String.valueOf(f.length()));
		
		InputStream in = new ProgressInputStream(new FileInputStream(f), new ProgressListener() {
			@Override
			public void start() {
				System.out.println("Started");
			}
			
			@Override
			public void progress(long progress, int percent) {
				System.out.println("Progress " + progress + " " + percent);
			}
			
			@Override
			public void end() {
				System.out.println("Ended");
			}
		}, f.length());
		
		Helper.URL_CLIENT.request("PUT", "http://192.168.0.13:8080", 
				new HashMap<String, String>(), in, headers);*/
		
		/*User user = User.login("mirza+test@ge.tt", "x17980", "trkkx27wybbo3whfrp8gf9l2jll3di");
		
		user.connect();
		
		Share share = user.createShare("My Share");
		
		for(int i = 1; i <= 3; i++) {
			java.io.File f = new java.io.File(String.format("tmp/m%s.mp3", i));
			final File file = share.createFile(f.getName());
			
			file.addListener(new FileListener.Adapter() {
				@Override
				public void download() {
					System.out.println(file.getFileid() + " Downlaoded");
				}

				@Override
				public void uploadStart() {
					System.out.println(file.getFileid() + " Started");
				}

				@Override
				public void uploadProgress(long progress, int percent) {
					System.out.println(file.getFileid() + " Progress " + progress +  " -> " + percent);
				}

				@Override
				public void uploadEnd() {
					System.out.println(file.getFileid() + " Ended");
				}
			});
			
			file.upload(f);
		}
		
		/*final File file = share.createFile("m.wmv");
		
		file.addListener(new FileListener.Adapter() {

			@Override
			public void uploadStart() {
				System.out.println(file.getFileid() + " - " + file.getFilename() + " Started");
			}

			@Override
			public void uploadProgress(long progress, int percent) {
				System.out.println(file.getFileid() + " Progress " + progress +  " -> " + percent);
			}

			@Override
			public void uploadEnd() {
				System.out.println(file.getFileid() + " Ended");
			}
		});
		
		file.upload(new java.io.File("tmp/r (1).wmv"));*/
	}
	
	static class FileTableModel extends AbstractTableModel {
		private static final String[] FIELDS = {
			"Filename", "Readystate", "Downloads", "Created"
		};

		private List<File> files;
		
		public void setFiles(List<File> files) {
			this.files = files;
			fireTableDataChanged();
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
				return file.getReadystate();
			case 2:
				return file.getDownloads();
			case 3:
				return file.getCreated();
			//case 4:
			//	return file;
			}
			
			return null;
		}

		@Override
		public String getColumnName(int column) {
			return FIELDS[column];
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			if(columnIndex == FIELDS.length - 1) {
				return File.class;
			}
			
			return super.getColumnClass(columnIndex);
		}
	}
	
	/*static class ActionRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(final JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			
			final File file = (File) value;
			
			JButton destroy = new JButton("Destroy");
			
			destroy.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						file.destroy();
					} catch (IOException e1) {
						return;
					}
					
					FileTableModel model = (FileTableModel) table.getModel();
					
					model.removeFile(file);
				}
			});
			
			return destroy;
		}
	}*/
}
