package models;
 
import java.util.*;
import javax.persistence.*;
 
import play.db.jpa.*;
 
@Entity
public class JUser extends Model {
 
    public String email;
    public String password;
    public String fullname;
    public boolean isAdmin;
    
    public JUser(String email, String password, String fullname) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
    }
 
}
