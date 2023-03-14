package Game;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;

public class MenuPanel extends JPanel implements ActionListener {
    private static final int screenWidth = 960;
    private static final int screenHeight = 800;

    private final MenuButton newGame = new MenuButton("New Game");
    private final MenuButton singlePlayer = new MenuButton("Single Player");
    private final MenuButton multiPlayer = new MenuButton("Multi Player");
    private final MenuButton options = new MenuButton("Options");
    private final MenuButton exit = new MenuButton("Exit");
    private final MenuButton back = new MenuButton("Back");
    private final MenuButton start = new MenuButton("Start");
    private final JLabel menuFrame = new JLabel();
    private final JLabel sizeLabel = new JLabel("Game Board Size");
    private final JComboBox sizeComboBox = new JComboBox(new String[]{"15x15", "16x16", "17x17"});
    private final JLabel graphicsLabel = new JLabel("Graphics: ");
    private final JComboBox graphicsComboBox = new JComboBox(GamePanel.graphicsPreset.values());
    private final JLabel musicLabel = new JLabel("Music: ");
    private final JSlider musicSlider = new JSlider(0,100,100);
    private final JLabel seLabel = new JLabel("SE: ");
    private final JSlider seSlider = new JSlider(0, 100, 100);
    private BufferedImage background;
    static final Sound menuMusic = new Sound("MaidenVoyage", false, Sound.SoundTypes.Music);

    //Game options
    private int gameBoardSize;
    private boolean isSinglePlayer;

