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
    private Scanner sc;
    private int x,y;

    Human(char type){
        super(type);
        this.sc = new Scanner(System.in);
    }
    @Override
    void turn(Field field, Gamer enemy){
        do {
            System.out.println("Enter X and Y (1 - " + field.getSize() + "):");
            x = sc.nextInt() - 1;
            y = sc.nextInt() - 1;
        } while (!field.isCellValid(x, y));
        field.setMapPoint(y,x,this.type);
    }
}

class Painter {
    Painter(Field field){
        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                System.out.print(field.getMapPoint(i,j));
            }
            System.out.println();
        }
    }
    void paint(Field field){
        for (int i = 0; i < field.getSize(); i++) {
            for (int j = 0; j < field.getSize(); j++) {
                System.out.print(field.getMapPoint(i, j));
            }
            System.out.println();
        }
        System.out.println("===================");
    }
}

class GameLogic {
    void go(int SIZE){
        Field field = new Field(SIZE);
        Painter painter = new Painter(field);
        Gamer ai = new AI('0');
        Gamer human = new Human('X');
        GameWindow gameWindow = new GameWindow();
        while (true) {
            human.turn(field,ai);
            painter.paint(field);
            if (checkWin(field,human)) {
                System.out.println("You win!");
                break;
            }
            if (field.isMapFull()) {
                System.out.println("Sorry, DRAW!");
                break;
            }
            ai.turn(field,human);
            painter.paint(field);
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
    private final int WINDOW_SIZE = 300;
    private final int WINDOW_DX = 9;
    private final int WINDOW_DY = 57;
    // private final int CELL_SIZE = WINDOW_SIZE / FIELD_SIZE;
    private final String BTN_START = "Заново";
    private final String BTN_EXIT = "Завершить";
    private Canvas canvas;
    private final int FIELD_SIZE;

    GameWindow(final Field field, Painter painter, int SIZE){
        this.FIELD_SIZE = SIZE;
        setTitle(TITLE_OF_PROGRAM);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setBounds(START_POSITION, START_POSITION, WINDOW_SIZE + WINDOW_DX,
                WINDOW_SIZE + WINDOW_DY);
        setResizable(false);
        this.canvas = new Canvas();
        canvas.setBackground(Color.WHITE);
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                System.out.printf("%d - %d",e.getX(),e.getY());
            }
        });
        JButton start = new JButton(BTN_START);
        start.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                field.init(FIELD_SIZE);
                canvas.repaint();
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
        add(canvas,BorderLayout.CENTER);
        setVisible(true);
    }

    class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            painter.paint(g);
        }
    }
}