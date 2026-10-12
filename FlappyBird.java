import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

// Base class: anything that has a position and size and can be moved and drawn.
abstract class GameObject {
    protected int x, y, w, h;

    GameObject(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }

    abstract void update();           // each subclass moves in its own way
    abstract void draw(Graphics g);   // each subclass draws itself differently

    Rectangle bounds() { return new Rectangle(x, y, w, h); }
}

// Bird IS-A GameObject. Its speed is private (encapsulation).
class Bird extends GameObject {
    private double speed = 0;

    Bird() { super(80, 250, 30, 30); }

    void flap() { speed = -8; }

    @Override
    void update() {
        speed += 0.5;   // gravity
        y += speed;
    }

    @Override
    void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, w, h);
    }
}

// One pipe pair: a top pipe and a bottom pipe with a gap between them.
class Pipe extends GameObject {
    static final int GAP = 140;
    private int gapY;            // top of the gap
    boolean passed = false;      // has the bird scored on this pipe yet?

    Pipe(int x, int gapY) {
        super(x, 0, 50, 600);
        this.gapY = gapY;
    }

    @Override
    void update() { x -= 3; }

    @Override
    void draw(Graphics g) {
        g.setColor(Color.GREEN.darker());
        g.fillRect(x, 0, w, gapY);                       // top pipe
        g.fillRect(x, gapY + GAP, w, 600 - gapY - GAP);  // bottom pipe
    }

    // Overrides the default box check, because a pipe has a hole in it.
    boolean hits(Bird b) {
        Rectangle r = b.bounds();
        return r.intersects(new Rectangle(x, 0, w, gapY))
            || r.intersects(new Rectangle(x, gapY + GAP, w, 600));
    }

    boolean offScreen() { return x + w < 0; }
}

// The game: owns a Bird and a list of Pipes, runs the loop, handles input.
public class FlappyBird extends JPanel implements ActionListener, KeyListener {
    private Bird bird = new Bird();
    private ArrayList<Pipe> pipes = new ArrayList<>();
    private Random rand = new Random();
    private Timer timer = new Timer(20, this);   // calls actionPerformed every 20 ms
    private int score = 0, ticks = 0;
    private boolean gameOver = false;

    FlappyBird() {
        setPreferredSize(new Dimension(400, 600));
        setFocusable(true);
        addKeyListener(this);
        timer.start();
    }

    private void reset() {
        bird = new Bird();
        pipes.clear();
        score = 0; ticks = 0; gameOver = false;
    }

    // Game loop: runs every tick.
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            bird.update();
            if (ticks++ % 90 == 0) pipes.add(new Pipe(400, 50 + rand.nextInt(350)));

            for (Pipe p : new ArrayList<>(pipes)) {
                p.update();
                if (p.hits(bird)) gameOver = true;
                if (!p.passed && p.x + p.w < bird.x) { p.passed = true; score++; }
                if (p.offScreen()) pipes.remove(p);
            }
            if (bird.y < 0 || bird.y + 30 > 600) gameOver = true;
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setBackground(Color.CYAN);
        bird.draw(g);
        for (Pipe p : pipes) p.draw(g);   // polymorphism: same call, each object draws itself
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 10, 30);
        if (gameOver) g.drawString("Game over - press SPACE", 80, 300);
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameOver) reset(); else bird.flap();
        }
    }
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("Flappy Bird");
        f.add(new FlappyBird());
        f.pack();
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }
}
