package Client;

import Server.Core;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class ChatGUI extends javax.swing.JFrame {

    private BufferedReader in = null;
    private BufferedWriter out = null;
    public String username;
    private Socket socket;
    private String size;
    boolean joined = false;

    public ChatGUI(Socket socket, String username, String size) {
        try {
            initComponents();
            this.socket = socket;
            this.username = username;
            this.setTitle(this.username);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        } catch (IOException ex) {
            Logger.getLogger(ChatGUI.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    Logger.getLogger(ChatGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void windowClosing(WindowEvent e) {
                DisconnectToServer(username);
            }

        });
    }

    private void DisconnectToServer(String username) {
        if (joined == true) {
            Core.Send(out, "SendMessage||Success||" + this.username + "||Disconnected");
        }else{
            Core.Send(out, "Disconnect||null");
        }

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jButton1.setText("Gửi");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Tìm");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 482, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(29, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(32, 32, 32))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public void WaitingMessage(String username) {
        String res = "";
        StringTokenizer stk;
        boolean a = false;
        while (true) {
            try {
                res = in.readLine();
                if (res != null) {
                    stk = new StringTokenizer(res, "||");
                    if (stk != null) {
                        String action = stk.nextToken();
                        if (action.equals("Response")) {
                            String user = stk.nextToken();
                            String message = stk.nextToken();
                            if (message.equals("Disconnected")) {
                                JOptionPane.showMessageDialog(null, user + " đã rời phòng");
                                builder = new StringBuilder();
                                jTextArea1.setText("");
                                break;
                            } else {
                                if (user.equals(this.username)) {
                                    builder.append("You :").append(message).append("\n");
                                } else {
                                    builder.append(user).append(" :").append(message).append("\n");
                                }
                                jTextArea1.setText(builder.toString());
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

    }
    StringBuilder builder = new StringBuilder();

    public void WaitingPartner(String username) {
        username = this.username;
        String res = "";
        try {
            while (true) {
                Core.Send(out, "JoinGroup||Waiting");
                res = in.readLine();
                if (res != null) {
                    if (res.contains("Found")) {
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }

        StringTokenizer stk = new StringTokenizer(res, "||");
        stk.nextToken();
        stk.nextToken();
        String user = stk.nextToken();

        int option = JOptionPane.showConfirmDialog(null, "Bạn có muốn chat với : " + user, "Lời mởi", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try {
                Core.Send(out, "JoinGroup||Yes||" + user);
                Thread.sleep(1000);
                if (res != null) {
                    res = in.readLine();
                    if (res.contains("Success")) {
                        stk = new StringTokenizer(res, "||");
                        stk.nextToken();
                        stk.nextToken();
                        user = stk.nextToken();
                        JOptionPane.showMessageDialog(null, "Kết nối thành công với :" + user);
                        joined = true;
                    }
                    if (res.contains("TimeOut")) {
                        System.out.println(res);
                        JOptionPane.showMessageDialog(this, "Đã hết thời gian chờ");
                    }
                }

            } catch (InterruptedException | IOException ex) {
                System.out.println(ex);
            }
        }
        if (option == JOptionPane.NO_OPTION) {
            try {
                Core.Send(out, "JoinGroup||No||" + user);
                Thread.sleep(1000);

                res = in.readLine();
                if (res != null) {
                    if (res.contains("Fail")) {
                        stk = new StringTokenizer(res, "||");
                        stk.nextToken();
                        stk.nextToken();
                        String message = stk.nextToken();
                        System.out.println(message);
                        if (message.endsWith("null")) {
                            JOptionPane.getRootFrame().dispose();
                        } else {
                            JOptionPane.showMessageDialog(null, message);
                        }
                        joined = false;
                    }
                }
            } catch (InterruptedException | IOException ex) {
                System.out.println(ex);
            }
        }

        if (joined == true) {
            Thread thread = new Thread(() -> WaitingMessage(this.username));
            thread.start();
        }
    }

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if (joined == false) {
            JOptionPane.showMessageDialog(null, "Hãy tìm kiếm đi!!!");
        } else {
            String message = jTextField1.getText();
            if (message.equals("")) {
                JOptionPane.showMessageDialog(null, "Hãy nhập vào tin nhắn bạn muốn gửi");
            } else {
                Core.Send(out, "SendMessage||Success||" + this.username + "||" + message);
                jTextField1.setText("");
            }
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:

        Core.Send(out, "JoinGroup||JoinQueue");
        Thread thread = new Thread(() -> WaitingPartner(this.username));
        thread.start();

    }//GEN-LAST:event_jButton2ActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}
