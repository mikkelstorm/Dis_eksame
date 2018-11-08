package cache;

import controllers.OrderController;
import controllers.UserController;
import model.Order;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it.        :FIX
public class UserCache {

    // Opretter en arrayliste hvor brugere gemmes
    private ArrayList<User> users;

    // Tidslængden cache gemmes i
    private long ttl;

    // gemmer hvornår cache er blevet gemt
    private long created;

    public UserCache() {
        this.ttl = Config.getUserTtl();
    }

    public ArrayList<User> getUsers(Boolean forceUpdate) {

        /**
         * Først bliver der tjekket om der skal forceUpdate, dette kan gøres når man vil gemmemtvinge en update af cache
         * Ellers bliver cache opdateret hvis cache er forældet
         * Tilsidst kan en opdatering skyldes at der ikke er noget gemt i cache og derfor må hente cache først
         */
        if (forceUpdate
                || ((this.created + this.ttl) <= (System.currentTimeMillis() / 1000L ))
                || this.users == null) {

            System.out.println(this.created + ttl);
            System.out.println(System.currentTimeMillis() / 1000L);
            System.out.println("Force hver gang");

            //Henter brugere fra UserController, da vi ønsker at opdatere cache
            ArrayList<User> users = UserController.getUsers();

            //Sætter den opdatere arrayliste af ordre som instans, samt giver den et tidsstempel, for at kunne tjekke
            //hvor gammel cache er.
            this.users = users;
            this.created = System.currentTimeMillis() / 1000L;
        }

        // Returnere cache over brugere
        return this.users;

    }

}
