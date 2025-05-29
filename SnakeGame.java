import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class SnakeGame extends JFrame {
    public SnakeGame() {
        setTitle("🍏 Snake Game 🍏");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        add(new MenuPanel(this));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void startGame() {
        getContentPane().removeAll();
        GamePanel gamePanel = new GamePanel(this);
        add(gamePanel);
        revalidate();
        repaint();
        gamePanel.requestFocusInWindow();
    }

    public void showGameOver(int score) {
        getContentPane().removeAll();
        add(new GameOverPanel(this, score));
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SnakeGame::new);
    }
}

class MenuPanel extends JPanel {
    public MenuPanel(SnakeGame frame) {
        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(30, 30, 60));
        setLayout(new BorderLayout());

        JLabel title = new JLabel("🐍 SNAKE GAME 🐍", SwingConstants.CENTER);
        title.setFont(new Font("Verdana", Font.BOLD, 48));
        title.setForeground(new Color(144, 238, 144));
        title.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
        add(title, BorderLayout.CENTER);

        JButton startBtn = new JButton("▶ START");
        startBtn.setFont(new Font("Verdana", Font.BOLD, 28));
        startBtn.setForeground(Color.WHITE);
        startBtn.setBackground(new Color(50, 200, 50));
        startBtn.setFocusPainted(false);
        startBtn.setPreferredSize(new Dimension(200, 60));
        startBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(30, 100, 30), 4),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        startBtn.addActionListener(e -> frame.startGame());

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(startBtn);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));
        add(btnPanel, BorderLayout.SOUTH);
    }
}

class GameOverPanel extends JPanel {
    public GameOverPanel(SnakeGame frame, int score) {
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.BLACK);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(20, 0, 20, 0);

        JLabel gameOverLabel = new JLabel("GAME OVER");
        gameOverLabel.setFont(new Font("Impact", Font.BOLD, 50));
        gameOverLabel.setForeground(new Color(200, 30, 30));
        add(gameOverLabel, gbc);

        gbc.gridy++;
        JLabel scoreLabel = new JLabel("Mele mangiate: " + score);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 26));
        scoreLabel.setForeground(Color.WHITE);
        add(scoreLabel, gbc);

        gbc.gridy++;
        JButton restartBtn = new JButton("🔄 Restart");
        restartBtn.setFont(new Font("Verdana", Font.BOLD, 24));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setBackground(new Color(80, 80, 200));
        restartBtn.setFocusPainted(false);
        restartBtn.setPreferredSize(new Dimension(200, 50));
        restartBtn.setBorder(BorderFactory.createLineBorder(new Color(40, 40, 120), 3));
        restartBtn.addActionListener(e -> frame.startGame());
        add(restartBtn, gbc);
    }
}

class GamePanel extends JPanel implements ActionListener {
    private final int TILE_SIZE = 24;  // Pixel grid cell size
    private final int WIDTH = 600;
    private final int HEIGHT = 600;
    private final int ALL_TILES = (WIDTH * HEIGHT) / (TILE_SIZE * TILE_SIZE);
    private final int DELAY = 90;

    private final int[] x = new int[ALL_TILES];
    private final int[] y = new int[ALL_TILES];

    private int bodyParts = 3;
    private int applesEaten;
    private int appleX;
    private int appleY;

    private char direction = 'R';
    private boolean running = false;
    private javax.swing.Timer timer;
    private final SnakeGame mainFrame;

    private final BufferedImage headSprite;
    private final BufferedImage bodySprite;

    public GamePanel(SnakeGame frame) {
        this.mainFrame = frame;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());

        // Initialize pixel sprites
        headSprite = createHeadSprite();
        bodySprite = createBodySprite();

        startGame();
        requestFocusInWindow();
    }

    private BufferedImage createHeadSprite() {
        int size = 6;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int headColor = 0xFF90EE90;
        int eyeColor = 0xFF004400;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                img.setRGB(i, j, headColor);
            }
        }
        img.setRGB(1, 1, eyeColor);
        img.setRGB(4, 1, eyeColor);
        return img;
    }

    private BufferedImage createBodySprite() {
        int size = 6;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        int bodyColor = 0xFF228B22;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                img.setRGB(i, j, bodyColor);
            }
        }
        return img;
    }

    public void startGame() {
        placeApple();
        running = true;
        timer = new javax.swing.Timer(DELAY, this);
        timer.start();
        requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        drawBackground(g2);
        drawApple(g2);
        drawSnake(g2);
        drawScore(g2);
    }

    private void drawBackground(Graphics2D g2) {
        g2.setColor(new Color(10, 10, 30));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
    }

    private void drawApple(Graphics2D g2) {
        g2.setColor(Color.RED);
        g2.fillOval(appleX, appleY, TILE_SIZE, TILE_SIZE);
    }

    private void drawSnake(Graphics2D g2) {
        for (int i = 0; i < bodyParts; i++) {
            BufferedImage sprite = (i == 0) ? headSprite : bodySprite;
            g2.drawImage(sprite, x[i], y[i], TILE_SIZE, TILE_SIZE, null);
        }
    }

    private void drawScore(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(Color.WHITE);
        g2.drawString("🍎 " + applesEaten, 20, 25);
    }

    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }
        switch (direction) {
            case 'U' -> y[0] -= TILE_SIZE;
            case 'D' -> y[0] += TILE_SIZE;
            case 'L' -> x[0] -= TILE_SIZE;
            case 'R' -> x[0] += TILE_SIZE;
        }
    }

    private void placeApple() {
        appleX = (int) (Math.random() * (WIDTH / TILE_SIZE)) * TILE_SIZE;
        appleY = (int) (Math.random() * (HEIGHT / TILE_SIZE)) * TILE_SIZE;
    }

    private void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            placeApple();
        }
    }

    private void checkCollisions() {
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }
        if (!running) {
            timer.stop();
            mainFrame.showGameOver(applesEaten);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyChar()) {
                case 'w' -> {
                    if (direction != 'D') direction = 'U';
                }
                case 's' -> {
                    if (direction != 'U') direction = 'D';
                }
                case 'a' -> {
                    if (direction != 'R') direction = 'L';
                }
                case 'd' -> {
                    if (direction != 'L') direction = 'R';
                }
            }
        }
    }
}