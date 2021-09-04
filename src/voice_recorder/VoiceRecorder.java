package voice_recorder;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/*
* 
* @author  AdriiE17
* 
*/

public class VoiceRecorder {

	public static void main(String[] args) {
		
		Frame frame = new Frame();
		
		frame.setVisible(true);
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
	}

}

//------------------------------------------------------------------The class of the frame------------------------------------------------------------

class Frame extends JFrame{

	public Frame() {
		
		setTitle("Voice recorder");
		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		Dimension dimensions = toolkit.getScreenSize();
		
		setBounds(dimensions.width/4, dimensions.height/4, dimensions.width/2, dimensions.height/2);
		
		Panel panel = new Panel();
		
		add(panel);
		
	}
	
	private static final long serialVersionUID = 1L;
	
}


//------------------------------------------------------------------The class of the panel------------------------------------------------------------

class Panel extends JPanel{

	public Panel() {
		
		// I create the buttons
		
		recordButton = new JButton("Record");
		
		stopButton = new JButton("Stop");
		
		saveButton = new JButton("Save");
		
		deleteButton = new JButton("Delete");
		
		
		// This three must not be enabled
		
		stopButton.setEnabled(false);
		
		saveButton.setEnabled(false);
		
		deleteButton.setEnabled(false);
		
		
		add(recordButton);
		
		add(stopButton);
		
		add(saveButton);
		
		add(deleteButton);
		
		
		// What the record button have to do
		
		recordButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				stopButton.setEnabled(true);
				
				recordButton.setEnabled(false);
				
				record = new Record();
				
				record.start();

			}
			
		});
		
		
		// What the stop button have to do
		
		stopButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				saveButton.setEnabled(true);
				
				deleteButton.setEnabled(true);
				
				stopButton.setEnabled(false);
				
				
				// I stop recording and I close the line opened in the method run of the record class
				
				Record.getTheLine().stop();
				
				Record.getTheLine().close();
				
			}
			
		});
		
		
		// What the save button have to do
		
		saveButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				recordButton.setEnabled(true);
				
				saveButton.setEnabled(false);
				
				deleteButton.setEnabled(false);
				
				
				// We use the class FileChooser to let the user save the audio anywhere
				
				JFileChooser FileChooser = new JFileChooser();
				
				FileChooser.setSelectedFile(Record.getFile());
				
				int option = FileChooser.showSaveDialog(FileChooser);
				
				
				// If the user choose a route, the audio is copied at that route (and it is deleted from the src folder). If not, the audio is deleted
				
				if(JFileChooser.APPROVE_OPTION == option) {
					
					Record.setSaveRoute(FileChooser);
					
					
				} else if(JFileChooser.CANCEL_OPTION == option) {
					
					Record.setTimes();
					
					Record.getFile().delete();
					
					JOptionPane.showConfirmDialog(null, "The audio have been deleted.", "Audio deleted", JOptionPane.CLOSED_OPTION, JOptionPane.INFORMATION_MESSAGE);
					
				}
				
				record.interrupt();
				
			}
			
		});
		
		// What the delete button have to do
		
		deleteButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				
				recordButton.setEnabled(true);
				
				saveButton.setEnabled(false);
				
				deleteButton.setEnabled(false);
				
				
				// Just delete the audio from the src folder and then I interrupt the thread
				
				Record.getFile().delete();
				
				JOptionPane.showConfirmDialog(null, "The audio have been deleted.", "Audio deleted", JOptionPane.CLOSED_OPTION, JOptionPane.INFORMATION_MESSAGE);
				
				record.interrupt();
				
				Record.setTimes();
				
			}
			
		});	
		
	}
	
	private JButton recordButton, stopButton, saveButton, deleteButton;
	
	private Record record;
	
	private static final long serialVersionUID = 1L;
	
}


//-------------------------------------------------------------------------Class for recording--------------------------------------------------------------

class Record extends Thread{
	
	public void run() {
		
		// Process for recording audio
		
		// The variable times is used to give the default name of the audio
		
		times++;
		
		AudioFormat AudioFormat = new AudioFormat(96000, 16, 2, true, true);
		
		AudioFileFormat.Type AudioType = AudioFileFormat.Type.WAVE;
		
		DataLine.Info LineInfo = new DataLine.Info(TargetDataLine.class, AudioFormat);
		
		try {
			
			line = (TargetDataLine) AudioSystem.getLine(LineInfo);
			
			line.open();
			
			line.start();
			
			AudioInputStream InputStream = new AudioInputStream(line);
			
			file = new File(numberRecord());
			
			AudioSystem.write(InputStream, AudioType, file);
			
		} catch (LineUnavailableException | IOException e1) {
			
			e1.printStackTrace();
		
		}
		
	}
	
	//-------------------------------------------------------------Method to set the route of the audio-------------------------------------------------
	
	public static void setSaveRoute(JFileChooser f) {
		
		// The process for saving the audio in an specific folder, chosen by the user
		
		JFileChooser FileChooser = f;
		
		ArrayList<Integer> list = new ArrayList<Integer>();
		
		try {
			
			// This will copy inside an array the bytes of the audio recorded, which is in the src folder
			
			FileInputStream inputFile = new FileInputStream(numberRecord());
			
			BufferedInputStream inputBuffer = new BufferedInputStream(inputFile);
			
			boolean end = false;
			
			while(!end) {
				
				int inputByte = inputBuffer.read();
				
				if(inputByte != -1) list.add(inputByte);
				
				if(inputByte == -1) end = true;
								
			}
			
			inputFile.close();
			
			
			// Then I delete the audio recorded located in the src folder
			
			File deleteFile = new File(getFile().getAbsolutePath());
			
			deleteFile.delete();
			
			
			// Finally I recover the audio file stored in the array
			
			FileOutputStream outputFile = new FileOutputStream(FileChooser.getSelectedFile().getAbsolutePath());
			
			BufferedOutputStream outputBuffer = new BufferedOutputStream(outputFile);
			
			Iterator<Integer> iterator = list.iterator();
			
			while(iterator.hasNext()) {
				
				outputBuffer.write((int)iterator.next());
				
			}
			
			outputFile.close();
			
		}catch(IOException e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	// Method that return the audio file
	
	public static File getFile() {
		
		return file;
	}
	
	// Method that return the Line
	
	public static TargetDataLine getTheLine() {
		
		return line;
		
	}
	
	// Method that return the default name of the audio
	
	public static String numberRecord() {
		
		return "record" + times + ".wav";
		
	}
	
	public static void setTimes() {
		
		times--;
		
	}
	
	private static int times;

	private static TargetDataLine line;
	
	private static File file;
	
}
