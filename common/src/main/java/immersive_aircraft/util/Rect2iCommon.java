package immersive_aircraft.util;

public class Rect2iCommon {
    private int xPos;
    private int yPos;
    private int width;
    private int height;

    public Rect2iCommon(int i, int j, int k, int l) {
        this.xPos = i;
        this.yPos = j;
        this.width = k;
        this.height = l;
    }

    public int getX() {
        return this.xPos;
    }

    public int getY() {
        return this.yPos;
    }

    public void setX(int xPos) {
        this.xPos = xPos;
    }

    public void setY(int yPos) {
        this.yPos = yPos;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setPosition(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public boolean contains(int x, int y) {
        return x >= this.xPos && x <= this.xPos + this.width && y >= this.yPos && y <= this.yPos + this.height;
    }
}