    public MenuPanel() {
        setPreferredSize(new Dimension(screenWidth,screenHeight));

        //load menu background image
        try{
            background = ImageIO.read(getClass().getResourceAsStream("/Menu/menuBG.png"));
            menuFrame.setIcon(new ImageIcon(ImageIO.read(getClass().getResourceAsStream("/Menu/newGameFrame.png"))));
        } catch (IOException e){
            e.printStackTrace();
        }

        //set components properties
        sizeLabel.setFont(UI.piratesFont.deriveFont(25f));
        sizeLabel.setForeground(new Color(187,129,65));
        sizeComboBox.setFont(UI.piratesFont.deriveFont(25f));
        sizeComboBox.setBackground(new Color(187,129,65));
        sizeComboBox.setBorder(null);

        graphicsLabel.setFont(UI.piratesFont.deriveFont(25f));
        graphicsLabel.setForeground(new Color(187,129,65));
        graphicsComboBox.setFont(UI.piratesFont.deriveFont(25f));
        graphicsComboBox.setBackground(new Color(187,129,65));
        graphicsComboBox.setBorder(null);
        graphicsComboBox.setSelectedIndex(2);

        musicLabel.setFont(UI.piratesFont.deriveFont(25f));
        musicLabel.setForeground(new Color(187,129,65));
        musicSlider.setBackground(new Color(187,129,65));
        musicSlider.setBorder(BorderFactory.createLineBorder(new Color(94,65,33), 2));

        seLabel.setFont(UI.piratesFont.deriveFont(25f));
        seLabel.setForeground(new Color(187,129,65));
        seSlider.setBackground(new Color(187,129,65));
        seSlider.setBorder(BorderFactory.createLineBorder(new Color(94,65,33), 2));

        newGame.setBounds(635,510,200,60);
        options.setBounds(635,590,200,60);
        exit.setBounds(635,670,200,60);
        singlePlayer.setBounds(630, 510, 220, 55);
        multiPlayer.setBounds(630, 590, 220, 55);
        back.setBounds(680,670,110,50);
        menuFrame.setBounds(580,350,530,530);
        sizeLabel.setBounds(660,500,170,50);
        sizeComboBox.setBounds(650, 540, 170, 50);
        start.setBounds(680,610,110,50);
        graphicsLabel.setBounds(620,505,120,40);
        graphicsComboBox.setBounds(730, 510, 120, 40);
        musicLabel.setBounds(620,555,120,40);
        musicSlider.setBounds(730, 560, 120, 40);
        seLabel.setBounds(620,605,120,40);
        seSlider.setBounds(730, 610, 120, 40);

        //add action listener to components
        newGame.addActionListener(this);
        options.addActionListener(this);
        exit.addActionListener(this);
        singlePlayer.addActionListener(this);
        multiPlayer.addActionListener(this);
        back.addActionListener(this);
        start.addActionListener(this);
        sizeComboBox.addActionListener(this);
        graphicsComboBox.addActionListener(this);

        //add change listener to sound sliders
        musicSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Sound.setMusicVolume(musicSlider.getValue());
                menuMusic.setVolume();
            }
        });

        seSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Sound.setSeVolume(seSlider.getValue());
                UIButton.clickSound.setVolume();
                UIButton.selectSound.setVolume();
            }
        });

        singlePlayer.setVisible(false);
        multiPlayer.setVisible(false);
        menuFrame.setVisible(false);
        back.setVisible(false);
        start.setVisible(false);
        sizeLabel.setVisible(false);
        sizeComboBox.setVisible(false);
        graphicsLabel.setVisible(false);
        graphicsComboBox.setVisible(false);
        musicLabel.setVisible(false);
        musicSlider.setVisible(false);
        seLabel.setVisible(false);
        seSlider.setVisible(false);

        //add components to menu panel
        add(newGame);
        add(singlePlayer);
        add(multiPlayer);
        add(back);
        add(start);
        add(options);
        add(exit);
        add(sizeLabel);
        add(sizeComboBox);
        add(graphicsLabel);
        add(graphicsComboBox);
        add(musicLabel);
        add(musicSlider);
        add(seLabel);
        add(seSlider);
        add(menuFrame);
        setLayout(null);

        gameBoardSize = 15;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background,0,0, 960, 800, null);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(newGame)){
            newGame.setVisible(false);
            options.setVisible(false);
            exit.setVisible(false);
            singlePlayer.setVisible(true);
            multiPlayer.setVisible(true);
            back.setVisible(true);
            menuFrame.setVisible(true);
        }
        else if (e.getSource().equals(exit)){
            System.exit(0);
        }
        else if (e.getSource().equals(options)){
            graphicsLabel.setVisible(true);
            graphicsComboBox.setVisible(true);
            musicLabel.setVisible(true);
            musicSlider.setVisible(true);
            seLabel.setVisible(true);
            seSlider.setVisible(true);
            menuFrame.setVisible(true);
            back.setVisible(true);
            newGame.setVisible(false);
            options.setVisible(false);
            exit.setVisible(false);
        }
        else if (e.getSource().equals(back)){
            if (singlePlayer.isVisible()){
                newGame.setVisible(true);
                options.setVisible(true);
                exit.setVisible(true);
                singlePlayer.setVisible(false);
                multiPlayer.setVisible(false);
                back.setVisible(false);
                menuFrame.setVisible(false);
            }
            else if (graphicsLabel.isVisible()){
                graphicsLabel.setVisible(false);
                graphicsComboBox.setVisible(false);
                musicLabel.setVisible(false);
                musicSlider.setVisible(false);
                seLabel.setVisible(false);
                seSlider.setVisible(false);
                menuFrame.setVisible(false);
                back.setVisible(false);
                newGame.setVisible(true);
                options.setVisible(true);
                exit.setVisible(true);
            }
            else{
                sizeLabel.setVisible(false);
                sizeComboBox.setVisible(false);
                start.setVisible(false);
                singlePlayer.setVisible(true);
                multiPlayer.setVisible(true);
            }
        }
        else if (e.getSource().equals(singlePlayer)){
            isSinglePlayer = true;
            sizeLabel.setVisible(true);
            sizeComboBox.setVisible(true);
            start.setVisible(true);
            singlePlayer.setVisible(false);
            multiPlayer.setVisible(false);
        }
        else if (e.getSource().equals(multiPlayer)){
            isSinglePlayer = false;
            sizeLabel.setVisible(true);
            sizeComboBox.setVisible(true);
            start.setVisible(true);
            singlePlayer.setVisible(false);
            multiPlayer.setVisible(false);
        }
        else if (e.getSource().equals(sizeComboBox)){
            if (sizeComboBox.getSelectedItem() == null) return;
            if (sizeComboBox.getSelectedItem().equals("15x15")) gameBoardSize = 15;
            else if (sizeComboBox.getSelectedItem().equals("16x16")) gameBoardSize = 16;
            else if (sizeComboBox.getSelectedItem().equals("17x17")) gameBoardSize = 17;
            UIButton.selectSound.play();
        }
        else if (e.getSource().equals(graphicsComboBox)){
            if (graphicsComboBox.getSelectedItem() == null) return;
            GamePanel.graphics = (GamePanel.graphicsPreset) graphicsComboBox.getSelectedItem();
            UIButton.selectSound.play();
        }
        else if(e.getSource().equals(start)){
            newGame.setVisible(true);
            options.setVisible(true);
            exit.setVisible(true);
            menuFrame.setVisible(false);
            sizeLabel.setVisible(false);
            sizeComboBox.setVisible(false);
            start.setVisible(false);
            back.setVisible(false);
            GameHandler.startNewGame(gameBoardSize,isSinglePlayer);
        }
    }
}

class MenuButton extends JButton{
    private static final Color pressedColor = new Color(140,97,49);
    private static final Color rolloverColor = new Color(201,154,103);
    private static final Color disabledColor = new Color(126,103,77);
    private static final Color normalColor = new Color(187,129,65);

    public MenuButton (String text) {
        super(text);
        setBorder(BorderFactory.createLineBorder(new Color(94,65,33), 3));
        setFocusPainted(false);

        setContentAreaFilled(false);
        setOpaque(true);

        setBackground(normalColor);
        setForeground(new Color(71,49,25));
        setFont(UI.piratesFont.deriveFont(45f));
        setText(text);

        addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent evt) {
                if (getModel().isPressed()) {
                    setBackground(pressedColor);
                    UIButton.selectSound.play();
                } else if (getModel().isRollover()) {
                    setBackground(rolloverColor);
                    UIButton.clickSound.play();
                } else if (!getModel().isEnabled()) {
                    setBackground(disabledColor);
                } else {
                    setBackground(normalColor);
                }
            }
        });
    }
}
