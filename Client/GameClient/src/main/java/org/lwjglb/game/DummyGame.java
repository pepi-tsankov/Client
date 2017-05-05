package org.lwjglb.game;

import java.util.Comparator;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.J3dPackage.Line3d;
import org.J3dPackage.Point3d;
import org.J3dPackage.Vector3d;
import org.game.World;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;
import org.lwjglb.engine.IGameLogic;
import org.lwjglb.engine.Window;
import org.maths.Math3d;

public class DummyGame implements IGameLogic {
    
    public static Point3d compare;
    float checking=0;
    private final Renderer renderer;
    
    boolean mouseButton1 =false;
    boolean mouseButton2=false;
    
    boolean keyW =false;
    boolean keyA =false;
    boolean keyS =false;
    boolean keyD =false;
    
    boolean key1 =false;
    boolean key2 =false;
    
    int type=0;
    
    Stack<String> load;
    Stack<Point3d> loadp;
    
    Stack<String> discard;
    Stack<Point3d> discardp;
    
    Stack<String> redo;
    Stack<Point3d> redop;
    Thread checkingThread=null;
    
    public DummyGame() {
        renderer = new Renderer();
        load=new Stack();
        loadp=new Stack();
        discard=new Stack();
        discardp=new Stack();
        redo=new Stack();
        redop=new Stack();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        World.Setup();
    }

    @Override
    public void input(Window window) {
        double[] mouse=window.pullMouseData();
        window.setMouseData(window.getWidth()/2, window.getHeight()/2);
        Vector3f rotation=World.player.getRotation();
        rotation.x+=(float) mouse[1];
        rotation.y+=(float) mouse[0];
        if(rotation.x>90){
            rotation.x=90;
        }
        if(rotation.x<-90){
            rotation.x=-90;
        }
        if(rotation.y<-180){
            rotation.y+=360;
        }
        if(rotation.y>180){
            rotation.y-=360;
        }
        World.player.setRotation(rotation.x, rotation.y, rotation.z);
        if(window.isKeyPressed(GLFW_KEY_X)){
            System.out.println("x");
        }
        if(keyW^window.isKeyPressed(GLFW_KEY_W)){
            keyW= !keyW;
        }
        if(keyS^window.isKeyPressed(GLFW_KEY_S)){
            keyS= !keyS;
        }
        if(keyA^window.isKeyPressed(GLFW_KEY_A)){
            keyA= !keyA;
        }
        if(keyD^window.isKeyPressed(GLFW_KEY_D)){
            keyD=! keyD;
        }
        if(key1^window.isKeyPressed(GLFW_KEY_1)){
            key1= !key1;
            if(key1){
                type=0;
            }
        }
        if(key2^window.isKeyPressed(GLFW_KEY_2)){
            key2= !key2;
            if(key2){
                type=1;
            }
        }
        //
        // DIGGING 
        //
        if (mouseButton1 ^ window.isMouseKeyPressed(GLFW_MOUSE_BUTTON_1)){
            mouseButton1= !mouseButton1;
            if(mouseButton1){
                Thread dig;
                dig = new Thread(){
                    @Override
                    public void run(){
                        Vector3f pos=World.player.getPosition();
                        Vector3f rot=World.player.getRotation();
                        Vector3d rotation=Math3d.rotateY(Math3d.rotateX(new Vector3d(0,0,1),rot.x), -(rot.y+180));
                        Vector<Point3d> cubes=Math3d.LineIntersections(new Point3d(pos.x,pos.y,pos.z), rotation, 5);
                        compare=new Point3d(pos.x,pos.y,pos.z);
                        cubes.sort(new Comparator(){
                            @Override
                            public int compare(Object o1, Object o2) {
                                return (int)((Math3d.distance(compare, (Point3d)o1)-Math3d.distance(compare, (Point3d)o2))*10000);
                            }
                            
                        });
                        Point3d intersection=null;
                        rotation.multiply(5);
                        for(int i=0;(i<cubes.size())&&(intersection==null);i++){
                            intersection=World.intersects(cubes.get(i),new Line3d(new Point3d(pos.x,pos.y,pos.z),new Point3d(pos.x+(rotation.x),pos.y+(rotation.y),pos.z+(rotation.z))));
                        }
                        if(intersection==null) return;
                        World.dig(intersection, 1);
                    }
                };
                dig.start();
            }
        }
        //
        // PLACING
        //
        if (mouseButton2 ^ window.isMouseKeyPressed(GLFW_MOUSE_BUTTON_2)){
            mouseButton2= !mouseButton2;
            if(mouseButton2){
                Thread place;
                place = new Thread(){
                    @Override
                    public void run(){
                        Vector3f pos=World.player.getPosition();
                        Vector3f rot=World.player.getRotation();
                        Vector3d rotation=Math3d.rotateY(Math3d.rotateX(new Vector3d(0,0,1),rot.x), -(rot.y+180));
                        Vector<Point3d> cubes=Math3d.LineIntersections(new Point3d(pos.x,pos.y,pos.z), rotation, 5);
                        compare=new Point3d(pos.x,pos.y,pos.z);
                        cubes.sort(new Comparator(){
                            @Override
                            public int compare(Object o1, Object o2) {
                                return (int)((Math3d.distance(compare, (Point3d)o1)-Math3d.distance(compare, (Point3d)o2))*10000);
                            }
                            
                        });
                        Point3d intersection=null;
                        rotation.multiply(5);
                        for(int i=0;(i<cubes.size())&&(intersection==null);i++){
                            intersection=World.intersects(cubes.get(i),new Line3d(new Point3d(pos.x,pos.y,pos.z),new Point3d(pos.x+(rotation.x),pos.y+(rotation.y),pos.z+(rotation.z))));
                        }
                        if(intersection==null) return;
                        World.place(intersection, (double)1,type);
                    }
                };
                place.start();
            }
        }
        
    }
    
