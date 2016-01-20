import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

public class mainVison extends JLabel{
	
	private static final long serialVersionUID = 1L;  
	
	private static final String BASE_FOLDER_FOR_ICON_IMAGES = "iconImages";
    private static final String BASE_FOLDER_FOR_URLINI = "ipUrl.ini";
	
	static JFrame frame = null;
	static Dimension windowSize;
	static JLabel jLabel = new JLabel();
	static BufferedImage resizedImage;
	static ImageIcon imageIcon;
	static Graphics2D g;
	static int widhtFrame = 640;
	static int heightFrame = 480;
	static ServerSocket serverSocket = null;
	static Socket clientSocket = null;
    static PrintWriter out = null; 
    static InputStream is = null;
    static BufferedReader in = null;
    static int widthImgRec;
    static int heightImgRec;
    static float xScale;
    static float yScale;
    static Mat mat;
    static String line;
    static int lengthImage;
    static byte[] data;
    static String duneGps;
    static Mat matResize;
    static Size size;
    static BufferedImage temp;
    static JPanel config;
    static JTextField txtText;
    static JTextField txtData;
    static JTextField txtDataTcp;
    static JFrame menu;
    static JCheckBox saveToDiskCheckBox;
    static String info;
    static JPopupMenu popup;
    static boolean isRunningRasPiCam = false;
	static boolean mainClose = false;
	static boolean isRunningIpCam = false;
	static boolean closeCom = false;
	static boolean stateRasPiCom = false;
	static boolean stateIpCamInic = false;
	static String camRtpsUrl = "rtsp://10.0.20.207:554/live/ch01_0";
	
	//Flag for IpCam Ip Check
	static boolean statePingOk = false;
    //JPanel for color state of ping to host ipcam
	static JPanel colorStateIpCam;
    //JFrame for IpCam Select
	static JFrame ipCamPing = new JFrame("Select IpCam");
    //JPanel for IpCam Select (MigLayout)
	static JPanel ipCamCheck = new JPanel(new MigLayout());
    //JButton to confirm ipcam
	static JButton selectIpCam;
    //JComboBox por list of ipcam in ipUrl.ini
    @SuppressWarnings("rawtypes")
    static JComboBox ipCamList;
    //row select from string matrix of IpCam List
    static int rowSelect;
    //JLabel for text ipCam Ping
    static JLabel jlabel;
    //Dimension of Desktop Screen
    static Dimension dim;
    //Flag to enable/disable zoom 
    static boolean zoomMask = false;
    //JPanel for zoom point
    static JPanel zoomImg = new JPanel();
    //Buffer image for zoom Img Cut
    static BufferedImage zoomImgCut;
    //JLabel to show zoom image
    static JLabel zoomLabel = new JLabel();
    //Graphics2D for zoom image scaling
    static Graphics2D graphics2D;
    //BufferedImage for zoom image scaling
    static BufferedImage scaledCutImage;
    //PopPup zoom Image
    static JPopupMenu popupzoom;
    //coord x for zoom
    static int zoomX = 100;
    //coord y for zoom
    static int zoomY = 100;
    
    static JLabel jLabelCutTpl = new JLabel();
    static JFrame showImageResultTpl = new JFrame("TPL");
    
    static JLabel jLabelCutWindow = new JLabel();
    static JFrame showImageResultWindow = new JFrame("Window");
    
    static Mat tpl;
    static Mat src;
    static Mat result;
    static BufferedImage imgCutTpl;
    static BufferedImage imgCutWindow;
    static int mouseTplX = 100;
    static int mouseTplY = 100;
    static boolean isTracking = false;
    static int counterRefreshTpl = 0;
    static int tplFoundX = 0;
    static int tplFoundY = 0;
    static int nFramesToRefresh = 20;
    
    static int lastPosWindowX;
    static int lastPosWindowY;
	
