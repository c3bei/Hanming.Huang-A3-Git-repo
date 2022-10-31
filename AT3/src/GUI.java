import javax.swing.*;
import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
//点击后出现的形状对应的类
abstract class Shape{

    int addSize;
}
class ClickCirCle extends Shape{
    int pos_x,pos_y;
    Color color;

    public ClickCirCle(int pos_x, int pos_y,Color color) {
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.addSize = 0;
        this.color=color;
    }
}
class ClickRect extends Shape{
    int pos_x,pos_y;
    Color color;

    public ClickRect(int pos_x, int pos_y,Color color) {
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.addSize = 0;
        this.color=color;
    }
}
//图形类
class Rect{
    int x,y;
    int width,height;
    boolean selected;
    public Rect(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.selected=false;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
public class GUI extends JFrame {
    private class ReboundListener implements ActionListener {
        Shape shape;

        public ReboundListener(Shape shape) {

            this.shape = shape;
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (shape != null && shape.addSize < endSize) {

                shape.addSize = shape.addSize + 3;

            } else {
                shape = null;
            }
        }
    }
    //偏移量
    static int deviationX=8;
    static int deviationY=30;
    //画面大小
    int width=666;
    int height=428;
    int row=3,col=5;
    //单个正方形的大小
    int grid_size=130;
    private final int DELAY = 10;
    int rect_size=10;
    Rect[][] rects=new Rect[row][col];
    Rect[][] areas=new Rect[row][col];
    static JFrame frame;
    static Graphics globalGraphics;
    int endSize=140;
    List<Shape> shapeList =new CopyOnWriteArrayList<>();
    //随机数
    Random generator = new Random();
    Color[] colors = {Color.RED, new Color(164, 164, 1),
            new Color(114, 2, 118),
            new Color(26, 73, 1),
            new Color(73, 2, 42),
            new Color(73, 39, 1)};
    List<AudioClip> soundList = new CopyOnWriteArrayList();
    //所有的点击生成的形状 对应的定时器
    List<Timer>timers=new CopyOnWriteArrayList<>();

    //绘制 圆形
    private void drawCircle(Graphics g, ClickCirCle clickCirCle) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color=new Color(clickCirCle.color.getRed(),clickCirCle.color.getGreen(),clickCirCle.color.getBlue(),255-clickCirCle.addSize*255/endSize);
        g2d.setColor(color);
        g2d.setStroke(new java.awt.BasicStroke(10.0f));
        int start_size=0;
        g2d.drawOval(clickCirCle.pos_x-(start_size+start_size+clickCirCle.addSize)/2, clickCirCle.pos_y-(start_size+clickCirCle.addSize)/2,
                start_size+start_size+clickCirCle.addSize, start_size+start_size+clickCirCle.addSize);
        g2d.dispose();
    }
    //绘制矩形
    private void drawRect(Graphics g, ClickRect clickRect) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color color=new Color(clickRect.color.getRed(),clickRect.color.getGreen(),clickRect.color.getBlue(),255-clickRect.addSize*255/endSize);
        g2d.setColor(color);
//        g2d.setColor(clickRect.color);
        g2d.setStroke(new java.awt.BasicStroke(10.0f));
        int start_size=0;
        g2d.drawRect(clickRect.pos_x-(start_size+start_size+clickRect.addSize)/2, clickRect.pos_y-(start_size+clickRect.addSize)/2,
                start_size+start_size+clickRect.addSize, start_size+start_size+clickRect.addSize);
        g2d.dispose();
    }
    static int curPlayCol=0;
    public GUI() {
        super("DrawRect");
        frame=this;
        init();
        setBounds(100, 100, width, height);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        // 定期更新画面
        Timer updaetTimer = new Timer(DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                frame.paint(globalGraphics);
            }
        });
        updaetTimer.start();
        //一个从左往右的光波会播放所在行所有被选中的方块的音效
        Timer playColTimer = new Timer(2000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                for(int i=0;i<row;i++){
                    if(rects[i][curPlayCol].selected){
                        soundList.get(i*col+curPlayCol).play();
                    }
                }
                curPlayCol++;
                if(curPlayCol>=col){
                    curPlayCol=0;
                }
            }
        });
        playColTimer.start();
        globalGraphics=getGraphics();
        //鼠标点击事件
        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int x=e.getX();
                int y=e.getY();
                //根据点击的绘制 随机的生成圆形或者方形
                Shape shape;
                int rand=generator.nextInt()%2;
                int randIndex=generator.nextInt()%colors.length;
                randIndex=randIndex<0?-randIndex:randIndex;
                if(rand==0)
                {
                    shape=new ClickCirCle(x,y,colors[randIndex]);
                }
                else{
                    shape=new ClickRect(x,y,colors[randIndex]);
                }
                shape.addSize=0;
                shapeList.add(shape);
                Timer timer = new Timer(DELAY,new ReboundListener(shape) );
                timers.add(timer);
                timer.start();
                int cnt=0;
                for(int i=0;i<row;i++){
                    for(int j=0;j<col;j++){
                        if(isInRect(x,y,areas[i][j])){

                            repaint(deviationX+rects[i][j].x,deviationY+rects[i][j].y,rects[i][j].width,rects[i][j].height);
                            if(rects[i][j].selected==false){
                                soundList.get(cnt).play();
                            }
                            rects[i][j].setSelected(!rects[i][j].isSelected());
                        }

                        cnt++;
                    }
                }
            }
        });

    }
    //根据提供的path 读取音频文件
    void addSound(String path){
        AudioClip audioClip = null;
        try {
            String url = new File(path).toURI().toURL().toString();
            audioClip = Applet.newAudioClip(new URL(url));
            soundList.add(audioClip);
//            System.out.println(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    //初始化
    void init(){
        //从文件中加载声音文件
        try {
            for(int cnt=1;cnt<=100;cnt++){
                if(new File("src\\sounds\\"+cnt+".wav").exists()){
                    addSound("src\\sounds\\"+cnt+".wav");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("soundList.size()="+soundList.size());
        for(int i=0;i<row;i++){
            for(int j=0;j<col;j++){
                rects[i][j]=new Rect(grid_size*j+(grid_size-rect_size)/2,grid_size*i+(grid_size-rect_size)/2,rect_size,rect_size);
                areas[i][j]=new Rect(grid_size*j,grid_size*i,grid_size,grid_size);
//                add(map[i][j]);
            }
        }
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(new Color(147,193,191));
        g.fillRect(0, 0, width, height);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {

//                g.setColor(Color.BLACK);
//                g.drawRect(deviationX+areas[i][j].x,deviationY+areas[i][j].y,areas[i][j].width,areas[i][j].height);
                if(curPlayCol==j) {
                    g.setColor(new Color(144,185,127));
                    g.fillRect(deviationX+areas[i][j].x,deviationY+areas[i][j].y,areas[i][j].width,areas[i][j].height);

                }
                g.setColor(Color.WHITE);
                if(rects[i][j].isSelected()){
                    g.setColor(Color.YELLOW);
                }
                else {
                    g.setColor(Color.WHITE);
                }
                g.fillRect(deviationX+rects[i][j].x,deviationY+rects[i][j].y,rects[i][j].width,rects[i][j].height);
            }
        }
        for(int i = 0; i< shapeList.size(); i++){
            Shape shape= shapeList.get(i);
            if(shape.addSize>=endSize){
                shapeList.remove(i);
                timers.get(i).stop();
                timers.remove(i);
                i--;

            }
        }
        for(Shape shape: shapeList){
            if(shape instanceof ClickCirCle){
                ClickCirCle clickCirCle1=(ClickCirCle)shape;
                drawCircle(g,clickCirCle1);
            }
            else if(shape instanceof ClickRect){
                ClickRect clickRect1=(ClickRect)shape;
                drawRect(g,clickRect1);
            }
        }

    }
    //判断当前点击位置和方块位置是否重合

    boolean isInRect(int x, int y, Rect rect){
        x-=deviationX;
        y-=deviationY;
        if(x>=rect.x&&x<=rect.x+rect.width&&y>=rect.y&&y<=rect.y+rect.height){
            return true;
        }
        return false;
    }
    public static void main(String[] args) {
        new GUI();
    }
}
