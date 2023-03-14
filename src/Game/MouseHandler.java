package Game;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseHandler implements MouseListener {

    public boolean is_left_clicked = false;
    public boolean is_right_clicked = false;
    public boolean is_middle_clicked = false;
    public boolean is_mouse_on_screen;

    @Override
    public void mouseClicked(MouseEvent e) {
        switch (e.getButton()) {
            case 1 -> is_left_clicked = true;
            case 2 -> is_middle_clicked = true;
            case 3 -> is_right_clicked = true;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        is_mouse_on_screen = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        is_mouse_on_screen = false;
    }

    public void reset_keys(){
        is_left_clicked = false;
        is_right_clicked = false;
        is_middle_clicked = false;
    }
}
