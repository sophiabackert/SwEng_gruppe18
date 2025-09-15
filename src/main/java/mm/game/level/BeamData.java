package mm.game.level;

public class BeamData {
    public double layoutX;
    public double layoutY;
    public double width;
    public double height;
    public String fillColor;

    public BeamData() {}

    public BeamData(double x, double y, double w, double h, String fillColor) {
        this.layoutX = x;
        this.layoutY = y;
        this.width = w;
        this.height = h;
        this.fillColor = fillColor;
    }
}

