package database;

/**
 * Created by dlopez on 23/10/15.
 */
public class Nombre {
    public int id, count;
    public float score;
    public String nombre;
    public boolean clicked;

    public Nombre (int id, String nombre, float score, int count){
        this.id     = id;
        this.nombre = nombre;
        this.score  = score;
        this.count  = count;
        clicked     = false;
    }

    public String toString(){
        return nombre + "(" +
                " id:" + id +
                " sc:" + score +
                " cn:" + count +
                " )";
    }
}