    @Override
    public void update(float interval) {
        long time = System.currentTimeMillis();
        if(keyW){
            World.player.movePosition(0, 0, -1.5f*interval);
        }
        if(keyA){
            World.player.movePosition(-1.5f*interval, 0, 0);
        }
        if(keyS){
            World.player.movePosition(0, 0, 1.5f*interval);
        }
        if(keyD){
            World.player.movePosition(1.5f*interval, 0, 0);
        } 
        if(checking<=0){
            checking=1000;
            checkingThread=new Thread(){
                @Override
                public void run(){
                    Vector3f position =World.player.getPosition();
                    int x=(int)Math.floor(position.x/5);
                    int y=(int)Math.floor(position.y/5);
                    int z=(int)Math.floor(position.z/5);
                    for(int i=x-3;i<=x+3;i++){
                        for(int j=y-3;j<=y+3;j++){
                            for(int l=z-3;l<=z+3;l++){
                                if(i==x-3||i==x+3||j==y-3||j==y+3||l==z-3||l==z+3){
                                    if(World.isChunkLoaded(new Point3d(i,j,l))){
                                        if(!discard.contains("("+i+";"+j+";"+l+")")){
                                           discard.add("("+i+";"+j+";"+l+")");
                                           discardp.add(new Point3d(i,j,l));
                                       }
                                    }
                                }else{
                                   if(!World.isChunkLoaded(new Point3d(i,j,l))){
                                       if(!load.contains("("+i+";"+j+";"+l+")")){
                                           World.CreateChunk(new Point3d(i,j,l));
                                           load.add("("+i+";"+j+";"+l+")");
                                           loadp.add(new Point3d(i,j,l));
                                           
                                       }
                                    }else{
                                        if((World.getChunk(new Point3d(i,j,l))!=null)&&World.getchunk(new Point3d(i,j,l)).isChanged()){
                                            if(!redo.contains("("+i+";"+j+";"+l+")")){
                                                World.getchunk(new Point3d(i,j,l)).readyMesh();
                                                redo.add("("+i+";"+j+";"+l+")");
                                                redop.add(new Point3d(i,j,l));
                                            }
                                        }
                                   }
                                }
                            }
                        }
                    }
                    checking=0.5f;
                }
            };
            checkingThread.start();
        }
        checking-=interval;
        if(((System.currentTimeMillis()-time)<(interval*1000))&&((!discard.isEmpty())||(!load.isEmpty())||(!redo.isEmpty()))){
            if(!discardp.isEmpty()){
                int size=discardp.size();
                try {
                    World.DiscardChunk(discardp.get(size-1));
                } catch (Exception ex) {
                    Logger.getLogger(DummyGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                discardp.remove(size-1);
                discard.remove(size-1);
            }
            if(!loadp.isEmpty()){
                int size=loadp.size();
                try {
                    World.generateMesh(loadp.get(size-1));
                } catch (Exception ex) {
                    Logger.getLogger(DummyGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                loadp.remove(size-1);
                load.remove(size-1);
            }
            if(!redop.isEmpty()){
                int size=redop.size();
                try{
                    World.redoChunk(redop.get(size-1));
                } catch (Exception ex) {
                    Logger.getLogger(DummyGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                redo.remove(size-1);
                redop.remove(size-1);
            }
        }
    }

    @Override
    public void render(Window window) {
        renderer.render(window,World.player, World.GetMeshes());
    }

    @Override
    public void cleanup() {
        renderer.cleanup();
        World.cleanup();
    }

}
