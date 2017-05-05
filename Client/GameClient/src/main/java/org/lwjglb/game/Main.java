package org.lwjglb.game;

import Connection.MessageObject;
import com.sun.corba.se.spi.ior.ObjectId;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.game.ClientChunk;
import org.game.World;
import org.game.chunk;
import org.lwjglb.engine.GameEngine;
import org.lwjglb.engine.IGameLogic;

public class Main {
    public static Socket clientSocket;
    public static ObjectInputStream in;
    public static ObjectOutputStream out;
    public static MessageObject Message;
    public static AtomicBoolean messageInuse;
    public static void main(String[] args) throws IOException {
        Message=new MessageObject(new Vector(),new Vector(),new Vector());
        messageInuse=new AtomicBoolean(false);
        System.out.println("Enter server ip:");
        int[] ip=readIP();
        System.out.println("Enter server port:");
        int port=readPort();
        System.out.println(port);
        clientSocket=new Socket(InetAddress.getByName(""+ip[0]+"."+ip[1]+"."+ip[2]+"."+ip[3]),port);
        out=new ObjectOutputStream(clientSocket.getOutputStream());
        out.flush();
        in=new ObjectInputStream(clientSocket.getInputStream());
        System.out.println("conected to server");
        Thread c=new Thread(){
            @Override
            public void run(){
                int message=0;
                try {
                    while(true){
                        if(!messageInuse.getAndSet(true)){
                            if(Message.sideMessage.size()>0){
                                System.out.println("sending message"+(message++));
                                out.writeObject(Message);
                                out.flush();
                                Message=new MessageObject(new Vector(), new Vector(), new Vector());
                            }
                            messageInuse.set(false);
                        }
                        try{
                            Thread.sleep(20);
                        }catch(Exception e){}
                    }
                } catch (Exception ex) {
                    if(!ex.getMessage().contains("java.net.SocketOutput")){
                        return;
                    }
                }
            }
        };
        c.start();
        Thread t=new Thread(){
            @Override
            public void run(){
                try {
                    while(true){
                        MessageObject mo=(MessageObject)in.readObject();
                        for(int i=0;i<mo.sideMessage.size();i++){
                            if(mo.sideMessage.get(i).equals("chunk")){
                                World.InsertChunk((ClientChunk)(chunk)mo.object_.get(i));
                            }
                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        t.start();
        try {
            boolean vSync = true;
            IGameLogic gameLogic = new DummyGame();
            GameEngine gameEng = new GameEngine("GAME", 600, 480, vSync, gameLogic);
            gameEng.start();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
    private static int readPort() throws IOException{
        int port=0;
        byte s[]={0,0,0,0,0,0,0,0,0,0,0};
        System.in.read(s);
        String b=new String(s);
        b=b.replace((char)10, '\0');
        b=b.trim();
        if(b.length()>4){System.err.println("FATAL ERROR: illegal size for socket, must be less than 9999");System.exit(1);}
        for(char by :b.toCharArray()){
            if((by>'9'||by<'0')&&by!=0&&by!=10){
                System.err.println("FATAL ERROR: found characters that are not between 0 and 9");
                System.exit(1);
            }else{
                if(by!=0&&by!=10){
                    port*=10;
                    port+=by-'0';
                }
            }
        }
        return port;
    }
    private static int[] readIP() throws IOException{
        int full_ip[]={0,0,0,0};
        int ip_part=0;
        byte s[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
        System.in.read(s);
        String b=new String(s);
        System.out.println(b);
        b=b.replace((char)10, '\0');
        b=b.trim();
        System.out.println(b.length());
        String[] ip=b.split("\\.");
        System.out.println(ip.length);
        for(int i=0;i<ip.length;i++){
            for(char by :ip[i].toCharArray()){
                if((by>'9'||by<'0')&&by!=0&&by!=10){
                    System.err.println("FATAL ERROR: found characters that are not between 0 and 9");
                    System.exit(1);
                }else{
                    if(by!=0&&by!=10){
                        ip_part*=10;
                        ip_part+=(by-'0');
                    }
                }
            }
            if(ip_part>255){System.err.println("FATAL ERROR: a part of the ip is greater than 255"); System.exit(1);}
            full_ip[i]=ip_part;
            System.out.println(ip_part);
            ip_part=0;
        }
        if(full_ip.length!=4){System.err.println("FATAL ERROW: this is not an ip");System.exit(1);}
        return full_ip;
    }
}