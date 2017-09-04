package editor.gui.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import editor.database.card.Card;
import editor.database.card.CardLayout;
import editor.gui.MainFrame;
import editor.gui.SettingsDialog;

/**
 * This class represents a panel that shows the images associated with a card if they
 * can be found, or a card-shaped rectangle with its oracle text and a warning if
 * they cannot.
 * 
 * @author Alec Roelke
 */
@SuppressWarnings("serial")
public class CardImagePanel extends JPanel
{
	/**
	 * Aspect ratio of a Magic: The Gathering card.
	 */
	public static final double ASPECT_RATIO = 63.0/88.0;
	
	private class ImageDownloadWorker extends SwingWorker<Void, String>
	{
		private final File img;
		private final URL site;
		
		public ImageDownloadWorker(int multiverseid) throws MalformedURLException
		{
			img = Paths.get(SettingsDialog.getAsString(SettingsDialog.CARD_SCANS), multiverseid + ".jpg").toFile();
			site = new URL(String.join("/", "http://gatherer.wizards.com", "Handlers", "Image.ashx?multiverseid=" + multiverseid + "&type=card"));
		}
		
		@Override
		protected Void doInBackground() throws Exception
		{
			img.getParentFile().mkdirs();
			try (BufferedInputStream in = new BufferedInputStream(site.openStream()))
			{
				try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(img)))
				{
					byte[] data = new byte[1024];
					int x;
					while ((x = in.read(data)) > 0)
						out.write(data, 0, x);
				}
			}
			return null;
		}
		
		@Override
		protected void done()
		{
			loadImages();
		}
		
		@Override
		protected void process(List<String> chunks)
		{
			for (String chunk: chunks)
				System.out.println(chunk);
		}
	}
	
	/**
	 * Card this CardImagePanel should display.
	 */
	private Card card;
	/**
	 * List of images to draw for the card.
	 */
	private List<BufferedImage> faceImages;
	/**
	 * Image of the card this CardImagePanel should display.
	 */
	private BufferedImage image;
	
	/**
	 * Create a new CardImagePanel displaying nothing.
	 */
	public CardImagePanel()
	{
		this(null);
	}
	
	/**
	 * Create a new CardImagePanel displaying the specified card.
	 * 
	 * @param c card to display
	 */
	public CardImagePanel(Card c)
	{
		super(null);
		image = null;
		faceImages = new ArrayList<BufferedImage>();
		setCard(c);
	}
	
	/**
	 * {@inheritDoc}
	 * The preferred size is the largest rectangle that fits the image this CardImagePanel is trying
	 * to draw that fits within the parent container.
	 */
	@Override
	public Dimension getPreferredSize()
	{
		if (getParent() == null)
			return super.getPreferredSize();
		else if (image == null)
			return super.getPreferredSize();
		else
		{
			double aspect = (double)image.getWidth()/(double)image.getHeight();
			return new Dimension((int)(getParent().getHeight()*aspect), getParent().getHeight());
		}
	}
	
	private void loadImages()
	{
		if (card != null)
		{
			faceImages.clear();
			for (int i: card.multiverseid())
			{
				BufferedImage img = null;
				try
				{
					if (i > 0)
					{
						File imageFile = Paths.get(SettingsDialog.getAsString(SettingsDialog.CARD_SCANS), i + ".jpg").toFile();
						if (imageFile.exists())
							img = ImageIO.read(imageFile);
					}
				}
				catch (IOException e)
				{}
				finally
				{
					faceImages.add(img);
				}
			}
			if (getParent() != null)
			{
				getParent().validate();
				repaint();
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * The panel will basically just be the image generated in {@link CardImagePanel#setCard(Card)}
	 * scaled to fit the container.
	 */
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (image != null)
		{
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			double aspectRatio = (double)image.getWidth()/(double)image.getHeight();
			int width = (int)(getHeight()*aspectRatio);
			int height = getHeight();
			if (width > getWidth())
			{
				width = getWidth();
				height = (int)(width/aspectRatio);
			}
			g2.drawImage(image, (getWidth() - width)/2, (getHeight() - height)/2, width, height, null);
		}
	}
	
	/**
	 * Set the bounding box of this CardImagePanel.  This will cause it to refresh its image
	 * to fit inside the new bounding box.
	 */
	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		super.setBounds(x, y, width, height);
		if (card == null || height == 0 || width == 0)
			image = null;
		else
		{
			int h = 0;
			int w = 0;
			for (BufferedImage face: faceImages)
			{
				if (face != null)
				{
					h = Math.max(h, face.getHeight());
					w += face.getWidth();
				}
			}
			if (h == 0)
				h = height;
			w += (int)(h*ASPECT_RATIO*Collections.frequency(faceImages, null));
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics g = image.createGraphics();
			int l = 0;
			for (int i = 0; i < faceImages.size(); i++)
			{
				if (faceImages.get(i) != null)
				{
					g.drawImage(faceImages.get(i), l, (h - faceImages.get(i).getHeight())/2, null);
					l += faceImages.get(i).getWidth();
				}
				else
				{
					int faceWidth = (int)(h*ASPECT_RATIO);
					
					JTextPane missingCardPane = new JTextPane();
					StyledDocument document = (StyledDocument)missingCardPane.getDocument();
					Style textStyle = document.addStyle("text", null);
					StyleConstants.setFontFamily(textStyle, UIManager.getFont("Label.font").getFamily());
					StyleConstants.setFontSize(textStyle, MainFrame.TEXT_SIZE);
					Style reminderStyle = document.addStyle("reminder", textStyle);
					StyleConstants.setItalic(reminderStyle, true);
					card.formatDocument(document, i);
					missingCardPane.setSize(new Dimension(faceWidth - 4, h - 4));
					
					BufferedImage img = new BufferedImage(faceWidth, h, BufferedImage.TYPE_INT_ARGB);
					missingCardPane.paint(img.getGraphics());
					g.drawImage(img, l + 2, 2, null);
					g.setColor(Color.BLACK);
					g.drawRect(l, 0, faceWidth - 1, h - 1);
					
					l += faceWidth;
				}
			}
		}
	}
	
	/**
	 * Set the card to display.  If any of the images associated with the new Card are
	 * missing, they are replaced with card-shaped rectangles containing a warning and
	 * the oracle text of the associated face.
	 * 
	 * @param c card to display
	 */
	public void setCard(Card c)
	{
		boolean already = true;
		if ((card = c) != null)
		{
			for (int i: card.multiverseid())
			{
				if (i > 0 && !Paths.get(SettingsDialog.getAsString(SettingsDialog.CARD_SCANS), i + ".jpg").toFile().exists())
				{
					try
					{
						new ImageDownloadWorker(i).execute();
						already = false;
					}
					catch (MalformedURLException e)
					{}
				}
			}
		}
		if (already)
			loadImages();
	}
}
