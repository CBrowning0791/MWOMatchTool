import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.*;
import java.net.*;
import javax.swing.*;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;


public class matchTool
{
	public static void main(String args[])
	{
		toolGui tg;
		System.out.println("Reactor Online. \nSensors Online.");
		tg = new toolGui();
		System.out.println("All systems nominal.");
	}
}

class toolGui extends JFrame implements ActionListener
{
	JFrame mainFrame;

	JTextField matchIdField;
	JTextField secretKeyField;

	JPanel textFieldPanel;
	JPanel mapDataPanel;
	JPanel teamOneDataPanel;
	JPanel teamTwoDataPanel;

	JTable dataTable;

	JLabel matchIdLabel;
	JLabel secretKeyLabel;

	JButton lookupButton;

	URL mwoURL;

	InputStream is;
	BufferedReader br;
	BufferedWriter bw;

	String recievedLine;

	File outputFile;
	File secretKeyFile;
	FileWriter fw;
	FileReader fr;

	JTabbedPane dataPane;

	DefaultListModel mapListModel;

	JScrollPane mapScrollPane;

	JList mapListGUI;

	public toolGui()
	{
		///////Creating everything///////

		mainFrame = new JFrame("MWO Match Tool");
		matchIdField = new JTextField(10);
		matchIdLabel = new JLabel("Match Id: ");
		secretKeyLabel = new JLabel("Secret Key: ");
		secretKeyField = new JTextField(10);
		textFieldPanel = new JPanel(new FlowLayout());
		lookupButton = new JButton("Look Up");
		//outputFile = new File("MatchData.txt");
		secretKeyFile = new File("data.data");
		dataPane = new JTabbedPane();
		mapDataPanel = new JPanel(new BorderLayout());
		teamOneDataPanel = new JPanel(new FlowLayout());
		teamTwoDataPanel = new JPanel(new FlowLayout());
		mapListModel = new DefaultListModel();
		mapListGUI = new JList(mapListModel);
		mapScrollPane = new JScrollPane(mapListGUI, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);



		///////Adding stuff to various parts//////
		textFieldPanel.add(matchIdLabel);
		textFieldPanel.add(matchIdField);
		textFieldPanel.add(secretKeyLabel);
		textFieldPanel.add(secretKeyField);

		lookupButton.setActionCommand("LOOK_UP");
		lookupButton.addActionListener(this);

		mapDataPanel.add(mapScrollPane, BorderLayout.CENTER);

		dataPane.addTab("Match Info", mapDataPanel);
		dataPane.addTab("Team One", teamOneDataPanel);
		dataPane.addTab("Team Two", teamTwoDataPanel);

		mainFrame.add(textFieldPanel, BorderLayout.PAGE_START);
		mainFrame.add(lookupButton, BorderLayout.PAGE_END);
		mainFrame.add(dataPane, BorderLayout.CENTER);


		///////Making things visible/////////
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(500,300);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);

		///////Doing Start up stuff////////
		readSecretKey();
	}

	public void actionPerformed(ActionEvent e)
	{
		System.out.println("Action received: " + e.getActionCommand()); //Debugging
		switch(e.getActionCommand())
		{
			case "LOOK_UP":
							String fileName = matchIdField.getText();
							fileName = fileName + ".txt";
							outputFile = new File(fileName);
							writeSecretKey();
							connectToMWO(matchIdField.getText(),secretKeyField.getText());
							writeMatchDetails();
							break;
		}
	}

	public void connectToMWO(String matchId, String secretKey)
	{
		String fullURLString = new String("https://mwomercs.com/api/v1/matches/");
		fullURLString = fullURLString.concat(matchId);
		fullURLString = fullURLString.concat("?api_token=");
		fullURLString = fullURLString.concat(secretKey);
		System.out.println("This is what I am trying to connect to: " + fullURLString);

		try
		{
			mwoURL = new URL(fullURLString);
			is = mwoURL.openStream();
			br = new BufferedReader(new InputStreamReader(is));
			Scanner ds = new Scanner(is);

			readWriteDataOutput(ds);
			//while ((recievedLine = br.readLine()) != null)
			//{
			//	System.out.println(recievedLine);
			//}	
		}
		catch (MalformedURLException mue)
		{
			System.out.println("I caught a malformed URL Exception");
			mue.printStackTrace();
		}
		catch (IOException ioe)
		{
			System.out.println("I caught an IOException");
			ioe.printStackTrace();
		}
		finally
		{
			try
			{
				br.close();
				is.close();
			}
			catch(IOException ioe)
			{
				//Nothing here
			}
		}
	}

	public void readWriteDataOutput(Scanner ds)
	{
			try
			{
				if(!outputFile.exists())
				{
					System.out.println("Output file doesn't exist yet, creating it");
					outputFile.createNewFile();
				}
				fw = new FileWriter(outputFile);
				bw = new BufferedWriter(fw);

				while ((recievedLine = br.readLine()) != null)
				{
					//System.out.println(recievedLine);
					recievedLine = recievedLine.replace("{","");
					recievedLine = recievedLine.replace("}","");
					recievedLine = recievedLine.replace(",","\r\n");
					recievedLine = recievedLine.replace("[","\r\n");
					recievedLine = recievedLine.replace("]","");
					recievedLine = recievedLine.replace("\"","");
					recievedLine = recievedLine.replace(":",": ");
					bw.write(recievedLine);
				}
				bw.close();
			}	
			catch(IOException ioe)
			{
				System.out.println("I caught an IOException");
				ioe.printStackTrace();
			}
	}

	public void readSecretKey()
	{
		try
		{
			if(secretKeyFile.exists())
			{
				fr = new FileReader(secretKeyFile);
				br = new BufferedReader(fr);
				secretKeyField.setText(br.readLine());
				br.close();
				fr.close();
			}
		}
		catch(IOException ioe)
		{
			System.out.println("I caught an IOException when trying to read the previously used secret key");
			ioe.printStackTrace();
		}
	}

	public void writeSecretKey()
	{
		try
		{
			fw = new FileWriter(secretKeyFile);
			fw.write(secretKeyField.getText());
			fw.close();
		}
		catch(IOException ioe)
		{
			System.out.println("I caught an IOException when trying to write the secret key to save it");
			ioe.printStackTrace();
		}
	}

	public void writeMatchDetails()
	{
		String line = new String();
		try
		{
			fr = new FileReader(outputFile);
			br = new BufferedReader(fr);
			do 
			{
				if(line.contains("MatchDetails: "))
					line = line.replace("MatchDetails: ","");
				System.out.println(line);
				mapListModel.addElement(line);
			}while((line = br.readLine()) != null && !line.contains("CompleteTime:"));
			System.out.println(line);
			//writeTeamOne(br);
			br.close();
			fr.close();
		}
		catch(IOException ioe)
		{
			System.out.println("I caught an IOException when attempting to write to the GUI from file.");
			ioe.printStackTrace();
		}
	}
	/*public void writeTeamOne(BufferedReader br)
	{
		Vector teamOneVector;
		String line;
		line = br.readLine();
		System.out.println(line);

	}*/
}