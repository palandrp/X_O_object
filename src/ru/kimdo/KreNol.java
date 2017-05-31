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
    private final static int SIZE = 5;

    public static void main(String[] args){

        new GameLogic().go(SIZE);
    }
}

abstract class Gamer {
    char type;

    Gamer(char type){
        this.type = type;
    }
    abstract void turn(Field field, Gamer gamer);
    abstract void graphicTurn(int x, int y, Field field);
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
    boolean isCellValid(int x, int y) {
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
    void graphicTurn(int x, int y, Field field){
        System.out.println("Not used");
    }
    @Override
    void turn(Field field, Gamer enemy){
        int x = field.getLastTurn().y;
        int y = field.getLastTurn().x;
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
    private int x,y;

    Human(char type){
        super(type);
    }
    @Override
    void graphicTurn(int x, int y, Field field){
        field.setMapPoint(y,x,this.type);
    }
    @Override
    void turn(Field field, Gamer enemy){
        System.out.println("Deprecated");
    }
}

class Painter extends JPanel {
    private int FIELD_SIZE;
    private int CELL_SIZE;
    private char HUMAN_DOT;
    private char AI_DOT;
    private Field field;

    Painter(Field field, Gamer human, Gamer ai){
        this.FIELD_SIZE = field.getSize();
        this.CELL_SIZE = GameWindow.WINDOW_SIZE / FIELD_SIZE;
        this.HUMAN_DOT = human.getDot();
        this.AI_DOT = ai.getDot();
        this.field = field;
    }
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(Color.lightGray);
        for (int i = 1; i < FIELD_SIZE; i++) {
            g.drawLine(0, i*CELL_SIZE, FIELD_SIZE*CELL_SIZE, i*CELL_SIZE);
            g.drawLine(i*CELL_SIZE, 0, i*CELL_SIZE, FIELD_SIZE*CELL_SIZE);
        }
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(5));
        for (int y = 0; y < FIELD_SIZE; y++) {
            for (int x = 0; x < FIELD_SIZE; x++) {
                if (field.getMapPoint(x,y) == HUMAN_DOT) {
                    g.setColor(Color.blue);
                    g2.draw(new Line2D.Float(x*CELL_SIZE+CELL_SIZE/4,
                            y*CELL_SIZE+CELL_SIZE/4, (x+1)*CELL_SIZE-CELL_SIZE/4,
                            (y+1)*CELL_SIZE-CELL_SIZE/4));
                    g2.draw(new Line2D.Float(x*CELL_SIZE+CELL_SIZE/4,
                            (y+1)*CELL_SIZE-CELL_SIZE/4, (x+1)*CELL_SIZE-CELL_SIZE/4,
                            y*CELL_SIZE+CELL_SIZE/4));
                }
                if (field.getMapPoint(x,y) == AI_DOT) {
                    g.setColor(Color.red);
                    g2.draw(new Ellipse2D.Float(x*CELL_SIZE+CELL_SIZE/4,
                            y*CELL_SIZE+CELL_SIZE/4, CELL_SIZE/2, CELL_SIZE/2));
                }
            }
        }
    }
}

class GameLogic {
    void go(int SIZE){
        Field field = new Field(SIZE);
        Gamer ai = new AI('0');
        Gamer human = new Human('X');
        Painter painter = new Painter(field,human,ai);
        GameWindow gameWindow = new GameWindow(field,painter,human);
        while (true) {
            gameWindow.waitTurn(field,painter,human);
            painter.repaint();
            if (checkWin(field,human)) {
                System.out.println("You win!");
                break;
            }
            if (field.isMapFull()) {
                System.out.println("Sorry, DRAW!");
                break;
            }
            ai.turn(field,human);
            painter.repaint();
            if (checkWin(field,ai)) {
                System.out.println("You lost!");
                break;
            }
            if (field.isMapFull()) {
                System.out.println("Sorry, DRAW!");
            }
        }
        System.out.println("GAME OVER!");
    }
    private boolean checkWin(Field field, Gamer gamer) {
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
}

class GameWindow extends JFrame {
    private final String TITLE_OF_PROGRAM = "Крестики-нолики";
    private final int START_POSITION = 300;
    static final int WINDOW_SIZE = 300;
    private final int WINDOW_DX = 9;
    private final int WINDOW_DY = 57;
    private final String BTN_START = "Заново";
    private final String BTN_EXIT = "Завершить";
    private int CELL_SIZE;
    private int X,Y;

    GameWindow(final Field field,final Painter painter,final Gamer human){
        this.CELL_SIZE = WINDOW_SIZE / field.getSize();
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(START_POSITION, START_POSITION, WINDOW_SIZE + WINDOW_DX,
                WINDOW_SIZE + WINDOW_DY);
        setResizable(false);
        painter.setBackground(Color.WHITE);
        waitTurn(field,painter,human);
        JButton start = new JButton(BTN_START);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                field.init(field.getSize());
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
    void waitTurn(final Field field,Painter painter,final Gamer human){
        painter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                do {
                    X = e.getX() / CELL_SIZE;
                    Y = e.getY() / CELL_SIZE;
                } while (!field.isCellValid(X, Y));
                human.graphicTurn(X,Y,field);
            }
        });
    }
}
