/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package application;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

/**
 *
 * @author sharv
 */
public class PlayerSpectrumListener implements AudioSpectrumListener{

    private Canvas spectrumCanvas;
    private MediaPlayer player;
    
    private double volume;
    private boolean skip;
    private boolean enable;
    
    public PlayerSpectrumListener(Canvas spectrumCanvas, MediaPlayer player) {
        enable = false;
        this.spectrumCanvas = spectrumCanvas;
        this.player = player;
    }
    
    public void setVolume(double newVolume) {
        this.volume = newVolume;
    }
    
    public void setSkip(boolean skip) {
        this.skip = skip;
    }
    
    public void setEnable(boolean newVal) {
        this.enable = newVal;
    }
    
    public void clearCanvas() {
        GraphicsContext gc = spectrumCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, spectrumCanvas.getWidth(), spectrumCanvas.getHeight());
    }
    
    @Override
    public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
        
        clearCanvas();
        GraphicsContext gc = spectrumCanvas.getGraphicsContext2D();
        gc.setFill(new Color(1, 0.4, 0.4, 0.6));
        
        if (skip || enable)
            return;
        
        double maxHeight = (double) spectrumCanvas.getHeight() * 0.6;
        int bandStartIndex = 5;
        int bandEndIndex = magnitudes.length;
        double xInterval = (double) spectrumCanvas.getWidth() / (double) (bandEndIndex - bandStartIndex);
        
        double max = -1;
        
        for (int i = bandStartIndex; i < bandEndIndex; i++) {
            double k = (magnitudes[i] - player.getAudioSpectrumThreshold());
            if (k > max)
                max = k;
        }
        
        for (int i = bandStartIndex; i < bandEndIndex; i++) {
            double k = (magnitudes[i] - player.getAudioSpectrumThreshold());
            k =  maxHeight * (k / max) * volume;
            gc.fillRect((i - bandStartIndex) * xInterval, spectrumCanvas.getHeight() - k, xInterval, spectrumCanvas.getHeight() * maxHeight);
        }        
    }
    
}
