/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.util.ArrayList;

/**
 *
 * @author sharv
 */
public abstract class ObservableObject {

    private ArrayList<Observer> observers;
    
    public ObservableObject() {
        observers = new ArrayList<>();
    }
    
    public void addObserver(Observer newObserver) {
        
        if (!observers.contains(newObserver))
            observers.add(newObserver);
    }

    protected void notifyObservers() {
        
        for (Observer o : observers)
        {
            o.update(this);
        }
    }
}
