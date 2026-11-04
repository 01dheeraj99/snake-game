import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.sound.sampled.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    // --- Game Settings ---
    static final int TILE = 25;        // size of each tile in pixels
    static final int WIDTH = 20;       // number of tiles horizontally
    static final int HEIGHT = 20;      // number of tiles vertically
    static final int MAX_BODY = 400;   // max snake length

    // Snake body coordinates
    int[] snakeX = new int[MAX_BODY];
    int[] snakeY = new int[MAX_BODY];
    int length = 3;  // starting length

    // Food position
    int foodX, foodY;

    // Direction: U=Up, D=Down, L=Left, R=Right
    char direction = 'R';

    // Game state
    int score = 0;
    boolean running = false;
    boolean paused = false;
    Timer timer;
    Random random = new Random();

    // Background image and apple icon
    BufferedImage bgImage;
    BufferedImage appleImage;

    // Background music clip
    Clip bgMusic;

    // --- Constructor: set up the game ---
    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH * TILE, HEIGHT * TILE));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // Load background image and apple icon
        try {
            bgImage = ImageIO.read(new File("background.png"));
        } catch (Exception e) {
            System.out.println("Could not load background.png: " + e.getMessage());
            bgImage = null;
        }
        try {
            appleImage = ImageIO.read(new File("apple.png"));
        } catch (Exception e) {
            System.out.println("Could not load apple.png: " + e.getMessage());
            appleImage = null;
        }

        // Load background music
        try {
            File audioFile = new File("music.wav");
            if (audioFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                bgMusic = AudioSystem.getClip();
                bgMusic.open(audioStream);
            }
        } catch (Exception e) {
            System.out.println("Could not load music.wav: " + e.getMessage());
            bgMusic = null;
        }

        startGame();
    }

    // --- Music Control ---
    void playMusic() {
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.setFramePosition(0); // restart from beginning
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY); // loop continuously
        }
    }

    void pauseMusic() {
        if (bgMusic != null && bgMusic.isRunning()) {
            bgMusic.stop();
        }
    }

    void resumeMusic() {
        if (bgMusic != null) {
            bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    // Reset everything and start a new game
    void startGame() {
        length = 3;
        score = 0;
        direction = 'R';
        running = true;
        paused = false;

        // Place snake in the middle
        for (int i = 0; i < length; i++) {
            snakeX[i] = (WIDTH / 2 - i) * TILE;
            snakeY[i] = (HEIGHT / 2) * TILE;
        }

        placeFood();

        // Play/restart looping music
        playMusic();

        // Timer triggers movement every 120ms
        if (timer != null) timer.stop();
        timer = new Timer(120, this);
        timer.start();
    }

    // Place food at a random position
    void placeFood() {
        foodX = random.nextInt(WIDTH) * TILE;
        foodY = random.nextInt(HEIGHT) * TILE;
    }

    // --- Drawing ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (running) {
            // Draw background image (stretched to fill the panel)
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, WIDTH * TILE, HEIGHT * TILE, null);
            }

            // Draw grid lines (faint)
            g.setColor(new Color(30, 30, 30, 80));
            for (int i = 0; i < WIDTH; i++)
                g.drawLine(i * TILE, 0, i * TILE, HEIGHT * TILE);
            for (int i = 0; i < HEIGHT; i++)
                g.drawLine(0, i * TILE, WIDTH * TILE, i * TILE);

            // Draw food (apple icon or red circle fallback)
            if (appleImage != null) {
                g.drawImage(appleImage, foodX, foodY, TILE, TILE, null);
            } else {
                g.setColor(Color.RED);
                g.fillOval(foodX, foodY, TILE, TILE);
            }

            // Draw snake (body first so head draws on top)
            for (int i = length - 1; i >= 0; i--) {
                int x = snakeX[i];
                int y = snakeY[i];

                if (i == 0) {
                    // --- HEAD ---
                    g.setColor(new Color(0, 180, 0));
                    g.fillOval(x, y, TILE, TILE);
                    // darker outline
                    g.setColor(new Color(0, 100, 0));
                    g.drawOval(x, y, TILE, TILE);

                    // Eyes (position depends on direction)
                    int eyeSize = 5;
                    int pupilSize = 3;
                    int ex1 = 0, ey1 = 0, ex2 = 0, ey2 = 0; // two eye positions
                    switch (direction) {
                        case 'U': ex1 = x+4;  ey1 = y+4;  ex2 = x+16; ey2 = y+4;  break;
                        case 'D': ex1 = x+4;  ey1 = y+16; ex2 = x+16; ey2 = y+16; break;
                        case 'L': ex1 = x+4;  ey1 = y+4;  ex2 = x+4;  ey2 = y+16; break;
                        case 'R': ex1 = x+16; ey1 = y+4;  ex2 = x+16; ey2 = y+16; break;
                    }
                    // white part of eye
                    g.setColor(Color.WHITE);
                    g.fillOval(ex1-eyeSize/2, ey1-eyeSize/2, eyeSize, eyeSize);
                    g.fillOval(ex2-eyeSize/2, ey2-eyeSize/2, eyeSize, eyeSize);
                    // black pupil
                    g.setColor(Color.BLACK);
                    g.fillOval(ex1-pupilSize/2, ey1-pupilSize/2, pupilSize, pupilSize);
                    g.fillOval(ex2-pupilSize/2, ey2-pupilSize/2, pupilSize, pupilSize);

                    // Tongue (red, forked, sticking out from the front)
                    g.setColor(Color.RED);
                    int tx = x + TILE/2, ty = y + TILE/2; // center of head
                    int tongueLen = 10;
                    int forkLen = 4;
                    switch (direction) {
                        case 'U':
                            g.drawLine(tx, y, tx, y - tongueLen);
                            g.drawLine(tx, y - tongueLen, tx-3, y - tongueLen - forkLen);
                            g.drawLine(tx, y - tongueLen, tx+3, y - tongueLen - forkLen);
                            break;
                        case 'D':
                            g.drawLine(tx, y+TILE, tx, y+TILE + tongueLen);
                            g.drawLine(tx, y+TILE + tongueLen, tx-3, y+TILE + tongueLen + forkLen);
                            g.drawLine(tx, y+TILE + tongueLen, tx+3, y+TILE + tongueLen + forkLen);
                            break;
                        case 'L':
                            g.drawLine(x, ty, x - tongueLen, ty);
                            g.drawLine(x - tongueLen, ty, x - tongueLen - forkLen, ty-3);
                            g.drawLine(x - tongueLen, ty, x - tongueLen - forkLen, ty+3);
                            break;
                        case 'R':
                            g.drawLine(x+TILE, ty, x+TILE + tongueLen, ty);
                            g.drawLine(x+TILE + tongueLen, ty, x+TILE + tongueLen + forkLen, ty-3);
                            g.drawLine(x+TILE + tongueLen, ty, x+TILE + tongueLen + forkLen, ty+3);
                            break;
                    }

                } else if (i == length - 1) {
                    // --- TAIL (smaller, tapered) ---
                    int shrink = 4;
                    g.setColor(i % 2 == 0 ? new Color(0, 160, 0) : new Color(0, 130, 0));
                    g.fillOval(x + shrink/2, y + shrink/2, TILE - shrink, TILE - shrink);

                } else {
                    // --- BODY (alternating pattern, rounded) ---
                    if (i % 2 == 0) {
                        g.setColor(new Color(0, 160, 0));  // lighter green
                    } else {
                        g.setColor(new Color(0, 130, 0));  // darker green
                    }
                    int shrink = 2; // slightly smaller than tile for rounded look
                    g.fillOval(x + shrink/2, y + shrink/2, TILE - shrink, TILE - shrink);
                    // belly highlight (small lighter oval in center)
                    g.setColor(new Color(100, 220, 100, 80));
                    g.fillOval(x + TILE/4, y + TILE/4, TILE/2, TILE/2);
                }
            }

            // Draw score at top-left
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.drawString("Score: " + score, 10, 20);

            // Draw Pause prompt in top-right
            g.setColor(new Color(255, 255, 255, 180));
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("ESC / P : Pause", WIDTH * TILE - 105, 20);

            // Draw PAUSED overlay if paused
            if (paused) {
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, WIDTH * TILE, HEIGHT * TILE);

                g.setColor(Color.YELLOW);
                g.setFont(new Font("Arial", Font.BOLD, 40));
                String pmsg = "PAUSED";
                int pw = g.getFontMetrics().stringWidth(pmsg);
                g.drawString(pmsg, (WIDTH * TILE - pw) / 2, HEIGHT * TILE / 2 - 10);

                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
                String sub = "Press ESC or P to Resume";
                int sw = g.getFontMetrics().stringWidth(sub);
                g.drawString(sub, (WIDTH * TILE - sw) / 2, HEIGHT * TILE / 2 + 30);
            }

        } else {
            // --- Game Over screen ---
            // Draw background image dimmed
            if (bgImage != null) {
                g.drawImage(bgImage, 0, 0, WIDTH * TILE, HEIGHT * TILE, null);
                // Dark overlay so text is readable
                g.setColor(new Color(0, 0, 0, 150));
                g.fillRect(0, 0, WIDTH * TILE, HEIGHT * TILE);
            }

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            String msg = "Game Over";
            int msgW = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (WIDTH * TILE - msgW) / 2, HEIGHT * TILE / 2 - 20);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            String info = "Score: " + score;
            int infoW = g.getFontMetrics().stringWidth(info);
            g.drawString(info, (WIDTH * TILE - infoW) / 2, HEIGHT * TILE / 2 + 20);

            String restart = "Press SPACE to Restart";
            int restW = g.getFontMetrics().stringWidth(restart);
            g.drawString(restart, (WIDTH * TILE - restW) / 2, HEIGHT * TILE / 2 + 55);
        }
    }

    // --- Game Logic (called every tick by Timer) ---
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move();
            checkFood();
            checkCollision();
        }
        repaint();
    }

    // Move the snake: shift body, then move head
    void move() {
        // Shift each body part to the position of the one before it
        for (int i = length - 1; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }
        // Move head in current direction
        switch (direction) {
            case 'U': snakeY[0] -= TILE; break;
            case 'D': snakeY[0] += TILE; break;
            case 'L': snakeX[0] -= TILE; break;
            case 'R': snakeX[0] += TILE; break;
        }
    }

    // Check if head is on the food
    void checkFood() {
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            length++;
            score += 10;
            placeFood();
        }
    }

    // Check if the snake hit a wall or itself
    void checkCollision() {
        // Hit wall?
        if (snakeX[0] < 0 || snakeX[0] >= WIDTH * TILE ||
            snakeY[0] < 0 || snakeY[0] >= HEIGHT * TILE) {
            running = false;
        }
        // Hit itself?
        for (int i = 1; i < length; i++) {
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                running = false;
            }
        }
        if (!running) {
            if (timer != null) timer.stop();
            pauseMusic();
        }
    }

    // --- Keyboard Input ---
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // Pause / Resume with ESC or P key
        if ((key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_P) && running) {
            paused = !paused;
            if (paused) {
                pauseMusic();
            } else {
                resumeMusic();
            }
            repaint();
            return;
        }

        // Ignore movement controls while paused
        if (paused) return;

        // Change direction (prevent reversing into yourself)
        if (key == KeyEvent.VK_UP    && direction != 'D') direction = 'U';
        if (key == KeyEvent.VK_DOWN  && direction != 'U') direction = 'D';
        if (key == KeyEvent.VK_LEFT  && direction != 'R') direction = 'L';
        if (key == KeyEvent.VK_RIGHT && direction != 'L') direction = 'R';

        // Restart with SPACE when game is over
        if (key == KeyEvent.VK_SPACE && !running) {
            startGame();
        }
    }

    // Required by KeyListener (not used)
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

    // --- Main: create window and start ---
    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame game = new SnakeGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null); // center on screen
        frame.setResizable(false);
        frame.setVisible(true);
    }
}

