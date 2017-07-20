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
	JLabel matchIdLabel;
	JLabel secretKeyLabel;
	JButton lookupButton;
	URL mwoURL;
	InputStream is;
	BufferedReader br;
	BufferedWriter bw;
	String recievedLine;
	File outputFile;
	FileWriter fw;

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
		outputFile = new File("MatchData.txt");


		///////Adding stuff to various parts//////
		textFieldPanel.add(matchIdLabel);
		textFieldPanel.add(matchIdField);
		textFieldPanel.add(secretKeyLabel);
		textFieldPanel.add(secretKeyField);
		mainFrame.add(textFieldPanel, BorderLayout.PAGE_START);
		mainFrame.add(lookupButton, BorderLayout.PAGE_END);

		lookupButton.setActionCommand("LOOK_UP");
		lookupButton.addActionListener(this);


		///////Making things visible/////////
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(500,300);
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		System.out.println("Action received: " + e.getActionCommand()); //Debugging
		switch(e.getActionCommand())
		{
			case "LOOK_UP":
							connectToMWO(matchIdField.getText(),secretKeyField.getText());
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
				fw = new FileWriter(outputFile,true);
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
}