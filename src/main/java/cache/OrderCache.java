package cache;

import controllers.OrderController;
import model.Order;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it.        :FIX

/**
 * Klassen OrderCache har til formål at gemme databasen i en cache, for at fremme hastigheden af applikationen.
 * Dette gøres ved gemme databasen i en ArrayList, som opdateres enten hvis cachen er forældet eller cachen er tom eller
 * Updateringen bliver gennemtvunget.
 */
public class OrderCache {

    // Opretter en arrayliste hvor ordrer gemmes
    private ArrayList<Order> orders;

    // Tidslængden cache gemmes i
    private long ttl;

    // gemmer hvornår cache er blevet gemt
    private long created;


    /**
     * OrderCache har til formål at hente cachen time to live, for at kunne bliver tjekket i getOrders
     */
    public OrderCache() {
        this.ttl = Config.getOrderTtl();
    }

    /**
     * Metoden getOrders tjekker først om der skal forceUpdate, dette kan gøres når man vil gemmemtvinge
     * en update af cache. Ellers bliver cache opdateret hvis cache er forældet
     * Tilsidst kan en opdatering skyldes at der ikke er noget gemt i cache og derfor må hente cache først
     *
     * @param forceUpdate
     * @return returnere cache med en liste af ordrer
     */
    public ArrayList<Order> getOrders(Boolean forceUpdate) {

        if (forceUpdate
                || ((this.created + this.ttl) <= (System.currentTimeMillis() / 1000L))
                || this.orders == null) {

            //Henter ordrer fra OrderController, da vi ønsker at opdatere cache
            ArrayList<Order> orders = OrderController.getOrders();

            //Sætter den opdatere arrayliste af ordre som instans, samt giver den et tidsstempel, for at kunne tjekke
            //hvor gammel cache er.
            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        // Returnere cache over ordrer
        return this.orders;

    }
}
