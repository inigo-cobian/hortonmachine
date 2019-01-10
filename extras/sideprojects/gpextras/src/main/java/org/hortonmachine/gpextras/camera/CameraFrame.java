package org.hortonmachine.gpextras.camera;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

/**
 * @author Bartosz Firyn (SarXos)
 */
public class CameraFrame extends JFrame
        implements
            Runnable,
            WebcamListener,
            WindowListener,
            UncaughtExceptionHandler,
            ItemListener,
            WebcamDiscoveryListener,
            MouseListener {

    private static final long serialVersionUID = 1L;

    private Webcam webcam = null;
    private WebcamPanel panel = null;

    private String fileToWrite;

    private List<CameraListener> cameraListeners = new ArrayList<>();

    public CameraFrame( String fileToWrite ) {
        this.fileToWrite = fileToWrite;
    }

    public void addListener( CameraListener listener ) {
        if (!cameraListeners.contains(listener)) {
            cameraListeners.add(listener);
        }
    }

    public void removeListener( CameraListener listener ) {
        if (cameraListeners.contains(listener)) {
            cameraListeners.remove(listener);
        }
    }

    public void clearListeners() {
        cameraListeners.clear();
    }

    @Override
    public void run() {

        Webcam.addDiscoveryListener(this);

        setTitle("Java Webcam Capture POC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        addWindowListener(this);
        addMouseListener(this);

        webcam = Webcam.getDefault();

        Dimension[] supportedViewSizes = webcam.getViewSizes();
        webcam.setViewSize(supportedViewSizes[supportedViewSizes.length - 1]);
        webcam.open();

        webcam.addWebcamListener(this);

        panel = new WebcamPanel(webcam, false);
        panel.setImageSizeDisplayed(true);
        add(panel, BorderLayout.CENTER);

        setUndecorated(true);
        pack();
        setVisible(true);

        Thread t = new Thread(){
            @Override
            public void run() {
                panel.start();
            }
        };
        t.setName("example-starter");
        t.setDaemon(true);
        t.setUncaughtExceptionHandler(this);
        t.start();
    }

    @Override
    public void webcamOpen( WebcamEvent we ) {
    }

    @Override
    public void webcamClosed( WebcamEvent we ) {
    }

    @Override
    public void webcamDisposed( WebcamEvent we ) {
    }

    @Override
    public void webcamImageObtained( WebcamEvent we ) {
    }

    @Override
    public void windowActivated( WindowEvent e ) {
    }

    @Override
    public void windowClosed( WindowEvent e ) {
        webcam.close();
    }

    @Override
    public void windowClosing( WindowEvent e ) {
    }

    @Override
    public void windowOpened( WindowEvent e ) {
    }

    @Override
    public void windowDeactivated( WindowEvent e ) {
    }

    @Override
    public void windowDeiconified( WindowEvent e ) {
        panel.resume();
    }

    @Override
    public void windowIconified( WindowEvent e ) {
        panel.pause();
    }

    @Override
    public void uncaughtException( Thread t, Throwable e ) {
        e.printStackTrace();
    }

    @Override
    public void itemStateChanged( ItemEvent e ) {
        if (e.getItem() != webcam) {
            if (webcam != null) {

                panel.stop();

                remove(panel);

                webcam.removeWebcamListener(this);
                webcam.close();

                webcam = (Webcam) e.getItem();
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.addWebcamListener(this);

                panel = new WebcamPanel(webcam, false);
                panel.setFPSDisplayed(true);

                add(panel, BorderLayout.CENTER);
                pack();

                Thread t = new Thread(){

                    @Override
                    public void run() {
                        panel.start();
                    }
                };
                t.setDaemon(true);
                t.setUncaughtExceptionHandler(this);
                t.start();
            }
        }
    }

    @Override
    public void webcamFound( WebcamDiscoveryEvent event ) {
    }

    @Override
    public void webcamGone( WebcamDiscoveryEvent event ) {
    }

    @Override
    public void mouseClicked( MouseEvent e ) {
    }

    @Override
    public void mousePressed( MouseEvent e ) {
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        try {
            ImageIO.write(webcam.getImage(), "JPG", new File(fileToWrite));
            panel.stop();
            setVisible(false);
            dispose();
            
            for( CameraListener cameraListener : cameraListeners ) {
                cameraListener.onPictureTaken(fileToWrite);
            }
        } catch (IOException t) {
            t.printStackTrace();
        }
    }

    @Override
    public void mouseEntered( MouseEvent e ) {
    }

    @Override
    public void mouseExited( MouseEvent e ) {
    }

}