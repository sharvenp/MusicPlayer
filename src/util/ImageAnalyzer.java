package util;

import java.util.Hashtable;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

/**
 *
 * @author sharv
 */
public class ImageAnalyzer {
    
    public static Background getProminentStyle(Image image) {   
        
        Hashtable<Integer, Integer> hist = new Hashtable<>();
        PixelReader reader = image.getPixelReader();

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {

                int c = reader.getArgb(i, j);

                if (!hist.contains(c)) {
                    hist.put(c, 0);
                }

                hist.put(c, hist.get(c) + 1);
            }
        }
        
        int max = -1;
        int prominentColor = -1;
        for (int k : hist.keySet()) {
            
            int val = hist.get(k);
            
            if (val > max) {
                max = val;
                prominentColor = k;
            }
        }

        //String style = String.format("-fx-background-color: #%06X;", (0xFFFFFF & prominentColor));
        //System.out.println("New Style: " + style);
        //return style;
    
        return new Background(new BackgroundFill(Color.valueOf(String.format("#%06X", 0xFFFFFF & prominentColor)), 
                              CornerRadii.EMPTY, Insets.EMPTY));
    }
}