    static int sizeTpl = 60;
    static int sizeWindowSearch = sizeTpl * 2;
    
    
    
    
    // Create a constructor method  
    public mainVison(){
      super();
    } 

    
    //!Read ipUrl.ini to find IpCam ON
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void checkIpCam(){
        String dataUrlIni[][];
        dataUrlIni = readIpUrl();
        int sizeDataUrl = dataUrlIni.length;
        String nameIpCam[] = new String[sizeDataUrl];
        for (int i=0; i < sizeDataUrl; i++)
            nameIpCam[i] = dataUrlIni[i][0];
        
        ipCamPing = new JFrame("Select IpCam");
        ipCamPing.setSize(400, 80);
        ipCamPing.setLocation(dim.width/2-ipCamPing.getSize().width/2, dim.height/2-ipCamPing.getSize().height/2);
        ipCamCheck = new JPanel(new MigLayout());
        ImageIcon imgIpCam = new ImageIcon(String.format(BASE_FOLDER_FOR_ICON_IMAGES + "/ipcam.png"));
        ipCamPing.setIconImage(imgIpCam.getImage());
        ipCamPing.setResizable(false);
        ipCamPing.setBackground(Color.GRAY);                  
        ipCamList = new JComboBox(nameIpCam);
        ipCamList.setSelectedIndex(0);
        ipCamList.addActionListener(new ActionListener(){

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                rowSelect = cb.getSelectedIndex();
                if(rowSelect != 0){
                    if(pingIpCam(rowSelect,dataUrlIni[rowSelect][1])){
                        camRtpsUrl = dataUrlIni[rowSelect][2];
                        colorStateIpCam.setBackground(Color.GREEN);
                        jlabel.setText("ON");
                    }
                    else{
                        colorStateIpCam.setBackground(Color.RED);
                        jlabel.setText("OFF");
                    }   
                }
                else{
                    statePingOk = false;
                    colorStateIpCam.setBackground(Color.RED);
                    jlabel.setText("OFF");
                }
            }
        });
        ipCamCheck.add(ipCamList,"span, split 3, center");
        
        colorStateIpCam = new JPanel();
        jlabel = new JLabel("OFF");
        jlabel.setFont(new Font("Verdana",1,14));
        colorStateIpCam.setBackground(Color.RED);
        colorStateIpCam.add(jlabel);
        ipCamCheck.add(colorStateIpCam,"h 30!, w 30!");
        
