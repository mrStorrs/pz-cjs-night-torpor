import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public final class CreatePoster {
    private CreatePoster() {
    }

    public static void main(String[] args) throws Exception {
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.setPaint(new Color(7, 10, 19));
        graphics.fillRect(0, 0, 512, 512);
        graphics.setPaint(new Color(18, 27, 45));
        graphics.fillRect(0, 315, 512, 197);

        graphics.setPaint(new Color(224, 226, 193));
        graphics.fill(new Ellipse2D.Double(130, 52, 252, 252));
        graphics.setPaint(new Color(7, 10, 19));
        graphics.fill(new Ellipse2D.Double(184, 38, 246, 246));

        graphics.setPaint(new Color(45, 56, 70));
        Polygon silhouette = new Polygon();
        silhouette.addPoint(264, 165);
        silhouette.addPoint(304, 188);
        silhouette.addPoint(319, 258);
        silhouette.addPoint(352, 315);
        silhouette.addPoint(167, 315);
        silhouette.addPoint(210, 264);
        silhouette.addPoint(224, 191);
        graphics.fill(silhouette);
        graphics.fill(new Ellipse2D.Double(226, 118, 74, 82));

        graphics.setPaint(new Color(236, 238, 228));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 39));
        graphics.drawString("NIGHT TORPOR", 112, 393);
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 21));
        graphics.setPaint(new Color(152, 166, 188));
        graphics.drawString("SAME SPEED  •  DULLED SENSES", 86, 439);

        graphics.dispose();
        ImageIO.write(image, "png", new File(args[0]));
    }
}
