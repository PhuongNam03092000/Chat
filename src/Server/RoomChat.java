/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.util.ArrayList;

/**
 *
 * @author DELL
 */
public class RoomChat {

    public ArrayList<ClientHandler> array = new ArrayList<>(2);

    public void Add(ClientHandler clientHandler) {
        array.add(clientHandler);
        if(array.contains(clientHandler)){
            System.out.println("Vào được rồi nha");
        }else{
            System.out.println("Đi đâu mất tiu");
        }
    }

    public boolean isFull() {
        return array.size() == 2;
    }

    public boolean contains(ClientHandler clientHandler) {
        return array.contains(clientHandler);
    }

    public int countClient() {
        return array.size();
    }

    public ClientHandler getClient(int i) {
        return array.get(i);
    }

    public void remove(ClientHandler clientHandler) {
        array.remove(clientHandler);
    }

    public void clear() {
        array.clear();
    }

    public ArrayList<ClientHandler> getArray() {
        return array;
    }

    public void setArray(ArrayList<ClientHandler> array) {
        this.array = array;
    }

}