        selectIpCam = new JButton("Select IpCam", imgIpCam);
        selectIpCam.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                if(statePingOk){
                	ipCamPing.setVisible(false);
                    System.out.println("IpCam Select: "+dataUrlIni[rowSelect][0]);
                    isRunningIpCam = true;
                    closeCom = false;
                    stateRasPiCom = true;
                }
            }
        });
        ipCamCheck.add(selectIpCam,"h 30!");
        
        ipCamPing.add(ipCamCheck);
        ipCamPing.setVisible(true);             
    }
    
    //!Ping CamIp
    private static boolean pingIpCam (int id, String host){
        boolean ping = false;
        try {
        	String cmd = "";
            if(System.getProperty("os.name").startsWith("Windows")) {   
                    // For Windows
                    cmd = "ping -n 1 " + host;
            } else {
                    // For Linux and OSX
                    cmd = "ping -c 1 " + host;
            }
            Process myProcess = Runtime.getRuntime().exec(cmd);
            try {
				myProcess.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
            if(myProcess.exitValue() == 0)
            	ping = true;
            else
            	ping = false;
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        } //Ping doesnt work 
        
        statePingOk = ping;
        return ping;
    }
    
    //!Read file
    private static String[][] readIpUrl(){
        //Open the file for reading and split (#)
        BufferedReader br = null;
        String lineFile;
        String[] splits;
        String[][] dataSplit = null;
        int cntReader = 0;
        try {
            br = new BufferedReader(new FileReader(BASE_FOLDER_FOR_URLINI));
            while ((lineFile = br.readLine()) != null)
                cntReader++;
            
            br.close();
            br = new BufferedReader(new FileReader(BASE_FOLDER_FOR_URLINI));
            
            dataSplit = new String[cntReader+1][3];
            cntReader = 1;
            dataSplit[0][0] = "IpCam Select";
            while ((lineFile = br.readLine()) != null) {
                splits = lineFile.split("#");
                dataSplit[cntReader][0] = splits[0];
                dataSplit[cntReader][1] = splits[1];
                dataSplit[cntReader][2] = splits[2];
                cntReader++;
            }
        }
        catch (IOException e) {
           System.err.println("Error: " + e);
        }
        try {
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return dataSplit;
    }
    
    //!Inic Layout
    public static void layoutIni(){
    	dim = Toolkit.getDefaultToolkit().getScreenSize();
    	matResize = new Mat(heightFrame, widhtFrame, CvType.CV_8UC3);
    	//Display in JFrame
    	frame = new JFrame("Main Frame");
    	frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	frame.setResizable(true);
    	frame.setSize(widhtFrame, heightFrame);
    	frame.addComponentListener(new ComponentAdapter() {  
    		public void componentResized(ComponentEvent evt) {
    			Component c = evt.getComponent();
    		    widhtFrame = c.getSize().width;
    		    heightFrame = c.getSize().height;
    		    //System.out.println(c.getSize());  		    
    		    xScale = (float)widhtFrame/widthImgRec;
    	        yScale = (float)heightFrame/heightImgRec;
    	        if(!isRunningRasPiCam && isRunningIpCam)
    	        	inicImage();
    		}
    	});
    	frame.setVisible(true);
    	
    	//!Detect key-pressed
    	frame.addKeyListener(new KeyListener() {            
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyChar() == 'z' && zoomMask){
                    zoomMask = false;
                    //TODO
                    popupzoom.setVisible(false);
                }
            }
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyChar() == 'z' && !zoomMask){
                    zoomMask = true;
                    popupzoom = new JPopupMenu();
                    popupzoom.setSize(300, 300);
                    popupzoom.setVisible(true);
                    popupzoom.add(zoomImg);
                }
                else if(e.getKeyChar() == 'i')
                    checkIpCam();
                else if(e.getKeyChar() == 'c')
                    menu.setVisible(true);
                if(zoomMask){
                    int xLocMouse = MouseInfo.getPointerInfo().getLocation().x - frame.getLocation().x;
                    int yLocMouse = MouseInfo.getPointerInfo().getLocation().y - frame.getLocation().y - 11;
                    
                    if(xLocMouse < 0)
                        xLocMouse = 0;
                    if(yLocMouse < 0)
                        yLocMouse = 0;
                    
                    if(xLocMouse + 52 < frame.getSize().getWidth() && xLocMouse - 52 > 0 && yLocMouse + 60 < frame.getSize().getHeight() && yLocMouse - 60 > 0){
                        zoomX = xLocMouse;
                        zoomY = yLocMouse;
                        popupzoom.setLocation(MouseInfo.getPointerInfo().getLocation().x - 150, MouseInfo.getPointerInfo().getLocation().y - 150);
                        getCutImage(resizedImage, zoomX, zoomY);
                        
                    }
                    else
                        popupzoom.setVisible(false);
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {
                
            }
        });
    	frame.setFocusable(true);
    	
    	size = new Size(widhtFrame, heightFrame);
    	    	
    	//Mouse click
    	frame.addMouseListener(new MouseListener() {
    		public void mousePressed(MouseEvent e) {
    		}

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
                    popup = new JPopupMenu();
                    @SuppressWarnings("unused")
					JMenuItem item1;
                    popup.add(item1 = new JMenuItem("Start RasPiCam", new ImageIcon(String.format(BASE_FOLDER_FOR_ICON_IMAGES + "/raspicam.jpg")))).addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            isRunningRasPiCam = true;
                            //isRunningIpCam = false;
                            closeCom = false;
                            stateIpCamInic = true;
                            isTracking = false;
                        }
                    });
                    @SuppressWarnings("unused")
					JMenuItem item2;
                    popup.add(item2 = new JMenuItem("Close all connection", new ImageIcon(String.format(BASE_FOLDER_FOR_ICON_IMAGES + "/close.gif")))).addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                        //	isRunningRasPiCam = false;
                        //	isRunningIpCam = false;
                        	System.out.println("Clossing all Video Stream...");
                        	closeCom = true;
                        	isTracking = false;

                        }
                    });
                    @SuppressWarnings("unused")
					JMenuItem item3;  
                    popup.add(item3 = new JMenuItem("Start Ip-Cam", new ImageIcon(String.format(BASE_FOLDER_FOR_ICON_IMAGES + "/ipcam.png")))).addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                        	//isRunningRasPiCam = false;
                        	checkIpCam();
                        	isTracking = false;
                        }
                    });
                    @SuppressWarnings("unused")
					JMenuItem item4;
                    popup.add(item4 = new JMenuItem("Config", new ImageIcon(String.format(BASE_FOLDER_FOR_ICON_IMAGES + "/config.jpeg")))).addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            //show_menu = !show_menu;
                            menu.setVisible(true);
                        }
                    });
                    popup.show((Component) e.getSource(), e.getX(), e.getY());
                }
				else if (e.getButton() == MouseEvent.BUTTON1) {
					@SuppressWarnings("unused")
					int xx = (int) (e.getX()/xScale);
	    			@SuppressWarnings("unused")
					int yy = (int) (e.getY()/yScale);
	    			//System.out.println("Coord. "+ xx + " " +yy);
				}
				else if (e.getButton() == MouseEvent.BUTTON2){
					if((int) e.getX() > (sizeTpl/2) && (int) e.getX() < widhtFrame - ((sizeTpl/2)+10) && (int) e.getY() > (sizeTpl/2) && (int) e.getY() <heightFrame){
						mouseTplX = (int) e.getX();
						mouseTplY = (int) e.getY();
						showImageResultTpl.setVisible(true);
						showImageResultWindow.setVisible(true);
						cutTplImg(temp, mouseTplX, mouseTplY, false);
					}
						
				}
                
			}

			@Override
			public void mouseReleased(MouseEvent e) {	
			}

			@Override
			public void mouseEntered(MouseEvent e) {	
			}

			@Override
			public void mouseExited(MouseEvent e) {	
			}		
    	});
    	
    	//JPanel for info and config values 
        config = new JPanel(new MigLayout());
    	
    	saveToDiskCheckBox = new JCheckBox("Save Image to HD");
        saveToDiskCheckBox.setMnemonic(KeyEvent.VK_C);
        saveToDiskCheckBox.setSelected(false);
        config.add(saveToDiskCheckBox,"width 160:180:200, h 40!, wrap");
        
        //JText info Data received
        txtText = new JTextField();
        txtText.setEditable(false);
        txtText.setToolTipText("Info of Frame received from DUNE.");
        info = String.format("X = 0 - Y = 0   x 1   0 bytes (KiB = 0)\t\t\t  ");
        txtText.setText(info);
        config.add(txtText, "cell 0 4 3 1, wrap");
        
        //JText info Data GPS received TCP
        txtDataTcp = new JTextField();
        txtDataTcp.setEditable(false);
        txtDataTcp.setToolTipText("Info of GPS received from DUNE (TCP).");
        info = String.format("\t\t\t\t\t  ");
        txtDataTcp.setText(info);
        config.add(txtDataTcp, "cell 0 5 3 1, wrap");
        
        //JText info
        txtData = new JTextField();
        txtData.setEditable(false);
        txtData.setToolTipText("Info of Frame received from DUNE.");
        info = String.format("\t\t\t\t\t  ");
        txtData.setText(info);
        config.add(txtData, "cell 0 6 3 1, wrap");
                
        menu = new JFrame("Menu_Config");
        menu.setVisible(false);
        menu.setResizable(false);
        menu.setSize(450, 350);
        menu.add(config);
        
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
    	    @Override
    	    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
    	    	if (JOptionPane.showConfirmDialog(frame,"Are you sure to close this window?", "Really Closing?", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
    	    		System.out.println("DONE...");
    	    		System.exit(0);
    	    	}
    	    	else {
    	    		frame.setVisible(true);
    	        }
    	    }
    	});
        
        showImageResultTpl.setResizable(false);
        showImageResultTpl.setSize(102, 126);
        showImageResultTpl.setLocation(200, 200);
        
        showImageResultWindow.setResizable(false);
        showImageResultWindow.setSize(152, 176);
        showImageResultWindow.setLocation(200, 400);
    }
    
    //!Fill cv::Mat image with zeros
    public static void inicImage(){
        Scalar black = new Scalar(0);
        matResize.setTo(black);
        temp=matToBufferedImage(matResize);
        showImage(temp);
        frame.setVisible(true);
    }
    
    //!Close TCP COM
    public static void closeTcpCom(){
    	try {
            is.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        try {
            in.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        out.close();
        try {
			serverSocket.close();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
    }
    
    //MAIN
	public static void main(String[] args) throws Exception {
		//System.out.println(System.getProperty("java.library.path"));
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		VideoCapture capture = null;
		VideoWriter saveVideo = new VideoWriter("teste.avi", VideoWriter.fourcc('D','I','V','X'), 10, new Size(640, 368));
		//Size a = new Size(960, 640);
		
		//saveVideo.open("teste.avi", VideoWriter.fourcc('D', 'I', 'V', 'X'), 10.0, new Size(960, 640));
				
		layoutIni();
		inicImage();
		//TODO
		
		/*String mediaURL="rtsp://192.168.0.90/axis-media/media.amp?videocodec=h264&resolution=640x480&RTSP/1.0";
		MediaLocator medialoc = new MediaLocator(mediaURL);
		Player myPlayer = Manager.createPlayer(medialoc);//Manager.createRealizedPlayer(ml);

		// get the components for the video and the playback controls
		Component myvideo = myPlayer.getVisualComponent();
		Component controls = myPlayer.getControlPanelComponent();*/
		
		
		while(!isRunningRasPiCam && !isRunningIpCam)
			Thread.sleep(10); 
		
		if(isRunningRasPiCam){
			tcpConnection();
			initSizeImage();
		}
		else{
			//TODO
			mat = new Mat(heightImgRec, widthImgRec, CvType.CV_8UC3);
			if(camRtpsUrl.equals("0"))
				capture = new VideoCapture(Integer.parseInt(camRtpsUrl));
			else
				capture = new VideoCapture(camRtpsUrl);
			
			if (capture.isOpened()){
                System.out.println("Video is captured");
                stateIpCamInic = false;
			}
            else{
            	System.out.println("Video is not captured");
            	Thread.sleep(1000);
            	stateIpCamInic = true;
            }
		}
			
		while (true){
			if(isRunningRasPiCam){
				if(isRunningIpCam){
					capture.release();
					stateIpCamInic = true;
					isRunningIpCam = false;
				}
				if(stateRasPiCom){
					tcpConnection();
					initSizeImage();
					stateRasPiCom = false;
				}
				if(!receivedDataImage()){
					closeCom = true;
				}
				if(closeCom){
					closeTcpCom();
		            stateRasPiCom = true;
		            closeCom = false;
		            isRunningRasPiCam = false;
                	isRunningIpCam = false;
				}
			}
			if(isRunningIpCam){
				if(isRunningRasPiCam){
					closeTcpCom();
					stateRasPiCom = true;
					isRunningRasPiCam = false;
				}
				if(stateIpCamInic)
				{
					mat = new Mat(heightImgRec, widthImgRec, CvType.CV_8UC3);
					if(camRtpsUrl.equals("0"))
						capture = new VideoCapture(Integer.parseInt(camRtpsUrl));
					else
						capture = new VideoCapture(camRtpsUrl);
					
					if (capture.isOpened()){
		                System.out.println("Video is captured");
		                stateIpCamInic = false;
					}
		            else{
		            	System.out.println("Video is not captured");
		            	Thread.sleep(1000);
		            	stateIpCamInic = true;
		            }
				}
				else
				{
					//capture.grab();
                    capture.read(mat);
                    System.out.println("size:"+mat.rows()+" : "+mat.cols());
                    System.out.println("cap");
                    saveVideo.write(mat);
                    System.out.println("sav");
                    //showMatchImage(matToBufferedImage(mat), zoomX, zoomY);
                    Imgproc.resize(mat, matResize, size);
                    temp=matToBufferedImage(matResize);
                    if(isTracking){
                    	cutWindowImg(temp, ((lastPosWindowX - sizeWindowSearch/2) + tplFoundX) - (sizeWindowSearch-sizeTpl)/2, ((lastPosWindowY - sizeWindowSearch/2) + tplFoundY) - (sizeWindowSearch-sizeTpl)/2);
                        findTpl(imgCutWindow, imgCutTpl);
                    }
                    showImage(temp);
				}
				if(closeCom){
					capture.release();
					stateIpCamInic = true;
					isRunningIpCam = false;
					closeCom = false;
					saveVideo.release();
				}
			}
			if(!isRunningRasPiCam && !isRunningIpCam)
				inicImage();
       }
    }
	
	private static boolean receivedDataImage() {
		long startTime = System.currentTimeMillis();
		try {
            line = in.readLine();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        if (line == null){
            //custom title, error icon
            JOptionPane.showMessageDialog(frame, "Lost connection with Vehicle...", "Connection error", JOptionPane.ERROR_MESSAGE);
            closeTcpCom();
            return false;
        }
        else{        
            lengthImage = Integer.parseInt(line);
            //buffer for save data receive
            data = new byte[lengthImage];
            //Send 1 for server for sync data send
            out.println("1\0");
            //read data image (ZP)
            int read = 0;
            while (read < lengthImage) {
                int readBytes = 0;
                try {
                    readBytes = is.read(data, read, lengthImage-read);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                if (readBytes < 0) {
                    System.err.println("stream ended");
                    closeTcpCom();
                }
                read += readBytes;
            }           
            
            //Receive data GPS over tcp DUNE
            try {
                duneGps = in.readLine();
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
            
            //Decompress data received 
            Inflater decompresser = new Inflater(false);
            decompresser.setInput(data,0,lengthImage);
            //Create an expandable byte array to hold the decompressed data
            ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
            // Decompress the data
            byte[] buf = new byte[(widthImgRec*heightImgRec*3)];
            while (!decompresser.finished()) 
            {
                try {
                    int count = decompresser.inflate(buf);                  
                    bos.write(buf, 0, count);
                } 
                catch (DataFormatException e) {
                    break;
                }
            }
            try {
                bos.close();
            } 
            catch (IOException e) {
            }
            
            // Get the decompressed data
            byte[] decompressedData = bos.toByteArray();
            //System.out.println("Original Size = " + decompressedData.length +" bytes");
            
            //Transform byte data to cv::Mat (for display image)
            mat.put(0, 0, decompressedData);
            //Resize image            
            Imgproc.resize(mat, matResize, size);
                       
            //Convert Mat to BufferedImage
            temp=matToBufferedImage(matResize);
            if(isTracking){
            	cutWindowImg(temp, ((lastPosWindowX - sizeWindowSearch/2) + tplFoundX) - (sizeWindowSearch-sizeTpl)/2, ((lastPosWindowY - sizeWindowSearch/2) + tplFoundY) - (sizeWindowSearch-sizeTpl)/2);
                findTpl(imgCutWindow, imgCutTpl);
            }
            long stopTime = System.currentTimeMillis();
            while((stopTime - startTime) < (1000/10))
                stopTime = System.currentTimeMillis();
            //Display image in JFrame
            info = String.format("Size:%d-%d | Scale:%.2f-%.2f | FPS:%d | %d bytes (KiB = %d)", widthImgRec, heightImgRec,xScale, yScale,(int) 1000/(stopTime - startTime),lengthImage,lengthImage/1024);
            txtText.setText(info);
            txtDataTcp.setText(duneGps);
            showImage(temp);
            return true;
        }
	}
	
	//TODO
	//!Roi for TPL
	public static void cutTplImg(final BufferedImage srcImage, int x, int y, boolean refresh){
		isTracking = false;
		if( x > (sizeTpl/2) && y > (sizeTpl/2) && x < srcImage.getWidth() - ((sizeTpl/2)+10) && y < srcImage.getHeight() - ((sizeTpl/2)+10)){
			imgCutTpl = new BufferedImage (sizeTpl, sizeTpl, BufferedImage.TYPE_3BYTE_BGR);
	        for( int i = -(sizeTpl/2); i < (sizeTpl/2); i++ )
	            for( int j = -(sizeTpl/2); j < (sizeTpl/2); j++ )
	            	imgCutTpl.setRGB(i + (sizeTpl/2), j + (sizeTpl/2), srcImage.getRGB( x + i, y + j));
	        
	        //draw image
	        jLabelCutTpl.setIcon(new ImageIcon(imgCutTpl));
	        showImageResultTpl.revalidate();
	        showImageResultTpl.add(jLabelCutTpl);
	        
	        if(!refresh)
	        	cutWindowImg(srcImage, x - sizeWindowSearch/2, y - sizeWindowSearch/2);
		}
		
        isTracking = true;
	}
	//!Roi for window search
	public static void cutWindowImg(final BufferedImage srcImage, int x, int y){
		if( x > 0 && y > 0 && x < srcImage.getWidth() - sizeWindowSearch && y < srcImage.getHeight() - sizeWindowSearch){
			imgCutWindow = new BufferedImage (sizeWindowSearch, sizeWindowSearch, BufferedImage.TYPE_3BYTE_BGR);
	        for( int i = 0; i < sizeWindowSearch; i++ )
	            for( int j = 0; j < sizeWindowSearch; j++ )
	            	imgCutWindow.setRGB(i, j, srcImage.getRGB( x + i, y + j));
	        
	        //draw image
	        jLabelCutWindow.setIcon(new ImageIcon(imgCutWindow));
	        showImageResultWindow.revalidate();
	        showImageResultWindow.add(jLabelCutWindow);
	        lastPosWindowX = x + sizeWindowSearch/2;
	        lastPosWindowY = y + sizeWindowSearch/2;
		}
	}
	
	//!Find TPL
	public static void findTpl( BufferedImage source, BufferedImage template){
		tpl = new Mat(sizeTpl, sizeTpl, CvType.CV_8UC3);
		tpl = bufferedImageToMat(template);
		
		src = new Mat(source.getWidth(), source.getHeight(), CvType.CV_8UC3);
		src = bufferedImageToMat(source);
		
		int resultCols = src.cols() - tpl.cols() + 1;
		int resultRows = src.rows() - tpl.rows() + 1;
		result = new Mat(resultRows, resultCols, CvType.CV_32F);
		
		Imgproc.matchTemplate(src, tpl, result, Imgproc.TM_SQDIFF_NORMED);
		
		//Get maxima minima
        Core.MinMaxLocResult locres = Core.minMaxLoc(result);
        Point matchLoc = locres.minLoc;
        tplFoundX = (int)matchLoc.x;
        tplFoundY = (int)matchLoc.y;
        
        counterRefreshTpl++;
        if(counterRefreshTpl > nFramesToRefresh){
        	cutTplImg(temp, lastPosWindowX, lastPosWindowY, true);
        	counterRefreshTpl = 0; 
        }
	}
	
	//!Convert bufferedImage to Mat
	public static Mat bufferedImageToMat(BufferedImage in)
    {
          Mat out;
          byte[] data;
          int r, g, b;

          if(in.getType() == BufferedImage.TYPE_INT_RGB)
          {
              out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC3);
              data = new byte[in.getWidth() * in.getHeight() * (int)out.elemSize()];
              int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
              for(int i = 0; i < dataBuff.length; i++)
              {
                  data[i*3] = (byte) ((dataBuff[i] >> 16) & 0xFF);
                  data[i*3 + 1] = (byte) ((dataBuff[i] >> 8) & 0xFF);
                  data[i*3 + 2] = (byte) ((dataBuff[i] >> 0) & 0xFF);
              }
          }
          else
          {
              out = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC1);
              data = new byte[in.getWidth() * in.getHeight() * (int)out.elemSize()];
              int[] dataBuff = in.getRGB(0, 0, in.getWidth(), in.getHeight(), null, 0, in.getWidth());
              for(int i = 0; i < dataBuff.length; i++)
              {
                r = (byte) ((dataBuff[i] >> 16) & 0xFF);
                g = (byte) ((dataBuff[i] >> 8) & 0xFF);
                b = (byte) ((dataBuff[i] >> 0) & 0xFF);
                data[i] = (byte)((0.21 * r) + (0.71 * g) + (0.07 * b)); //luminosity
              }
           }
           out.put(0, 0, data);
           return out;
     } 
	
	//!Zoom in
    public static void getCutImage(BufferedImage imageToCut, int w, int h){
        zoomImgCut = new BufferedImage (100, 100, BufferedImage.TYPE_3BYTE_BGR);
        for( int i = -50; i < 50; i++ )
            for( int j = -50; j < 50; j++ )
                zoomImgCut.setRGB(i + 50, j + 50, imageToCut.getRGB( w+i, h+j));

        // Create new (blank) image of required (scaled) size
        scaledCutImage = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        // Paint scaled version of image to new image
        graphics2D = scaledCutImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(zoomImgCut, 0, 0, 300, 300, null);
        // clean up
        graphics2D.dispose();
        //draw image
        zoomLabel.setIcon(new ImageIcon(scaledCutImage));
        zoomImg.revalidate();
        zoomImg.add(zoomLabel);
    }
    
	//!Show Image
	public static void showImage(final BufferedImage image){
	    int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
	    
	    resizedImage = new BufferedImage(widhtFrame, heightFrame, type);
	    g = resizedImage.createGraphics();
	    g.drawImage(image, 0, 0, widhtFrame, heightFrame, null);
	    if(isTracking && (lastPosWindowX + tplFoundX) > 0 && (lastPosWindowX + tplFoundX) < image.getWidth() - (sizeWindowSearch/2) && (lastPosWindowY + tplFoundY) > 0 && (lastPosWindowY + tplFoundY) < image.getHeight() - (sizeWindowSearch/2)){
	    	g.setColor(Color.RED);
	        g.drawRect(lastPosWindowX - sizeWindowSearch/2, lastPosWindowY - sizeWindowSearch/2, sizeWindowSearch, sizeWindowSearch);
	        g.setColor(Color.GREEN);
	        g.drawRect(lastPosWindowX - (sizeTpl/2), lastPosWindowY - (sizeTpl/2), sizeTpl, sizeTpl);
	    }
	    g.dispose();
	    	    
	    imageIcon = new ImageIcon(resizedImage);
	    imageIcon.getImage().flush();
	    jLabel.setIcon(imageIcon);
	    frame.revalidate();
	    frame.add(jLabel, BorderLayout.CENTER);
	  }
	
	//!Create Socket service
    public static void tcpConnection(){
        //Socket Config
        try { 
            serverSocket = new ServerSocket(2424); 
        } 
        catch (IOException e) 
        { 
            System.err.println("Could not listen on port: "+ serverSocket); 
            System.exit(1); 
        } 
        
        System.out.println ("Waiting for connection.....");
        try { 
            clientSocket = serverSocket.accept(); 
        } 
        catch (IOException e) 
        { 
            System.err.println("Accept failed."); 
            System.exit(1); 
        } 
        System.out.println ("Connection successful from Server: "+clientSocket.getInetAddress()+":"+serverSocket.getLocalPort());
        System.out.println ("Receiving data image.....");
        
        //Send data for sync 
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }

        //Buffer for data image
        try {
            is = clientSocket.getInputStream();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        //Buffer for info of data image
        in = new BufferedReader( new InputStreamReader( is ));
    }
    
    //Get size of image
    public static void initSizeImage(){
        //Width size of image
        try {
            widthImgRec = Integer.parseInt(in.readLine());
        }
        catch (NumberFormatException | IOException e) {
            e.printStackTrace();
        }
        //Height size of image
        try {
            heightImgRec = Integer.parseInt(in.readLine());
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        xScale = (float)widhtFrame/widthImgRec;
        yScale = (float)heightFrame/heightImgRec;
        //Create Buffer (type MAT) for Image receive
        mat = new Mat(heightImgRec, widthImgRec, CvType.CV_8UC3);
    }
    
    /**  
     * Converts/writes a Mat into a BufferedImage.  
     * @param matrix Mat of type CV_8UC3 or CV_8UC1  
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY  
     */  
    public static BufferedImage matToBufferedImage(Mat matrix) {
        int cols = matrix.cols();  
        int rows = matrix.rows();  
        int elemSize = (int)matrix.elemSize();  
        byte[] data = new byte[cols * rows * elemSize];  
        int type;  
        matrix.get(0, 0, data);  
        switch (matrix.channels()) {  
            case 1:  
                type = BufferedImage.TYPE_BYTE_GRAY;  
                break;  
            case 3:  
                type = BufferedImage.TYPE_3BYTE_BGR;  
                // bgr to rgb  
                byte b;  
                for(int i=0; i<data.length; i=i+3) {  
                    b = data[i];  
                    data[i] = data[i+2];  
                    data[i+2] = b;  
                }  
                break;  
        default:  
            return null;  
        }
        BufferedImage image2 = new BufferedImage(cols, rows, type);  
        image2.getRaster().setDataElements(0, 0, cols, rows, data);  
        return image2;
    }
} 
