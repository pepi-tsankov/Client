/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.game;

import java.util.HashMap;
import java.util.Vector;
import org.J3dPackage.Line3d;
import org.J3dPackage.Point3d;
import org.lwjglb.engine.graph.Mesh;
import org.lwjglb.engine.graph.Texture;
import org.maths.Math3d;
import static org.game.Options.*;

/**
 *
 * @author Userr
 */
public class ClientChunk extends chunk{
    Mesh mesh=null;
    private boolean changed;
    ClientChunk(Point3d loc){
        super(loc);
    }
    public void cleanup() {
        if(mesh!=null)
        mesh.cleanUp();
        mesh=null;
    }
    @Override
    public void New(){
        for(int x=(int)(loc.x*chunkSize);x<(int)((loc.x+1)*chunkSize);x++){
            for(int y=(int)(loc.y*chunkSize);y<(int)((loc.y+1)*chunkSize);y++){
                for(int z=(int)(loc.z*chunkSize);z<(int)((loc.z+1)*chunkSize);z++){
                    cubes.put("("+x+";"+y+";"+z+")", new Cube(this));
                    cubes.get("("+x+";"+y+";"+z+")").New(new Point3d(x,y,z));
                }
            }
        }
        readyMesh();
    }
    private float[] positions;
    private float[] texCoords;
    private int indices[];
    @SuppressWarnings("LocalVariableHidesMemberVariable")
    public void readyMesh(){
        String[] keys=cubes.keySet().toArray(new String[cubes.size()]);
        Vector<Float> positions;
        Vector<Float> texCoords;
        positions=new Vector();
        texCoords=new Vector();
        positions.clear();
        texCoords.clear();
        Cube c;
        for (String key : keys) {
            c = cubes.get(key);
            if(c.isChanged())c.setupVectors();
            positions.addAll(c.getPoints());
            texCoords.addAll(c.getTexture());
        }
        this.positions=Math3d.ConvertArrayFloat(positions.toArray(new Float[positions.size()]));
        this.texCoords=Math3d.ConvertArrayFloat(texCoords.toArray(new Float[texCoords.size()]));
        indices=new int[this.positions.length/3];
        for(int i=0;i<indices.length;i++){
            indices[i]=i;
        }
        changed=false;
    }
    public void createMesh() throws Exception{
        Texture t=new Texture("/textures/textures.png");
        mesh=new Mesh(positions,texCoords,indices,t);
    }
    public Mesh getMesh() {
        return mesh;
    }
    public boolean hasMesh(){
        return mesh!=null;
    }
    public void discardMesh(){
        if(mesh!=null)
        mesh.cleanUp();
        mesh=null;
    }
}
