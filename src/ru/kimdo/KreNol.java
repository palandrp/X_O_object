package ru.kimdo;

/**
 * @author Pavel Petrikovskiy
 * @version 25.05.17.
 * (на заметку: добавить метод canIWin для AI
 * по типу проверки противника на предвыигрыш
 * только по своим точкам, тогда AI начнет
 * иногда побеждать в сложных ситуациях)
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Random;
import java.util.Scanner;

class KreNol {
    final static int SIZE = 3;
    public static void main(String[] args){
        new GameWindow(SIZE);
    }
}

abstract class Gamer {
    char type;

    Gamer(char type){
        this.type = type;
    }
    abstract void turn(int x, int y, Field field, Gamer gamer);
    char getDot() {
        return this.type;
    }
}

class Field {
    private int SIZE;
    private char DOT_EMPTY = '.';
    private char[][] map;
    private LastTurn lastTurn;

    Field(int SIZE){
        init(SIZE);
    }
    void init(int SIZE){
        this.SIZE = SIZE;
        this.map = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                map[i][j] = DOT_EMPTY;
        this.lastTurn = new LastTurn();
    }
    char getMapPoint(int i, int j){
        return map[i][j];
    }
    int getSize(){
        return SIZE;
    }
    void setMapPoint(int i, int j, char type){
        this.map[i][j] = type;
        setLastTurn(this.lastTurn, i, j);
    }
    private void setLastTurn(LastTurn lastTurn, int x, int y){
        lastTurn.x = x;
        lastTurn.y = y;
    }
    LastTurn getLastTurn(){
        return this.lastTurn;
    }
    char getDot(){
        return DOT_EMPTY;
    }
    boolean isCellValid(int y, int x) {
        if (x < 0 || y < 0 || x >= SIZE || y >= SIZE) return false;
        if (map[y][x] == DOT_EMPTY) return true;
        return false;
    }
    boolean isMapFull(){
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (map[i][j] == DOT_EMPTY) return false;
            }
        }
        return true;
    }

    class LastTurn {
        int x;
        int y;
    }
}

class AI extends Gamer {
    private Random rand;

    AI(char type){
        super(type);
        this.rand = new Random();
    }
    @Override
    void turn(int x, int y, Field field, Gamer enemy){
        boolean isAssInTheFire = checkIsAssInTheFire(field, enemy, x, y);
        if (!isAssInTheFire) {
            do {
                x = rand.nextInt(field.getSize());
                y = rand.nextInt(field.getSize());
            } while (!field.isCellValid(x, y));
            field.setMapPoint(y,x,this.type);
        }
    }
    private boolean checkIsAssInTheFire(Field field, Gamer enemy, int x, int y){
        boolean flag = false;
        int count, size = field.getSize();
        char dot = enemy.getDot();
        char dotEmpty = field.getDot();
        // Диагонали проверяем только если оказались на диагонали
        if (x == y) {
            count = 0;
            for (int i = 0; i < size; i++) {
                if (field.getMapPoint(i,i) == dot) count++;
            }
            if (count == size - 1) {
                for (int i = 0; i < size; i++) {
                    if (field.getMapPoint(i,i) != dot
                            && field.getMapPoint(i,i) == dotEmpty) {
                        x = i;
                        y = i;
                        field.setMapPoint(y,x,this.type);
                        return true;
                    }
                }
            }
        }
        if (x + y == size - 1) {
            count = 0;
            for (int i = 0; i < size; i++) {
                if (field.getMapPoint(i,(size - 1) - i)
                        == dot) {
                    if (field.getMapPoint(i,size - 1)
                            == dot) count++;
                }
            }
            if (count == size - 1) {
                for (int i = 0; i < size; i++) {
                    if (field.getMapPoint(i,(size - 1) - i)
                            != dot
                            && field.getMapPoint(i,(size - 1) - i)
                                == dotEmpty) {
                        x = (size - 1) - i;
                        y = i;
                        field.setMapPoint(y,x,this.type);
                        return true;
                    }
                }
            }
        }
        // Проверяем столбец
        count = 0;
        for (int i = 0; i < size; i++) {
            if (field.getMapPoint(y,i) == dot) {
                if (field.getMapPoint(y,i) == dot) count++;
            }
        }
        if (count == size - 1) {
            for (int i = 0; i < size; i++) {
                if (field.getMapPoint(y,i) != dot
                        && field.getMapPoint(y,i) == dotEmpty) {
                    x = i;
                    field.setMapPoint(y,x,this.type);
                    return true;
                }
            }
        }
        // Проверяем строку
        count = 0;
        for (int i = 0; i < size; i++) {
            if (field.getMapPoint(i,x) == dot) {
                if (field.getMapPoint(i,x) == dot) count++;
            }
        }
        if (count == size - 1) {
            for (int i = 0; i < size; i++) {
                if (field.getMapPoint(i,x) != dot
                        && field.getMapPoint(i,x) == dotEmpty) {
                    y = i;
                    field.setMapPoint(y,x,this.type);
                    return true;
                }
            }
        }
        return false;
    }
}

class Human extends Gamer {
    Human(char type){
        super(type);
    }
    @Override
    void turn(int x, int y, Field field, Gamer enemy){
        field.setMapPoint(y,x,this.type);
    }
}

class GameLogic {
    boolean checkWin(Field field, Gamer gamer) {
        int x = field.getLastTurn().y;
        int y = field.getLastTurn().x;
        char dot = gamer.getDot();
        int size = field.getSize();
        // Метод поддерживает любое кол-во фишек и полей
        boolean flag = false;
        // Диагонали проверяем только если оказались на диагонали
        if (x == y) {
            for (int i = 0; i < size; i++) {
                if (field.getMapPoint(i,i) == dot) flag = true;
                else {
                    flag = false;
                    break;
                }
            }
            if (flag) return true;
        }
        if (x + y == size - 1) {
            for (int i = 0; i < size; i++) {
                if (field.getMapPoint(i,(size - 1) - i)
                        == dot) flag = true;
                else {
                    flag = false;
                    break;
                }
            }
            if (flag) return true;
        }
        // Проверяем столбец
        for (int i = 0; i < size; i++) {
            if (field.getMapPoint(y,i) == dot)
                flag = true;
            else {
                flag = false;
                break;
            }
        }
        if (flag) return true;
        // Проверяем строку
        for (int i = 0; i < size; i++) {
            if (field.getMapPoint(i,x) == dot) flag = true;
            else {
                flag = false;
                break;
            }
        }
        if (flag) return true;
        return false;
    }
    String getYouWinMsg(){
        return "Вы победили!";
    }

    String getYouLostMsg() {
        return "Вы проиграли!";
    }

    String getDrawMsg() {
        return "Ничья!"; }
}

class GameWindow extends JFrame {
    private int CELL_SIZE;
    private int SIZE;
    private Field field;
    private Gamer ai = new AI('0');
    private Gamer human = new Human('X');
    private GameLogic gameLogic = new GameLogic();
    private Painter painter = new Painter();

    GameWindow(final int SIZE){
        final String TITLE_OF_PROGRAM = "Крестики-нолики";
        final int START_POSITION = 300;
        final int WINDOW_SIZE = 300;
        final int WINDOW_DX = 9;
        final int WINDOW_DY = 57;
        final String BTN_START = "Заново";
        final String BTN_EXIT = "Завершить";
        this.field = new Field(SIZE);
        this.SIZE = SIZE;
        this.CELL_SIZE = WINDOW_SIZE/SIZE;

        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(START_POSITION, START_POSITION, WINDOW_SIZE + WINDOW_DX,
                WINDOW_SIZE + WINDOW_DY);
        setResizable(false);
        painter.setBackground(Color.WHITE);
        painter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (field.isCellValid(e.getX()/CELL_SIZE,e.getY()/CELL_SIZE)) {
                    human.turn(e.getY()/CELL_SIZE,e.getX()/CELL_SIZE, field, ai);
                    painter.repaint();
                    if (field.isMapFull()){
                        JOptionPane.showMessageDialog(GameWindow.this,
                                gameLogic.getDrawMsg());
                        field.init(SIZE);
                        painter.repaint();
                    }
                    if (gameLogic.checkWin(field, human)) {
                        JOptionPane.showMessageDialog(GameWindow.this,
                                gameLogic.getYouWinMsg());
                        field.init(SIZE);
                        painter.repaint();
                    }
                    ai.turn(field.getLastTurn().y, field.getLastTurn().x, field, human);
                    painter.repaint();
                    if (field.isMapFull()){
                        JOptionPane.showMessageDialog(GameWindow.this,
                                gameLogic.getDrawMsg());
                        field.init(SIZE);
                        painter.repaint();
                    }
                    if (gameLogic.checkWin(field, ai)) {
                        JOptionPane.showMessageDialog(GameWindow.this,
                                gameLogic.getYouLostMsg());
                        field.init(SIZE);
                        painter.repaint();
                    }
                }
            }
        });
        JButton start = new JButton(BTN_START);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                field.init(SIZE);
                painter.repaint();
            }
        });
        JButton exit = new JButton(BTN_EXIT);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(start);
        buttonPanel.add(exit);
        setLayout(new BorderLayout());
        add(buttonPanel,BorderLayout.SOUTH);
        add(painter,BorderLayout.CENTER);
        setVisible(true);
    }

    class Painter extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.setColor(Color.lightGray);
            for (int i = 1; i < SIZE; i++) {
                g.drawLine(0, i*CELL_SIZE,
                        SIZE*CELL_SIZE,
                        i*CELL_SIZE);
                g.drawLine(i*CELL_SIZE, 0,
                        i*CELL_SIZE,
                        SIZE*CELL_SIZE);
            }
            Graphics2D g2 = (Graphics2D) g; // use Graphics2D
            g2.setStroke(new BasicStroke(5));
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    if (field.getMapPoint(x,y) == human.getDot()) {
                        g.setColor(Color.blue);
                        g2.draw(new Line2D.Float(
                                x*CELL_SIZE+CELL_SIZE/4,
                                y*CELL_SIZE+CELL_SIZE/4,
                                (x+1)*CELL_SIZE-CELL_SIZE/4,
                                (y+1)*CELL_SIZE-CELL_SIZE/4));
                        g2.draw(new Line2D.Float(
                                x*CELL_SIZE+CELL_SIZE/4,
                                (y+1)*CELL_SIZE-CELL_SIZE/4,
                                (x+1)*CELL_SIZE-CELL_SIZE/4,
                                y*CELL_SIZE+CELL_SIZE/4));
                    }
                    if (field.getMapPoint(x,y) == ai.getDot()) {
                        g.setColor(Color.red);
                        g2.draw(new Ellipse2D.Float(
                                x*CELL_SIZE+CELL_SIZE/4,
                                y*CELL_SIZE+CELL_SIZE/4,
                                CELL_SIZE/2,
                                CELL_SIZE/2));
                    }
                }
            }
        }
    }
}